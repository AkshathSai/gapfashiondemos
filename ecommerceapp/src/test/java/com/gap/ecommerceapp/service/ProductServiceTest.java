package com.gap.ecommerceapp.service;

import com.gap.ecommerceapp.model.Product;
import com.gap.ecommerceapp.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        testProduct1 = new Product();
        testProduct1.setId(1L);
        testProduct1.setName("T-Shirt");
        testProduct1.setDescription("Cotton T-Shirt");
        testProduct1.setPrice(new BigDecimal("29.99"));
        testProduct1.setCategory("Clothing");
        testProduct1.setStockQuantity(10);

        testProduct2 = new Product();
        testProduct2.setId(2L);
        testProduct2.setName("Jeans");
        testProduct2.setDescription("Blue Jeans");
        testProduct2.setPrice(new BigDecimal("59.99"));
        testProduct2.setCategory("Clothing");
        testProduct2.setStockQuantity(0);
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Arrange
        List<Product> expectedProducts = Arrays.asList(testProduct1, testProduct2);
        when(productRepository.findAll()).thenReturn(expectedProducts);

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertEquals(2, result.size());
        assertEquals(expectedProducts, result);
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getAllProducts_ShouldReturnEmptyList_WhenNoProducts() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getProductById_ShouldReturnProduct_WhenProductExists() {
        // Arrange
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct1));

        // Act
        Optional<Product> result = productService.getProductById(productId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testProduct1, result.get());
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void getProductById_ShouldReturnEmpty_WhenProductNotExists() {
        // Arrange
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act
        Optional<Product> result = productService.getProductById(productId);

        // Assert
        assertFalse(result.isPresent());
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void getProductsByCategory_ShouldReturnProductsInCategory() {
        // Arrange
        String category = "Clothing";
        List<Product> expectedProducts = Arrays.asList(testProduct1, testProduct2);
        when(productRepository.findByCategory(category)).thenReturn(expectedProducts);

        // Act
        List<Product> result = productService.getProductsByCategory(category);

        // Assert
        assertEquals(2, result.size());
        assertEquals(expectedProducts, result);
        verify(productRepository, times(1)).findByCategory(category);
    }

    @Test
    void getProductsByCategory_ShouldReturnEmptyList_WhenCategoryNotExists() {
        // Arrange
        String category = "NonExistent";
        when(productRepository.findByCategory(category)).thenReturn(Arrays.asList());

        // Act
        List<Product> result = productService.getProductsByCategory(category);

        // Assert
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findByCategory(category);
    }

    @Test
    void searchProducts_ShouldReturnMatchingProducts() {
        // Arrange
        String keyword = "shirt";
        List<Product> expectedProducts = Arrays.asList(testProduct1);
        when(productRepository.searchProducts(keyword)).thenReturn(expectedProducts);

        // Act
        List<Product> result = productService.searchProducts(keyword);

        // Assert
        assertEquals(1, result.size());
        assertEquals(expectedProducts, result);
        verify(productRepository, times(1)).searchProducts(keyword);
    }

    @Test
    void searchProducts_ShouldReturnAllProducts_WhenKeywordIsNull() {
        // Arrange
        List<Product> allProducts = Arrays.asList(testProduct1, testProduct2);
        when(productRepository.findAll()).thenReturn(allProducts);

        // Act
        List<Product> result = productService.searchProducts(null);

        // Assert
        assertEquals(2, result.size());
        assertEquals(allProducts, result);
        verify(productRepository, times(1)).findAll();
        verify(productRepository, never()).searchProducts(anyString());
    }

    @Test
    void searchProducts_ShouldReturnAllProducts_WhenKeywordIsEmpty() {
        // Arrange
        List<Product> allProducts = Arrays.asList(testProduct1, testProduct2);
        when(productRepository.findAll()).thenReturn(allProducts);

        // Act
        List<Product> result = productService.searchProducts("");

        // Assert
        assertEquals(2, result.size());
        assertEquals(allProducts, result);
        verify(productRepository, times(1)).findAll();
        verify(productRepository, never()).searchProducts(anyString());
    }

    @Test
    void searchProducts_ShouldReturnAllProducts_WhenKeywordIsWhitespace() {
        // Arrange
        List<Product> allProducts = Arrays.asList(testProduct1, testProduct2);
        when(productRepository.findAll()).thenReturn(allProducts);

        // Act
        List<Product> result = productService.searchProducts("   ");

        // Assert
        assertEquals(2, result.size());
        assertEquals(allProducts, result);
        verify(productRepository, times(1)).findAll();
        verify(productRepository, never()).searchProducts(anyString());
    }

    @Test
    void searchProducts_ShouldTrimKeywordAndSearch() {
        // Arrange
        String keyword = "  shirt  ";
        String trimmedKeyword = "shirt";
        List<Product> expectedProducts = Arrays.asList(testProduct1);
        when(productRepository.searchProducts(trimmedKeyword)).thenReturn(expectedProducts);

        // Act
        List<Product> result = productService.searchProducts(keyword);

        // Assert
        assertEquals(1, result.size());
        verify(productRepository, times(1)).searchProducts(trimmedKeyword);
    }

    @Test
    void getAvailableProducts_ShouldReturnOnlyInStockProducts() {
        // Arrange
        List<Product> availableProducts = Arrays.asList(testProduct1); // Only product1 has stock > 0
        when(productRepository.findByStockQuantityGreaterThan(0)).thenReturn(availableProducts);

        // Act
        List<Product> result = productService.getAvailableProducts();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testProduct1, result.get(0));
        verify(productRepository, times(1)).findByStockQuantityGreaterThan(0);
    }

    @Test
    void getAvailableProducts_ShouldReturnEmptyList_WhenNoProductsInStock() {
        // Arrange
        when(productRepository.findByStockQuantityGreaterThan(0)).thenReturn(Arrays.asList());

        // Act
        List<Product> result = productService.getAvailableProducts();

        // Assert
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findByStockQuantityGreaterThan(0);
    }

    @Test
    void saveProduct_ShouldReturnSavedProduct() {
        // Arrange
        when(productRepository.save(testProduct1)).thenReturn(testProduct1);

        // Act
        Product result = productService.saveProduct(testProduct1);

        // Assert
        assertEquals(testProduct1, result);
        verify(productRepository, times(1)).save(testProduct1);
    }

    @Test
    void updateStock_ShouldUpdateSuccessfully_WhenSufficientStock() {
        // Arrange
        Long productId = 1L;
        Integer quantity = 5;
        testProduct1.setStockQuantity(10);
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct1));
        when(productRepository.save(testProduct1)).thenReturn(testProduct1);

        // Act
        boolean result = productService.updateStock(productId, quantity);

        // Assert
        assertTrue(result);
        assertEquals(5, testProduct1.getStockQuantity()); // 10 - 5 = 5
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(testProduct1);
    }

    @Test
    void updateStock_ShouldReturnFalse_WhenInsufficientStock() {
        // Arrange
        Long productId = 1L;
        Integer quantity = 15; // More than available stock (10)
        testProduct1.setStockQuantity(10);
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct1));

        // Act
        boolean result = productService.updateStock(productId, quantity);

        // Assert
        assertFalse(result);
        assertEquals(10, testProduct1.getStockQuantity()); // Should remain unchanged
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateStock_ShouldReturnFalse_WhenProductNotFound() {
        // Arrange
        Long productId = 999L;
        Integer quantity = 5;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act
        boolean result = productService.updateStock(productId, quantity);

        // Assert
        assertFalse(result);
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateStock_ShouldHandleZeroQuantity() {
        // Arrange
        Long productId = 1L;
        Integer quantity = 0;
        testProduct1.setStockQuantity(10);
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct1));
        when(productRepository.save(testProduct1)).thenReturn(testProduct1);

        // Act
        boolean result = productService.updateStock(productId, quantity);

        // Assert
        assertTrue(result);
        assertEquals(10, testProduct1.getStockQuantity()); // Should remain 10 - 0 = 10
        verify(productRepository, times(1)).save(testProduct1);
    }

    @Test
    void updateStock_ShouldReduceStockToZero_WhenExactQuantity() {
        // Arrange
        Long productId = 1L;
        Integer quantity = 10; // Exact stock amount
        testProduct1.setStockQuantity(10);
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct1));
        when(productRepository.save(testProduct1)).thenReturn(testProduct1);

        // Act
        boolean result = productService.updateStock(productId, quantity);

        // Assert
        assertTrue(result);
        assertEquals(0, testProduct1.getStockQuantity()); // 10 - 10 = 0
        verify(productRepository, times(1)).save(testProduct1);
    }
}
