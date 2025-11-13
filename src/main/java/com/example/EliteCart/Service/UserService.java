package com.example.EliteCart.Service;

import com.example.EliteCart.Entity.Order;
import com.example.EliteCart.Entity.User;
import com.example.EliteCart.Repository.OrderRepository;
import com.example.EliteCart.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Map<String, Object>> getOrderHistory(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);

        return orders.stream().map(order -> {
            Map<String, Object> orderMap = new LinkedHashMap<>();
            orderMap.put("orderId", order.getId());
            orderMap.put("orderDate", order.getOrderDate());
            orderMap.put("status", order.getStatus());
            orderMap.put("deliveryDate", order.getDeliveryDate());
            orderMap.put("totalAmount", order.getTotalAmount());
            orderMap.put("cancelReason", order.getCancelReason());

            List<Map<String, Object>> items = order.getItems().stream()
                    .filter(i -> i.getProduct() != null)
                    .map(i -> {
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("product", i.getProduct().getName());
                        itemMap.put("quantity", i.getQuantity());
                        itemMap.put("price", i.getProduct().getPrice());
                        return itemMap;
                    })
                    .toList();

            orderMap.put("items", items);
            return orderMap;
        }).toList();
    }

    public Map<String, Object> updateUserProfile(Long userId, Map<String, Object> updates) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✔ Username editable
        if (updates.containsKey("username")) {
            user.setUsername((String) updates.get("username"));
        }

        // ✔ Phone number editable
        if (updates.containsKey("phoneNumber")) {
            user.setPhoneNumber((String) updates.get("phoneNumber"));
        }

        // ✔ Password editable (if you add encryption, encode here)
        if (updates.containsKey("password")) {
            user.setPassword((String) updates.get("password"));
        }

        // ❌ Email NOT editable
        if (updates.containsKey("email")) {
            throw new RuntimeException("Email cannot be changed once registered!");
        }

        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Profile updated successfully");
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("phoneNumber", user.getPhoneNumber());
        response.put("email", user.getEmail()); // return original email

        return response;
    }

}
