package com.example.EliteCart.Service;

import com.example.EliteCart.Entity.Order;
import com.example.EliteCart.Entity.OrderItem;
import com.example.EliteCart.Entity.Product;
import com.example.EliteCart.Entity.User;
import com.example.EliteCart.Enum.Role;
import com.example.EliteCart.Exception.ResourceNotFoundException;
import com.example.EliteCart.Repository.OrderRepository;
import com.example.EliteCart.Repository.ProductRepository;
import com.example.EliteCart.Repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SellerService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public SellerService(ProductRepository productRepository,
                         OrderRepository orderRepository,
                         UserRepository userRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> getSellerDashboard(String sellerName) {
        // ✅ Verify authenticated seller matches requested sellerName
        String currentEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Allow admin or matching seller
        if (currentUser.getRole() != Role.ROLE_ADMIN &&
                !currentUser.getUsername().equalsIgnoreCase(sellerName)) {
            throw new RuntimeException("Access denied: You can only view your own dashboard");
        }

        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();

        List<Product> sellerProducts = productRepository.findAll().stream()
                .filter(p -> p.getSeller() != null && p.getSeller().equalsIgnoreCase(sellerName))
                .collect(Collectors.toList());
        stats.put("seller", sellerName);
        stats.put("totalProducts", sellerProducts.size());

        List<Order> sellerOrders = orderRepository.findAll().stream()
                .filter(order -> order.getItems() != null && order.getItems().stream()
                        .anyMatch(item -> item.getProduct() != null &&
                                sellerName.equalsIgnoreCase(item.getProduct().getSeller())))
                .collect(Collectors.toList());
        stats.put("totalOrders", sellerOrders.size());

        double revenueToday = sellerOrders.stream()
                .filter(o -> o.getOrderDate() != null && o.getOrderDate().toLocalDate().isEqual(today))
                .flatMap(o -> o.getItems().stream())
                .filter(i -> i.getProduct() != null &&
                        sellerName.equalsIgnoreCase(i.getProduct().getSeller()))
                .mapToDouble(OrderItem::getPrice)
                .sum();
        stats.put("revenueToday", revenueToday);

        LocalDate firstDay = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDay = today.with(TemporalAdjusters.lastDayOfMonth());

        double monthlyRevenue = sellerOrders.stream()
                .filter(o -> o.getOrderDate() != null)
                .filter(o -> {
                    LocalDate date = o.getOrderDate().toLocalDate();
                    return !date.isBefore(firstDay) && !date.isAfter(lastDay);
                })
                .flatMap(o -> o.getItems().stream())
                .filter(i -> i.getProduct() != null &&
                        sellerName.equalsIgnoreCase(i.getProduct().getSeller()))
                .mapToDouble(OrderItem::getPrice)
                .sum();
        stats.put("revenueThisMonth", monthlyRevenue);

        Map<String, Long> productSales = sellerOrders.stream()
                .flatMap(order -> order.getItems().stream()
                        .filter(item -> item.getProduct() != null
                                && item.getProduct().getSeller() != null
                                && item.getProduct().getSeller().equalsIgnoreCase(sellerName)
                                && item.getProduct().getName() != null)
                        .map(item -> Map.entry(item.getProduct().getName(), (long) item.getQuantity())))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.summingLong(Map.Entry::getValue)
                ));

        List<Map<String, Object>> bestSellingProducts = productSales.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("productName", entry.getKey());
                    map.put("unitsSold", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
        stats.put("bestSellingProducts", bestSellingProducts);

        return stats;
    }

    public List<Map<String, Object>> getSellerOrders(String sellerName) {
        // ✅ Verify authenticated seller matches requested sellerName
        String currentEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (currentUser.getRole() != Role.ROLE_ADMIN &&
                !currentUser.getUsername().equalsIgnoreCase(sellerName)) {
            throw new RuntimeException("Access denied: You can only view your own orders");
        }

        return orderRepository.findAll().stream()
                .flatMap(order -> order.getItems().stream()
                        .filter(item -> item.getProduct() != null &&
                                sellerName.equalsIgnoreCase(item.getProduct().getSeller()))
                        .map(item -> {
                            Map<String, Object> orderMap = new HashMap<>();
                            orderMap.put("orderId", order.getId());
                            orderMap.put("productName", item.getProduct().getName());
                            orderMap.put("quantity", item.getQuantity());
                            orderMap.put("price", item.getPrice());
                            orderMap.put("orderDate", order.getOrderDate());
                            orderMap.put("status", order.getStatus());
                            return orderMap;
                        }))
                .collect(Collectors.toList());
    }

    public Map<String, Object> updateProductDiscount(Long productId, Double discount) {
        // ✅ Get authenticated seller
        String currentEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        // ✅ Verify seller owns this product (unless admin)
        if (currentUser.getRole() != Role.ROLE_ADMIN &&
                !product.getSeller().equalsIgnoreCase(currentUser.getUsername())) {
            throw new RuntimeException("Access denied: You can only update your own products");
        }

        if (discount < 0 || discount > 90) {
            throw new RuntimeException("Invalid discount value. Must be between 0 and 90.");
        }

        product.setDiscount(discount);
        productRepository.save(product);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("productId", product.getId());
        response.put("name", product.getName());
        response.put("originalPrice", product.getPrice());
        response.put("discount", product.getDiscount());
        response.put("discountedPrice", product.getDiscountedPrice());
        response.put("message", discount > 0
                ? "Discount applied successfully!"
                : "Discount removed (reset to 0).");

        return response;
    }
}