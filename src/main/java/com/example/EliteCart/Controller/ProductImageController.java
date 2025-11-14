package com.example.EliteCart.Controller;

import com.example.EliteCart.Entity.ProductImage;
import com.example.EliteCart.Service.ProductImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@RestController
@RequestMapping("/api/products")
public class ProductImageController {

    private final ProductImageService imageService;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    public ProductImageController(ProductImageService imageService) {
        this.imageService = imageService;
    }

    @PreAuthorize("hasRole('ROLE_SELLER') or hasRole('ROLE_ADMIN')")
    @PostMapping("/{productId}/upload-image")
    public ResponseEntity<?> uploadImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File cannot be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            return ResponseEntity.badRequest()
                    .body("Only JPEG, PNG, GIF, and WebP images are allowed");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body("File size exceeds 5MB limit");
        }

        try {
            ProductImage savedImage = imageService.addImage(productId, file);
            return ResponseEntity.ok(savedImage);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Failed to upload image: " + e.getMessage());
        }
    }
}