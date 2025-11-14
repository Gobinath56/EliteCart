package com.example.EliteCart.Dtos;

import jakarta.validation.constraints.NotBlank;

public class BlockUserDto {

    @NotBlank(message = "Block reason is required")
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public BlockUserDto() {}
    public BlockUserDto(String reason) { this.reason = reason; }
}