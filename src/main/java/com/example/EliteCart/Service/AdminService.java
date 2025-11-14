package com.example.EliteCart.Service;

import com.example.EliteCart.Entity.Order;
import com.example.EliteCart.Entity.Product;
import com.example.EliteCart.Entity.User;
import com.example.EliteCart.Exception.ResourceNotFoundException;
import com.example.EliteCart.Repository.OrderRepository;
import com.example.EliteCart.Repository.ProductRepository;
import com.example.EliteCart.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public AdminService(UserRepository userRepository,
                        ProductRepository productRepository,
                        OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public String blockUser(Long id, String reason) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(false);
        user.setBlockedReason(reason != null ? reason : "Blocked by admin");
        userRepository.save(user);
        return "User " + user.getUsername() + " blocked successfully.";
    }

    public String unblockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(true);
        user.setBlockedReason(null);
        userRepository.save(user);
        return "User " + user.getUsername() + " unblocked successfully.";
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public String deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with ID " + id);
        }
        productRepository.deleteById(id);
        return "Product deleted successfully.";
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Map<String, Object> getDashboard() {
        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();

        List<Order> todayOrders = orderRepository.findAll().stream()
                .filter(o -> o.getOrderDate().toLocalDate().isEqual(today))
                .toList();
        stats.put("totalOrdersToday", todayOrders.size());

        double revenueToday = todayOrders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();
        stats.put("revenueToday", revenueToday);

        LocalDate firstDay = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDay = today.with(TemporalAdjusters.lastDayOfMonth());
        double monthlyRevenue = orderRepository.findAll().stream()
                .filter(o -> {
                    LocalDate date = o.getOrderDate().toLocalDate();
                    return !date.isBefore(firstDay) && !date.isAfter(lastDay);
                })
                .mapToDouble(Order::getTotalAmount)
                .sum();
        stats.put("revenueThisMonth", monthlyRevenue);

        List<Map<String, Object>> bestProducts = productRepository.findAll().stream()
                .sorted((p1, p2) -> Double.compare(
                        Optional.ofNullable(p2.getRatings()).orElse(0.0),
                        Optional.ofNullable(p1.getRatings()).orElse(0.0)
                ))
                .limit(5)
                .map(p -> {
                    Map<String, Object> productMap = new HashMap<>();
                    productMap.put("name", p.getName());
                    productMap.put("category", p.getCategory());
                    productMap.put("price", p.getPrice());
                    productMap.put("ratings", p.getRatings());
                    return productMap;
                })
                .collect(Collectors.toList());

        stats.put("topRatedProducts", bestProducts);

        return stats;
    }
}