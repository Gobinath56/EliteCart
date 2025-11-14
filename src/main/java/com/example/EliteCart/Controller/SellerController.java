package com.example.EliteCart.Controller;

import com.example.EliteCart.Service.SellerService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seller")
@PreAuthorize("hasRole('ROLE_SELLER') or hasRole('ROLE_ADMIN')")
@Validated
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @GetMapping("/dashboard/{sellerName}")
    public ResponseEntity<Map<String, Object>> getSellerDashboard(@PathVariable String sellerName) {
        return ResponseEntity.ok(sellerService.getSellerDashboard(sellerName));
    }

    @GetMapping("/orders/{sellerName}")
    public ResponseEntity<List<Map<String, Object>>> getSellerOrders(@PathVariable String sellerName) {
        return ResponseEntity.ok(sellerService.getSellerOrders(sellerName));
    }

    @PatchMapping("/product/{productId}/discount")
    public ResponseEntity<Map<String, Object>> updateProductDiscount(
            @PathVariable Long productId,
            @RequestParam @Min(0) @Max(90) Double discount) {

        return ResponseEntity.ok(sellerService.updateProductDiscount(productId, discount));
    }
}