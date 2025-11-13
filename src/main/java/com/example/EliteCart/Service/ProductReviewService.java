package com.example.EliteCart.Service;

import com.example.EliteCart.Dtos.ProductReviewDto;
import com.example.EliteCart.Entity.Product;
import com.example.EliteCart.Entity.ProductReview;
import com.example.EliteCart.Repository.ProductRepository;
import com.example.EliteCart.Repository.ProductReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ProductReviewService {

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private ProductRepository productRepository;

    public ResponseEntity<?> addReview(ProductReviewDto productReviewDto) {

        // ✅ Step 1: Validate product ID
        if (productReviewDto.getId() == null) {
            return ResponseEntity.badRequest().body("Product ID cannot be null");
        }

        // ✅ Step 2: Fetch product
        Product product = productRepository.findById(productReviewDto.getId())
                .orElseThrow(() -> new RuntimeException("No product found with ID: " + productReviewDto.getId()));

        // ✅ Step 3: Create and save review
        ProductReview productReview = new ProductReview();
        productReview.setComment(productReviewDto.getComment());
        productReview.setRating(productReviewDto.getRating());
        productReview.setProduct(product);

        productReviewRepository.save(productReview);

        // ✅ Step 4: Update product ratings and review count
        int currentCount = product.getNumOfReviews();
        double newAverageRating = ((product.getRatings() * currentCount) + productReviewDto.getRating()) / (currentCount + 1);

        product.setNumOfReviews(currentCount + 1);
        product.setRatings(newAverageRating);

        productRepository.save(product);

        // ✅ Step 5: Return updated product
        return ResponseEntity.ok(productRepository.findById(product.getId()).get());
    }
}
