package com.example.EliteCart.Dtos;

import jakarta.validation.constraints.*;

public class ProductReviewDto {

    @NotNull(message = "Product ID is required")
    private Long productid;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Double rating;

    @NotBlank(message = "Comment is required")
    @Size(min = 10, max = 500, message = "Comment must be between 10 and 500 characters")
    private String comment;

    // Getters and Setters
    public Long getId() { return productid; }
    public void setId(Long productid) { this.productid = productid; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public ProductReviewDto() {}
}