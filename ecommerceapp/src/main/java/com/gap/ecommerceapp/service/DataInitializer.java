package com.gap.ecommerceapp.service;

import com.gap.ecommerceapp.model.Product;
import com.gap.ecommerceapp.model.User;
import com.gap.ecommerceapp.repository.ProductRepository;
import com.gap.ecommerceapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() == 0) {
            initializeProducts();
            log.info("Sample products initialized successfully! Total products: {}", productRepository.count());
        } else {
            log.info("Products already exist. Skipping initialization.");
        }

        if (userRepository.count() == 0) {
            initializeUsers();
            log.info("Sample users initialized successfully! Total users: {}", userRepository.count());
        } else {
            log.info("Users already exist. Skipping initialization.");
        }
    }

    private void initializeProducts() {
        List<Product> products = Arrays.asList(
            // Men's Clothing - Tops
            createProduct("Classic White T-Shirt", "Men's Tops",
                "Premium 100% cotton classic white t-shirt. Perfect for everyday wear with superior comfort and durability.",
                new BigDecimal("19.99"), 150),

            createProduct("Polo Shirt Navy", "Men's Tops",
                "Classic navy polo shirt made from pique cotton. Ideal for casual and smart-casual occasions.",
                new BigDecimal("39.99"), 80),

            createProduct("Oxford Button-Down Shirt", "Men's Tops",
                "Traditional oxford cotton button-down shirt in light blue. Perfect for office or casual wear.",
                new BigDecimal("49.99"), 60),

            createProduct("Crewneck Sweatshirt", "Men's Tops",
                "Comfortable gray crewneck sweatshirt made from cotton blend fleece. Perfect for layering.",
                new BigDecimal("34.99"), 70),

            // Men's Clothing - Bottoms
            createProduct("Slim Fit Jeans", "Men's Bottoms",
                "Dark wash slim fit jeans with stretch denim for comfort. Classic five-pocket styling.",
                new BigDecimal("59.99"), 90),

            createProduct("Khaki Chinos", "Men's Bottoms",
                "Versatile khaki chino pants in a modern fit. Perfect for work or weekend wear.",
                new BigDecimal("44.99"), 75),

            createProduct("Athletic Shorts", "Men's Bottoms",
                "Lightweight athletic shorts with moisture-wicking fabric and side pockets.",
                new BigDecimal("24.99"), 100),

            // Men's Outerwear
            createProduct("Denim Jacket", "Men's Outerwear",
                "Classic blue denim jacket with traditional styling. Made from premium cotton denim.",
                new BigDecimal("79.99"), 45),

            createProduct("Bomber Jacket", "Men's Outerwear",
                "Modern bomber jacket in olive green. Lightweight with ribbed cuffs and hem.",
                new BigDecimal("89.99"), 35),

            // Women's Clothing - Tops
            createProduct("Silk Blouse", "Women's Tops",
                "Elegant silk blouse in cream color. Perfect for professional settings with a feminine touch.",
                new BigDecimal("69.99"), 50),

            createProduct("Cashmere Sweater", "Women's Tops",
                "Luxurious cashmere sweater in soft pink. Ultra-soft and warm for cooler weather.",
                new BigDecimal("129.99"), 25),

            createProduct("Cotton Tank Top", "Women's Tops",
                "Basic cotton tank top in white. Essential wardrobe piece for layering or wearing alone.",
                new BigDecimal("14.99"), 120),

            // Women's Clothing - Bottoms
            createProduct("High-Waisted Jeans", "Women's Bottoms",
                "High-waisted skinny jeans in dark indigo. Flattering fit with stretch for comfort.",
                new BigDecimal("64.99"), 85),

            createProduct("Pleated Skirt", "Women's Bottoms",
                "A-line pleated skirt in navy. Knee-length with classic styling perfect for office wear.",
                new BigDecimal("39.99"), 40),

            createProduct("Yoga Leggings", "Women's Bottoms",
                "High-performance yoga leggings with four-way stretch. Perfect for workouts or casual wear.",
                new BigDecimal("34.99"), 110),

            // Women's Dresses
            createProduct("Little Black Dress", "Women's Dresses",
                "Classic little black dress with a modern twist. Perfect for cocktail parties or dinner dates.",
                new BigDecimal("89.99"), 30),

            createProduct("Floral Maxi Dress", "Women's Dresses",
                "Flowing maxi dress with beautiful floral print. Perfect for summer occasions.",
                new BigDecimal("79.99"), 35),

            createProduct("Wrap Dress", "Women's Dresses",
                "Flattering wrap dress in solid burgundy. Versatile piece that transitions from day to night.",
                new BigDecimal("54.99"), 45),

            // Shoes
            createProduct("White Leather Sneakers", "Shoes",
                "Premium white leather sneakers with classic court shoe design. Comfortable for all-day wear.",
                new BigDecimal("99.99"), 60),

            createProduct("Running Shoes", "Shoes",
                "High-performance running shoes with advanced cushioning and breathable mesh upper.",
                new BigDecimal("119.99"), 55),

            createProduct("Ankle Boots", "Shoes",
                "Stylish ankle boots in black leather. Perfect for adding edge to any outfit.",
                new BigDecimal("149.99"), 40),

            createProduct("Loafers", "Shoes",
                "Classic leather loafers in brown. Comfortable slip-on style perfect for business casual.",
                new BigDecimal("89.99"), 50),

            // Accessories
            createProduct("Leather Belt", "Accessories",
                "Premium leather belt in black with silver buckle. Classic accessory for any wardrobe.",
                new BigDecimal("29.99"), 80),

            createProduct("Canvas Tote Bag", "Accessories",
                "Durable canvas tote bag in natural color. Perfect for shopping, work, or beach days.",
                new BigDecimal("24.99"), 70),

            createProduct("Wool Scarf", "Accessories",
                "Soft wool scarf in gray. Cozy accessory perfect for cold weather styling.",
                new BigDecimal("39.99"), 60),

            createProduct("Sunglasses", "Accessories",
                "Classic aviator sunglasses with UV protection. Timeless style that never goes out of fashion.",
                new BigDecimal("49.99"), 90),

            // Active Wear
            createProduct("Sports Bra", "Activewear",
                "Medium support sports bra with moisture-wicking fabric. Perfect for yoga and light workouts.",
                new BigDecimal("24.99"), 85),

            createProduct("Athletic Hoodie", "Activewear",
                "Performance hoodie with moisture-wicking technology. Great for pre and post-workout.",
                new BigDecimal("54.99"), 65)
        );

        productRepository.saveAll(products);
    }

    private void initializeUsers() {
        List<User> users = Arrays.asList(
            createUser("John Doe", "john.doe@gap.com", "password123", "2349885777"),
            createUser("Jane Smith", "jane.smith@gap.com", "password123", "4352602652"),
            createUser("Emily Davis", "emily.davis@gap.com", "password123", "ACC004567890"),
            createUser("Sarah Brown", "sarah.brown@gap.com", "password123", "ACC005678901")
        );

        userRepository.saveAll(users);
    }

    private Product createProduct(String name, String category, String description, BigDecimal price, Integer stockQuantity) {
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        return product;
    }

    private User createUser(String name, String email, String password, String bankAccountNumber) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password); // This was missing and causing the NULL constraint violation
        return user;
    }
}
