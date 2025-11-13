package com.example.EliteCart.Controller;

import com.example.EliteCart.Dtos.OrderRequestDto;
import com.example.EliteCart.Dtos.OrderResponseDto;
import com.example.EliteCart.Entity.Order;
import com.example.EliteCart.Enum.OrderStatus;
import com.example.EliteCart.Service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/create")
    public ResponseEntity<OrderResponseDto> placeOrder(@Valid @RequestBody OrderRequestDto orderRequestDto) {
        Order saved = orderService.placeOrder(orderRequestDto);
        return ResponseEntity.ok(orderService.convertToDto(saved));
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> body) {

        String reason = body.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Cancellation reason is required");
        }

        Order cancelled = orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(orderService.convertToDto(cancelled));
    }

    @PreAuthorize("hasRole('ROLE_SELLER') or hasRole('ROLE_ADMIN')")
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> body) {

        String statusStr = body.get("status");
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Status is required");
        }

        try {
            OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
            Order updated = orderService.updateDeliveryStatus(orderId, status);
            return ResponseEntity.ok(orderService.convertToDto(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status: " + statusStr);
        }
    }
}