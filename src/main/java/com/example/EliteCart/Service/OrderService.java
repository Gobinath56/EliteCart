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
import com.example.EliteCart.Repository.OrderRepository;
import com.example.EliteCart.Repository.ProductRepository;
import com.example.EliteCart.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // ------------------------------------------------------------
    // 1) PLACE ORDER
    // ------------------------------------------------------------
    public Order placeOrder(OrderRequestDto orderRequestDto) {

        User user = userRepository.findById(orderRequestDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔥 STEP 1: Validate all products belong to ONE seller
        String sellerName = null;

        for (OrderItemRequestDto item : orderRequestDto.getItems()) {

            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

            if (sellerName == null) {
                sellerName = product.getSeller(); // first product seller
            } else if (!sellerName.equalsIgnoreCase(product.getSeller())) {
                throw new RuntimeException(
                        "You cannot order products from multiple sellers in a single order. " +
                                "Seller mismatch between " + sellerName + " and " + product.getSeller()
                );
            }
        }

        // 🔥 STEP 2: Create order as usual
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setDeliveryDate(LocalDateTime.now().plusDays(5));
        order.setStatus(OrderStatus.CONFIRMED);
        order.setShippingAddress(orderRequestDto.getShippingAddress());
        order.setContactNumber(orderRequestDto.getContactNumber());

        double total = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequestDto itemDto : orderRequestDto.getItems()) {

            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemDto.getProductId()));

            if (product.getStock() < itemDto.getQuantity()) {
                throw new RuntimeException("Insufficient stock for: " + product.getName());
            }

            // decrease stock
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

    // ------------------------------------------------------------
    // 2) CONVERT TO DTO
    // ------------------------------------------------------------
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

    // ------------------------------------------------------------
    // 3) CANCEL ORDER
    // ------------------------------------------------------------
    public Order cancelOrder(Long id, String reason) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED)
            throw new RuntimeException("Order already cancelled");

        // restore stock
        for (OrderItem item : order.getItems()) {
            Product p = item.getProduct();
            p.setStock(p.getStock() + item.getQuantity());
            productRepository.save(p);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);

        return orderRepository.save(order);
    }

    // ------------------------------------------------------------
    // 4) UPDATE DELIVERY STATUS (SELLER / ADMIN)
    // ------------------------------------------------------------
    public Order updateDeliveryStatus(Long orderId, OrderStatus status) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED)
            throw new RuntimeException("Cannot update cancelled orders");

        order.setStatus(status);

        // if delivered → set delivery date
        if (status == OrderStatus.DELIVERED) {
            order.setDeliveryDate(LocalDateTime.now());
        }

        return orderRepository.save(order);
    }
}
