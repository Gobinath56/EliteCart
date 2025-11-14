package com.example.EliteCart.Controller;

import com.example.EliteCart.Dtos.CancelOrderDto;
import com.example.EliteCart.Dtos.OrderRequestDto;
import com.example.EliteCart.Dtos.OrderResponseDto;
import com.example.EliteCart.Dtos.UpdateOrderStatusDto;
import com.example.EliteCart.Entity.Order;
import com.example.EliteCart.Service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/create")
    public ResponseEntity<OrderResponseDto> placeOrder(@Valid @RequestBody OrderRequestDto orderRequestDto) {
        Order saved = orderService.placeOrder(orderRequestDto);
        return ResponseEntity.ok(orderService.convertToDto(saved));
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDto> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody CancelOrderDto cancelDto) {

        Order cancelled = orderService.cancelOrder(orderId, cancelDto.getReason());
        return ResponseEntity.ok(orderService.convertToDto(cancelled));
    }

    @PreAuthorize("hasRole('ROLE_SELLER') or hasRole('ROLE_ADMIN')")
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusDto statusDto) {

        Order updated = orderService.updateDeliveryStatus(orderId, statusDto.getStatus());
        return ResponseEntity.ok(orderService.convertToDto(updated));
    }
}