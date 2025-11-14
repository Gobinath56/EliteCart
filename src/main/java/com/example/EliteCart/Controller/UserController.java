package com.example.EliteCart.Controller;

import com.example.EliteCart.Dtos.UpdateUserProfileDto;
import com.example.EliteCart.Dtos.UserDto;
import com.example.EliteCart.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<Map<String, Object>>> getOrderHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getOrderHistory(userId));
    }

    @PatchMapping("/{userId}/update")
    public ResponseEntity<UserDto> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserProfileDto updateDto)
    {
        return ResponseEntity.ok(userService.updateUserProfile(userId, updateDto));
    }
}