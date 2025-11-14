package com.example.EliteCart.Service;

import com.example.EliteCart.Dtos.UpdateUserProfileDto;
import com.example.EliteCart.Dtos.UserDto;
import com.example.EliteCart.Entity.Order;
import com.example.EliteCart.Entity.User;
import com.example.EliteCart.Enum.Role;
import com.example.EliteCart.Exception.ResourceNotFoundException;
import com.example.EliteCart.Repository.OrderRepository;
import com.example.EliteCart.Repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(OrderRepository orderRepository,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Map<String, Object>> getOrderHistory(Long userId) {
        // ✅ Get authenticated user
        String currentEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // ✅ Verify user can only access their own orders (unless admin)
        if (!currentUser.getId().equals(userId) &&
                currentUser.getRole() != Role.ROLE_ADMIN) {
            throw new RuntimeException("Access denied: You can only view your own orders");
        }

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

    public UserDto updateUserProfile(Long userId, UpdateUserProfileDto updateDto) {
        // ✅ Get authenticated user
        String currentEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // ✅ Verify user can only update their own profile (unless admin)
        if (!currentUser.getId().equals(userId) &&
                currentUser.getRole() != Role.ROLE_ADMIN) {
            throw new RuntimeException("Access denied: You can only update your own profile");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update fields
        if (updateDto.getUsername() != null && !updateDto.getUsername().trim().isEmpty()) {
            user.setUsername(updateDto.getUsername());
        }

        if (updateDto.getPhoneNumber() != null && !updateDto.getPhoneNumber().trim().isEmpty()) {
            user.setPhoneNumber(updateDto.getPhoneNumber());
        }

        if (updateDto.getPassword() != null && !updateDto.getPassword().trim().isEmpty()) {
            // ✅ IMPORTANT: Hash the password!
            user.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }

        userRepository.save(user);
        return convertToDto(user);
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole().name());
        dto.setActive(user.isActive());
        return dto;
    }
}