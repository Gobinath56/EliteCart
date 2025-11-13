package com.example.EliteCart.Dtos;

import com.example.EliteCart.Enum.Role;

public class RegisterRequestDto {
    private String username;
    private String email;
    private String password;
    private String phoneNumber;
    private Role role; // ROLE_USER, ROLE_SELLER, ROLE_ADMIN

    // Getters & Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
