package com.example.EliteCart.Repository;

import com.example.EliteCart.Entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByCategoryContainingIgnoreCaseAndPriceBetweenAndRatingsGreaterThanEqual(
            String category, Double min, Double max, Double ratings, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndPriceBetween(
            String name, Double min, Double max, Pageable pageable);

    List<Product> findTop5ByOrderByRatingsDesc();

    List<Product> findBySeller(String seller);
}
