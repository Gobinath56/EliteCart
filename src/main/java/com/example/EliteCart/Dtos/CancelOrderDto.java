package com.example.EliteCart.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CancelOrderDto {

    @NotBlank(message = "Cancellation reason is required")
    @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public CancelOrderDto() {}
    public CancelOrderDto(String reason) { this.reason = reason; }
}