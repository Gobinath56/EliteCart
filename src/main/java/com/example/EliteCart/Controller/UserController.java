package com.example.EliteCart.Controller;

import com.example.EliteCart.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<Map<String, Object>>> getOrderHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getOrderHistory(userId));
    }

    @PatchMapping("/{userId}/update")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> body) {

        return ResponseEntity.ok(userService.updateUserProfile(userId, body));
    }

}

