package com.example.EliteCart.Service;

import com.example.EliteCart.Dtos.ProductDto;
import com.example.EliteCart.Entity.Product;
import com.example.EliteCart.Entity.User;
import com.example.EliteCart.Enum.Role;
import com.example.EliteCart.Exception.ResourceNotFoundException;
import com.example.EliteCart.Repository.ProductRepository;
import com.example.EliteCart.Repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository,
                          UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setCategory(product.getCategory());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setRatings(product.getRatings());
        dto.setSeller(product.getSeller());
        dto.setStock(product.getStock());
        dto.setNumOfReviews(product.getNumOfReviews());

        if (product.getReviews() != null && !product.getReviews().isEmpty()) {
            dto.setReviews(product.getReviews());
        }

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            List<String> urls = product.getImages()
                    .stream()
                    .map(img -> img.getUrl())
                    .toList();
            dto.setImageUrls(urls);
        }

        if (product.getDiscount() != null && product.getDiscount() > 0) {
            dto.setDiscount(product.getDiscount());
            dto.setDiscountedPrice(product.getDiscountedPrice());
        }

        return dto;
    }

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

    public ProductDto addProduct(Product product) {
        // ✅ Set authenticated user as seller
        String currentEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        product.setSeller(currentUser.getUsername());
        Product saved = productRepository.save(product);
        return convertToDto(saved);
    }

    public ProductDto updateProduct(Long id, Product updatedProduct) {
        // ✅ Get authenticated user
        String currentEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        // ✅ Verify seller owns this product (unless admin)
        if (currentUser.getRole() != Role.ROLE_ADMIN &&
                !product.getSeller().equalsIgnoreCase(currentUser.getUsername())) {
            throw new RuntimeException("Access denied: You can only update your own products");
        }

        if (updatedProduct.getName() != null) product.setName(updatedProduct.getName());
        if (updatedProduct.getPrice() != null) product.setPrice(updatedProduct.getPrice());
        if (updatedProduct.getCategory() != null) product.setCategory(updatedProduct.getCategory());
        if (updatedProduct.getDescription() != null) product.setDescription(updatedProduct.getDescription());
        if (updatedProduct.getRatings() != null) product.setRatings(updatedProduct.getRatings());
        if (updatedProduct.getStock() != null) product.setStock(updatedProduct.getStock());

        Product saved = productRepository.save(product);
        return convertToDto(saved);
    }

    public void removeProduct(Long id) {
        // ✅ Get authenticated user
        String currentEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        // ✅ Verify seller owns this product (unless admin)
        if (currentUser.getRole() != Role.ROLE_ADMIN &&
                !product.getSeller().equalsIgnoreCase(currentUser.getUsername())) {
            throw new RuntimeException("Access denied: You can only delete your own products");
        }

        productRepository.deleteById(id);
    }

    public Optional<ProductDto> findId(Long id) {
        return productRepository.findById(id)
                .map(this::convertToDto);
    }

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

    public List<ProductDto> getTopRatedProducts() {
        return productRepository.findTop5ByOrderByRatingsDesc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}