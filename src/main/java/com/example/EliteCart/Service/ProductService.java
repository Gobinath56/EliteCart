package com.example.EliteCart.Service;

import com.example.EliteCart.Dtos.ProductDto;
import com.example.EliteCart.Entity.Product;
import com.example.EliteCart.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * ✅ Convert Product → ProductDto
     */
    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setCategory(product.getCategory());
        dto.setDescripation(product.getDescripation());
        dto.setPrice(product.getPrice());
        dto.setRatings(product.getRatings());
        dto.setSeller(product.getSeller());
        dto.setStock(product.getStock());
        dto.setNumOfReviews(product.getNumOfReviews());

        // ✅ Convert Reviews
        if (product.getReviwes() != null && !product.getReviwes().isEmpty()) {
            dto.setReviwes(product.getReviwes());
        }

        // ✅ Convert Images → Only URL list
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            List<String> urls = product.getImages()
                    .stream()
                    .map(img -> img.getUrl()) // only extract URLs
                    .toList();
            dto.setImageUrls(urls);
        }

        // ✅ Include discount only if > 0
        if (product.getDiscount() != null && product.getDiscount() > 0) {
            dto.setDiscount(product.getDiscount());
            dto.setDiscountedPrice(product.getDiscountedPrice());
        }

        return dto;
    }


    /**
     * ✅ Get all products with pagination + sorting
     */
    public Page<ProductDto> getAllProductsSorted(int page, int size, String sortBy, String order) {
        Sort sort = order.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.findAll(pageable);

        List<ProductDto> dtos = products.getContent()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, products.getTotalElements());
    }

    /**
     * ✅ Get products by category + price + rating filter
     */
    public Page<ProductDto> getByCategory(String category, Double min, Double max, Double ratings, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository
                .findByCategoryContainingIgnoreCaseAndPriceBetweenAndRatingsGreaterThanEqual(
                        category, min, max, ratings, pageable);

        List<ProductDto> dtos = products.getContent()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, products.getTotalElements());
    }

    /**
     * ✅ Add a new product
     */
    public ProductDto addProduct(Product product) {
        Product saved = productRepository.save(product);
        return convertToDto(saved);
    }

    /**
     * ✅ Update a product by ID
     */
    public ProductDto updateProduct(Long id, Product updatedProduct) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));

        if (updatedProduct.getName() != null) product.setName(updatedProduct.getName());
        if (updatedProduct.getPrice() != null) product.setPrice(updatedProduct.getPrice());
        if (updatedProduct.getCategory() != null) product.setCategory(updatedProduct.getCategory());
        if (updatedProduct.getDescripation() != null) product.setDescripation(updatedProduct.getDescripation());
        if (updatedProduct.getRatings() != null) product.setRatings(updatedProduct.getRatings());
        if (updatedProduct.getStock() != null) product.setStock(updatedProduct.getStock());

        Product saved = productRepository.save(product);
        return convertToDto(saved);
    }

    /**
     * ✅ Remove product by ID
     */
    public void removeProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with ID: " + id);
        }
        productRepository.deleteById(id);
    }

    /**
     * ✅ Find product by ID
     */
    public Optional<ProductDto> findId(Long id) {
        return productRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * ✅ Search by product name + price filter
     */
    public Page<ProductDto> searchByName(String name, Double min, Double max, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByNameContainingIgnoreCaseAndPriceBetween(
                name, min, max, pageable);

        List<ProductDto> dtos = products.getContent()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, products.getTotalElements());
    }

    /**
     * ✅ (Optional) Get top-rated or best-selling products
     */
    public List<ProductDto> getTopRatedProducts() {
        return productRepository.findTop5ByOrderByRatingsDesc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}
