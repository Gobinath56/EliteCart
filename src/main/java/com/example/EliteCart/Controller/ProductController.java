package com.example.EliteCart.Controller;

import com.example.EliteCart.Dtos.ProductDto;
import com.example.EliteCart.Entity.Product;
import com.example.EliteCart.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * ✅ Get all products (with pagination and sorting)
     * Accessible to: USER, SELLER, ADMIN
     */
    @GetMapping("/all")
    public ResponseEntity<Page<ProductDto>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order) {

        Page<ProductDto> products = productService.getAllProductsSorted(page, size, sortBy, order);
        return ResponseEntity.ok(products);
    }

    /**
     * ✅ Get products by category + filters
     * Accessible to: USER, SELLER, ADMIN
     */
    @GetMapping("/{category}")
    public ResponseEntity<Page<ProductDto>> getByCategory(
            @PathVariable String category,
            @RequestParam(required = false, defaultValue = "0") Double min,
            @RequestParam(required = false, defaultValue = "99999999") Double max,
            @RequestParam(required = false, defaultValue = "0") Double ratings,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Page<ProductDto> result = productService.getByCategory(category, min, max, ratings, page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * ✅ Add a new product
     * Accessible to: SELLER, ADMIN (for now public)
     */
    @PreAuthorize("hasRole('ROLE_SELLER') or hasRole('ROLE_ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<ProductDto> addProduct(@RequestBody Product product) {
        ProductDto savedProduct = productService.addProduct(product);
        return ResponseEntity.ok(savedProduct);
    }

    /**
     * ✅ Update a product
     * Accessible to: SELLER, ADMIN
     */
    @PreAuthorize("hasRole('ROLE_SELLER') or hasRole('ROLE_ADMIN')")
    @PatchMapping("/update/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product) {

        ProductDto updatedProduct = productService.updateProduct(id, product);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * ✅ Delete product
     * Accessible to: SELLER, ADMIN
     */
    @PreAuthorize("hasRole('ROLE_SELLER') or hasRole('ROLE_ADMIN')")
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<String> removeProduct(@PathVariable Long id) {
        productService.removeProduct(id);
        return ResponseEntity.ok("Product with ID " + id + " deleted successfully.");
    }

    /**
     * ✅ Find by product ID
     * Accessible to: USER, SELLER, ADMIN
     */
    @GetMapping("/find/{id}")
    public ResponseEntity<?> findProductById(@PathVariable Long id) {
        Optional<ProductDto> product = productService.findId(id);
        return product.isPresent()
                ? ResponseEntity.ok(product.get())
                : ResponseEntity.status(404).body("Product not found with ID: " + id);
    }

    /**
     * ✅ Search product by name
     * Accessible to: USER, SELLER, ADMIN
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto>> searchProducts(
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "0") Double min,
            @RequestParam(required = false, defaultValue = "99999999") Double max,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ProductDto> products = productService.searchByName(name, min, max, page, size);
        return ResponseEntity.ok(products);
    }
}
