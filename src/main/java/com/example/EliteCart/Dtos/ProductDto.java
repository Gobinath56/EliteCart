package com.example.EliteCart.Dtos;

import com.example.EliteCart.Entity.ProductReview;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDto {
    private Long id;
    private String name;
    private String category;
    private String descripation;
    private Double price;
    private Double ratings;
    private String seller;
    private Integer stock;
    private Integer numOfReviews;


    private Double discount;         // only if > 0
    private Double discountedPrice;

    // ✅ List of review objects (optional)
    private List<ProductReview> reviwes;

    // ✅ List of image URLs
    private List<String> imageUrls;

    // ✅ Getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescripation() { return descripation; }
    public void setDescripation(String descripation) { this.descripation = descripation; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getRatings() { return ratings; }
    public void setRatings(Double ratings) { this.ratings = ratings; }

    public String getSeller() { return seller; }
    public void setSeller(String seller) { this.seller = seller; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getNumOfReviews() { return numOfReviews; }
    public void setNumOfReviews(Integer numOfReviews) { this.numOfReviews = numOfReviews; }

    public List<ProductReview> getReviwes() { return reviwes; }
    public void setReviwes(List<ProductReview> reviwes) { this.reviwes = reviwes; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Double getDiscountedPrice() {
        return discountedPrice;
    }

    public void setDiscountedPrice(Double discountedPrice) {
        this.discountedPrice = discountedPrice;
    }
}
