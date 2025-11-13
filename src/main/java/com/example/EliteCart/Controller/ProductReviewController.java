package com.example.EliteCart.Controller;

import com.example.EliteCart.Dtos.ProductReviewDto;
import com.example.EliteCart.Service.ProductReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/products/review")
public class ProductReviewController {

    @Autowired
    private ProductReviewService productReviewService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping
    public ResponseEntity<?> addReview(@RequestBody @Valid ProductReviewDto productReviewDto) {
        return productReviewService.addReview(productReviewDto);
    }

}
