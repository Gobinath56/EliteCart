package com.example.EliteCart.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

@Entity
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(value = 1)
    @Max(value = 5)
    private Double rating;

    private String comment;

    // ✅ ADDED - Track who wrote the review
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "productid", nullable = false)
    @JsonBackReference
    private Product product;

    private LocalDateTime createdAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public ProductReview(Long id, Double rating, String comment, User user) {
        this.id = id;
        this.rating = rating;
        this.comment = comment;
        this.user = user;
    }

    public ProductReview() {}
}