package com.example.EliteCart.Controller;

import com.example.EliteCart.Service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/seller")
@PreAuthorize("hasRole('ROLE_SELLER') or hasRole('ROLE_ADMIN')")
public class SellerController {

    @Autowired
    private SellerService sellerService;

    @GetMapping("/dashboard/{sellerName}")
    public ResponseEntity<Map<String, Object>> getSellerDashboard(@PathVariable String sellerName) {
        return ResponseEntity.ok(sellerService.getSellerDashboard(sellerName));
    }

    @GetMapping("/orders/{sellerName}")
    public ResponseEntity<?> getSellerOrders(@PathVariable String sellerName) {
        return ResponseEntity.ok(sellerService.getSellerOrders(sellerName));
    }

    @PatchMapping("/product/{productId}/discount")
    public ResponseEntity<Map<String, Object>> updateProductDiscount(
            @PathVariable Long productId,
            @RequestParam Double discount) {

        return ResponseEntity.ok(sellerService.updateProductDiscount(productId, discount));
    }
}

