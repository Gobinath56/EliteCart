package com.example.EliteCart.Controller;

import com.example.EliteCart.Dtos.BlockUserDto;
import com.example.EliteCart.Entity.Order;
import com.example.EliteCart.Entity.Product;
import com.example.EliteCart.Entity.User;
import com.example.EliteCart.Service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")  // ✅ Added class-level security
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PatchMapping("/users/{id}/block")
    public ResponseEntity<String> blockUser(
            @PathVariable Long id,
            @Valid @RequestBody BlockUserDto blockDto) {
        return ResponseEntity.ok(adminService.blockUser(id, blockDto.getReason()));
    }

    @PatchMapping("/users/{id}/unblock")
    public ResponseEntity<String> unblockUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.unblockUser(id));
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(adminService.getAllProducts());
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.deleteProduct(id));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(adminService.getAllOrders());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }
}