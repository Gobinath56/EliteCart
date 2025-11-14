package com.example.EliteCart.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public class OrderRequestDto {
    // ✅ REMOVED userId - security risk!

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Invalid phone number format")
    private String contactNumber;

    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderItemRequestDto> items;

    // Getters & Setters
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public List<OrderItemRequestDto> getItems() { return items; }
    public void setItems(List<OrderItemRequestDto> items) { this.items = items; }
}