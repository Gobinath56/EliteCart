package com.example.EliteCart.Service;

import com.example.EliteCart.Dtos.LoginRequestDto;
import com.example.EliteCart.Dtos.LoginResponseDto;
import com.example.EliteCart.Dtos.RegisterRequestDto;
import com.example.EliteCart.Entity.User;
import com.example.EliteCart.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    // ✅ Register a new user
    public String register(RegisterRequestDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered!");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); // you can hash later with bcrypt
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setRole(dto.getRole());
        user.setActive(true);

        userRepository.save(user);
        return "User registered successfully!";
    }


}
