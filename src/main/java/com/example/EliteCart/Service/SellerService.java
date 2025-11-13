package com.example.EliteCart.Service;

import com.example.EliteCart.Entity.Order;
import com.example.EliteCart.Entity.OrderItem;
import com.example.EliteCart.Entity.Product;
import com.example.EliteCart.Repository.OrderRepository;
import com.example.EliteCart.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SellerService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    // ✅ Seller Dashboard Summary
    public Map<String, Object> getSellerDashboard(String sellerName) {
        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();

        // ✅ 1. Seller Products
        List<Product> sellerProducts = productRepository.findAll().stream()
                .filter(p -> p.getSeller() != null && p.getSeller().equalsIgnoreCase(sellerName))
                .collect(Collectors.toList());
        stats.put("seller", sellerName);
        stats.put("totalProducts", sellerProducts.size());

        // ✅ 2. Orders containing seller’s products
        List<Order> sellerOrders = orderRepository.findAll().stream()
                .filter(order -> order.getItems() != null && order.getItems().stream()
                        .anyMatch(item -> item.getProduct() != null &&
                                sellerName.equalsIgnoreCase(item.getProduct().getSeller())))
                .collect(Collectors.toList());
        stats.put("totalOrders", sellerOrders.size());

        // ✅ 3. Revenue Today
        double revenueToday = sellerOrders.stream()
                .filter(o -> o.getOrderDate() != null && o.getOrderDate().toLocalDate().isEqual(today))
                .flatMap(o -> o.getItems().stream())
                .filter(i -> i.getProduct() != null &&
                        sellerName.equalsIgnoreCase(i.getProduct().getSeller()))
                .mapToDouble(OrderItem::getPrice)
                .sum();
        stats.put("revenueToday", revenueToday);

        // ✅ 4. Revenue This Month
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

        // ✅ 5. Best-Selling Products (FIXED VERSION)
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

    // ✅ Seller Orders (History)
    public List<Map<String, Object>> getSellerOrders(String sellerName) {
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
    // update the discount
    public Map<String, Object> updateProductDiscount(Long productId, Double discount) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

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
