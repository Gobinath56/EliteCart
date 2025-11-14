package com.example.EliteCart.Controller;

import com.example.EliteCart.Dtos.LoginRequestDto;
import com.example.EliteCart.Entity.User;
import com.example.EliteCart.Enum.Role;
import com.example.EliteCart.Exception.ResourceNotFoundException;
import com.example.EliteCart.Repository.UserRepository;
import com.example.EliteCart.Service.CustomUserDetailsService;
import com.example.EliteCart.Util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          CustomUserDetailsService userDetailsService,
                          UserRepository userRepository,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email already registered"));
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole(Role.ROLE_USER);
        }

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Check if user is blocked
            if (!user.isActive()) {
                return ResponseEntity.status(403).body(Map.of(
                        "message", "Account is blocked",
                        "reason", user.getBlockedReason() != null ? user.getBlockedReason() : "No reason provided"
                ));
            }

            final String token = jwtUtil.generateToken(userDetails, user.getRole().name());

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "token", token,
                    "role", user.getRole().name(),
                    "username", user.getUsername(),
                    "email", user.getEmail()
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Invalid email or password"));
        }
    }
}