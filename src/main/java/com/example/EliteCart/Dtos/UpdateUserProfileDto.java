package com.example.EliteCart.Dtos;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateUserProfileDto {

    @Size(min = 2, max = 50, message = "Username must be between 2 and 50 characters")
    private String username;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UpdateUserProfileDto() {}
}