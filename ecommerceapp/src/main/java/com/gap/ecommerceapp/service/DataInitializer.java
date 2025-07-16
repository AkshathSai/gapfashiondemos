package com.gap.ecommerceapp.service;

import com.gap.ecommerceapp.model.Product;
import com.gap.ecommerceapp.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() == 0) {
            initializeProducts();
        }
    }

    private void initializeProducts() {
        // Fashion products
        productRepository.save(new Product(null, "Classic Denim Jacket",
                "Timeless denim jacket perfect for casual wear",
                new BigDecimal("79.99"), "Outerwear", 50,
                "https://images.unsplash.com/photo-1551698618-1dfe5d97d256", null));

        productRepository.save(new Product(null, "Cotton T-Shirt",
                "100% organic cotton comfortable t-shirt",
                new BigDecimal("24.99"), "Tops", 100,
                "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab", null));

        productRepository.save(new Product(null, "Skinny Jeans",
                "Perfect fit skinny jeans in dark wash",
                new BigDecimal("59.99"), "Bottoms", 75,
                "https://images.unsplash.com/photo-1542272604-787c3835535d", null));

        productRepository.save(new Product(null, "Summer Dress",
                "Flowy summer dress in floral print",
                new BigDecimal("89.99"), "Dresses", 30,
                "https://images.unsplash.com/photo-1515372039744-b8f02a3ae446", null));

        productRepository.save(new Product(null, "Sneakers",
                "Comfortable white sneakers for everyday wear",
                new BigDecimal("99.99"), "Shoes", 40,
                "https://images.unsplash.com/photo-1549298916-b41d501d3772", null));

        productRepository.save(new Product(null, "Hoodie",
                "Warm and cozy pullover hoodie",
                new BigDecimal("54.99"), "Outerwear", 60,
                "https://images.unsplash.com/photo-1556821840-3a63f95609a7", null));

        productRepository.save(new Product(null, "Chino Pants",
                "Smart casual chino pants in khaki",
                new BigDecimal("49.99"), "Bottoms", 45,
                "https://images.unsplash.com/photo-1473966968600-fa801b869a1a", null));

        productRepository.save(new Product(null, "Leather Jacket",
                "Premium leather jacket for a bold look",
                new BigDecimal("199.99"), "Outerwear", 20,
                "https://images.unsplash.com/photo-1551028719-00167b16eac5", null));

        System.out.println("Sample products initialized successfully!");
    }
}
