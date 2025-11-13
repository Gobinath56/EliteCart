package com.example.EliteCart.Service;

import com.example.EliteCart.Entity.Product;
import com.example.EliteCart.Entity.ProductImage;
import com.example.EliteCart.Repository.ProductImageRepository;
import com.example.EliteCart.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class ProductImageService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository imageRepository;

    public ProductImage addImage(Long productId, MultipartFile file) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Example — pretend you uploaded file to Cloudinary or saved locally
        String publicId = UUID.randomUUID().toString();
        String imageUrl = "/uploads/" + file.getOriginalFilename();

        ProductImage image = new ProductImage(publicId, imageUrl, product);
        product.addImage(image);

        return imageRepository.save(image);
    }
}
