package com.example.EliteCart.Service;

import com.example.EliteCart.Dtos.OrderItemDto;
import com.example.EliteCart.Dtos.OrderItemRequestDto;
import com.example.EliteCart.Dtos.OrderRequestDto;
import com.example.EliteCart.Dtos.OrderResponseDto;
import com.example.EliteCart.Entity.Order;
import com.example.EliteCart.Entity.OrderItem;
import com.example.EliteCart.Entity.Product;
import com.example.EliteCart.Entity.User;
import com.example.EliteCart.Enum.OrderStatus;
import com.example.EliteCart.Enum.Role;
import com.example.EliteCart.Exception.ResourceNotFoundException;
import com.example.EliteCart.Repository.OrderRepository;
import com.example.EliteCart.Repository.ProductRepository;
import com.example.EliteCart.Repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Order placeOrder(OrderRequestDto orderRequestDto) {
        // ✅ Use authenticated user instead of userId from request
        String currentEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate all products belong to ONE seller
        String sellerName = null;

        for (OrderItemRequestDto item : orderRequestDto.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.getProductId()));

            if (sellerName == null) {
                sellerName = product.getSeller();
            } else if (!sellerName.equalsIgnoreCase(product.getSeller())) {
                throw new RuntimeException(
                        "You cannot order products from multiple sellers in a single order. " +
                                "Seller mismatch between " + sellerName + " and " + product.getSeller()
                );
            }
        }

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setDeliveryDate(LocalDateTime.now().plusDays(5));
        order.setStatus(OrderStatus.CONFIRMED);
        order.setShippingAddress(orderRequestDto.getShippingAddress());
        order.setContactNumber(orderRequestDto.getContactNumber());
        order.setSeller(sellerName);

        double total = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequestDto itemDto : orderRequestDto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemDto.getProductId()));

            if (product.getStock() < itemDto.getQuantity()) {
                throw new RuntimeException("Insufficient stock for: " + product.getName());
            }

            // Decrease stock
            product.setStock(product.getStock() - itemDto.getQuantity());
            productRepository.save(product);

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemDto.getQuantity());
            item.setPrice(product.getPrice() * itemDto.getQuantity());
            item.setOrder(order);

            orderItems.add(item);
            total += item.getPrice();
        }

        order.setItems(orderItems);
        order.setTotalAmount(total);
        return orderRepository.save(order);
    }

    public OrderResponseDto convertToDto(Order order) {
        OrderResponseDto dto = new OrderResponseDto();

        dto.setId(order.getId());
        dto.setUsername(order.getUser().getUsername());
        dto.setEmail(order.getUser().getEmail());

        dto.setShippingAddress(order.getShippingAddress());
        dto.setContactNumber(order.getContactNumber());

        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setOrderDate(order.getOrderDate());
        dto.setDeliveryDate(order.getDeliveryDate());
        dto.setCancelReason(order.getCancelReason());

        if (!order.getItems().isEmpty()) {
            String sellerName = order.getItems().get(0).getProduct().getSeller();
            dto.setSeller(sellerName);
        }

        List<OrderItemDto> itemDtos = order.getItems().stream().map(item -> {
            OrderItemDto i = new OrderItemDto();
            i.setProductName(item.getProduct().getName());
            i.setPrice(item.getPrice());
            i.setQuantity(item.getQuantity());
            return i;
        }).collect(Collectors.toList());

        dto.setItems(itemDtos);
        return dto;
    }

    @Transactional
    public Order cancelOrder(Long id, String reason) {
        // ✅ Get authenticated user
        String currentEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // ✅ Verify user owns this order (or is admin)
        if (!order.getUser().getId().equals(currentUser.getId()) &&
                currentUser.getRole() != Role.ROLE_ADMIN) {
            throw new RuntimeException("Access denied: You can only cancel your own orders");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order already cancelled");
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel delivered orders");
        }

        // Restore stock
        for (OrderItem item : order.getItems()) {
            Product p = item.getProduct();
            p.setStock(p.getStock() + item.getQuantity());
            productRepository.save(p);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateDeliveryStatus(Long orderId, OrderStatus status) {
        // ✅ Get authenticated user
        String currentEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // ✅ Verify seller owns products in this order (or is admin)
        if (currentUser.getRole() != Role.ROLE_ADMIN) {
            boolean ownsProducts = order.getItems().stream()
                    .allMatch(item -> item.getProduct().getSeller()
                            .equalsIgnoreCase(currentUser.getUsername()));

            if (!ownsProducts) {
                throw new RuntimeException("Access denied: You can only update orders for your products");
            }
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot update cancelled orders");
        }

        order.setStatus(status);

        if (status == OrderStatus.DELIVERED) {
            order.setDeliveryDate(LocalDateTime.now());
        }

        return orderRepository.save(order);
    }
}