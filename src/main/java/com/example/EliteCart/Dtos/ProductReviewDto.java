package com.example.EliteCart.Dtos;

public class ProductReviewDto {
    public Long getId() {
        return productid;
    }

    public void setId(Long productid) {
        this.productid = productid;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    private Long productid;
    private Double rating;
    private String comment;


}
