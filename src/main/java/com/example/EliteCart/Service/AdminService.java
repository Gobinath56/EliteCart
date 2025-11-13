package com.example.EliteCart.Service;

import com.example.EliteCart.Entity.Order;
import com.example.EliteCart.Entity.Product;
import com.example.EliteCart.Entity.User;
import com.example.EliteCart.Repository.OrderRepository;
import com.example.EliteCart.Repository.ProductRepository;
import com.example.EliteCart.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    // ✅ 1. Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ✅ 2. Block user
    public String blockUser(Long id, String reason) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(false);
        user.setBlockedReason(reason != null ? reason : "Blocked by admin");
        userRepository.save(user);
        return "User " + user.getUsername() + " blocked successfully.";
    }

    // ✅ 3. Unblock user
    public String unblockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(true);
        user.setBlockedReason(null);
        userRepository.save(user);
        return "User " + user.getUsername() + " unblocked successfully.";
    }

    // ✅ 4. Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // ✅ 5. Delete product
    public String deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with ID " + id);
        }
        productRepository.deleteById(id);
        return "Product deleted successfully.";
    }

    // ✅ 6. Get all orders
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // ✅ 7. Dashboard Summary (Final Correct Version)
    public Map<String, Object> getDashboard() {
        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();

        // Orders today
        List<Order> todayOrders = orderRepository.findAll().stream()
                .filter(o -> o.getOrderDate().toLocalDate().isEqual(today))
                .toList();
        stats.put("totalOrdersToday", todayOrders.size());

        // Revenue today
        double revenueToday = todayOrders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();
        stats.put("revenueToday", revenueToday);

        // Monthly revenue
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

        // ✅ Top rated products (Fixed version)
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
