package com.example.EliteCart.Service;

import com.example.EliteCart.Dtos.ProductReviewDto;
import com.example.EliteCart.Entity.Product;
import com.example.EliteCart.Entity.ProductReview;
import com.example.EliteCart.Entity.User;
import com.example.EliteCart.Enum.OrderStatus;
import com.example.EliteCart.Exception.ResourceNotFoundException;
import com.example.EliteCart.Repository.OrderRepository;
import com.example.EliteCart.Repository.ProductRepository;
import com.example.EliteCart.Repository.ProductReviewRepository;
import com.example.EliteCart.Repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProductReviewService {

    private final ProductReviewRepository productReviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ProductReviewService(ProductReviewRepository productReviewRepository,
                                ProductRepository productRepository,
                                OrderRepository orderRepository,
                                UserRepository userRepository) {
        this.productReviewRepository = productReviewRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public ResponseEntity<?> addReview(ProductReviewDto productReviewDto) {
        // ✅ Get authenticated user
        String currentEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (productReviewDto.getId() == null) {
            return ResponseEntity.badRequest().body("Product ID cannot be null");
        }

        Product product = productRepository.findById(productReviewDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No product found with ID: " + productReviewDto.getId()));

        // ✅ Verify user has purchased and received this product
        boolean hasPurchased = orderRepository.findByUserId(currentUser.getId())
                .stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .flatMap(order -> order.getItems().stream())
                .anyMatch(item -> item.getProduct().getId().equals(product.getId()));

        if (!hasPurchased) {
            return ResponseEntity.badRequest()
                    .body("You can only review products you have purchased and received");
        }

        // ✅ Check if user already reviewed this product
        if (product.getReviews() != null) {
            boolean alreadyReviewed = product.getReviews().stream()
                    .anyMatch(review -> review.getUser() != null &&
                            review.getUser().getId().equals(currentUser.getId()));

            if (alreadyReviewed) {
                return ResponseEntity.badRequest()
                        .body("You have already reviewed this product");
            }
        }

        ProductReview review = new ProductReview();
        review.setComment(productReviewDto.getComment());
        review.setRating(productReviewDto.getRating());
        review.setProduct(product);
        review.setUser(currentUser);  // ✅ Set user
        review.setCreatedAt(LocalDateTime.now());

        productReviewRepository.save(review);

        // Update product ratings and review count
        int currentCount = product.getNumOfReviews();
        double newAverageRating = ((product.getRatings() * currentCount) + productReviewDto.getRating()) / (currentCount + 1);

        product.setNumOfReviews(currentCount + 1);
        product.setRatings(newAverageRating);

        productRepository.save(product);

        return ResponseEntity.ok(productRepository.findById(product.getId()).get());
    }
}