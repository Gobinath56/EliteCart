package com.example.EliteCart.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.List;

@Entity
@Table(name = "Products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "price is required")
    @PositiveOrZero(message = "value must be positive")
    private Double price;

    @NotBlank(message = "description is required")
    private String description;  // ✅ Fixed typo

    @NotBlank(message = "category is required")
    private String category;

    private Double ratings = 0.0;

    @NotBlank(message = "seller name is required")
    private String seller;

    @NotNull(message = "stock is required")
    private Integer stock;

    @PositiveOrZero(message = "Discount must be 0 or positive")
    private Double discount = 0.0;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "product_id")
    private List<ProductImage> images;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "productid")
    @JsonManagedReference
    private List<ProductReview> reviews;  // ✅ Fixed typo

    private Integer numOfReviews = 0;

    @Transient
    public Double getDiscountedPrice() {
        if (discount != null && discount > 0) {
            return price - (price * discount / 100);
        }
        return price;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getDescription() { return description; }  // ✅ Fixed
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getRatings() { return ratings; }
    public void setRatings(Double ratings) { this.ratings = ratings; }

    public String getSeller() { return seller; }
    public void setSeller(String seller) { this.seller = seller; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }

    public Integer getNumOfReviews() { return numOfReviews; }
    public void setNumOfReviews(Integer numOfReviews) { this.numOfReviews = numOfReviews; }

    public List<ProductImage> getImages() { return images; }
    public void setImages(List<ProductImage> images) { this.images = images; }

    public List<ProductReview> getReviews() { return reviews; }  // ✅ Fixed
    public void setReviews(List<ProductReview> reviews) { this.reviews = reviews; }

    public void addImage(ProductImage image) {
        if (images == null) {
            images = new java.util.ArrayList<>();
        }
        images.add(image);
        image.setProduct(this);
    }

    public void removeImage(ProductImage image) {
        if (images != null) {
            images.remove(image);
            image.setProduct(null);
        }
    }

    public Product() {}
}