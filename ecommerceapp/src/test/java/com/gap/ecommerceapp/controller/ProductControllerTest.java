package com.gap.ecommerceapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gap.ecommerceapp.model.Product;
import com.gap.ecommerceapp.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

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
        testProduct2.setStockQuantity(5);
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() throws Exception {
        // Arrange
        List<Product> products = Arrays.asList(testProduct1, testProduct2);
        when(productService.getAllProducts()).thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("T-Shirt"))
                .andExpect(jsonPath("$[0].price").value(29.99))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Jeans"))
                .andExpect(jsonPath("$[1].price").value(59.99));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void getAllProducts_ShouldReturnEmptyList_WhenNoProducts() throws Exception {
        // Arrange
        when(productService.getAllProducts()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void getProductById_ShouldReturnProduct_WhenProductExists() throws Exception {
        // Arrange
        Long productId = 1L;
        when(productService.getProductById(productId)).thenReturn(Optional.of(testProduct1));

        // Act & Assert
        mockMvc.perform(get("/api/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("T-Shirt"))
                .andExpect(jsonPath("$.description").value("Cotton T-Shirt"))
                .andExpect(jsonPath("$.price").value(29.99))
                .andExpect(jsonPath("$.category").value("Clothing"))
                .andExpect(jsonPath("$.stockQuantity").value(10));

        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    void getProductById_ShouldReturnNotFound_WhenProductNotExists() throws Exception {
        // Arrange
        Long productId = 999L;
        when(productService.getProductById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    void searchProducts_ShouldReturnMatchingProducts() throws Exception {
        // Arrange
        String keyword = "shirt";
        List<Product> products = Arrays.asList(testProduct1);
        when(productService.searchProducts(keyword)).thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("keyword", keyword)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("T-Shirt"));

        verify(productService, times(1)).searchProducts(keyword);
    }

    @Test
    void searchProducts_ShouldReturnAllProducts_WhenNoKeyword() throws Exception {
        // Arrange
        List<Product> products = Arrays.asList(testProduct1, testProduct2);
        when(productService.searchProducts(null)).thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2));

        verify(productService, times(1)).searchProducts(null);
    }

    @Test
    void searchProducts_ShouldReturnEmptyList_WhenNoMatches() throws Exception {
        // Arrange
        String keyword = "nonexistent";
        when(productService.searchProducts(keyword)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("keyword", keyword)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(productService, times(1)).searchProducts(keyword);
    }

    @Test
    void getProductsByCategory_ShouldReturnProductsInCategory() throws Exception {
        // Arrange
        String category = "Clothing";
        List<Product> products = Arrays.asList(testProduct1, testProduct2);
        when(productService.getProductsByCategory(category)).thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/api/products/category/{category}", category)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].category").value("Clothing"))
                .andExpect(jsonPath("$[1].category").value("Clothing"));

        verify(productService, times(1)).getProductsByCategory(category);
    }

    @Test
    void getProductsByCategory_ShouldReturnEmptyList_WhenCategoryNotExists() throws Exception {
        // Arrange
        String category = "NonExistent";
        when(productService.getProductsByCategory(category)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/products/category/{category}", category)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(productService, times(1)).getProductsByCategory(category);
    }

    @Test
    void getAvailableProducts_ShouldReturnOnlyInStockProducts() throws Exception {
        // Arrange
        List<Product> availableProducts = Arrays.asList(testProduct1, testProduct2);
        when(productService.getAvailableProducts()).thenReturn(availableProducts);

        // Act & Assert
        mockMvc.perform(get("/api/products/available")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2));

        verify(productService, times(1)).getAvailableProducts();
    }

    @Test
    void getAvailableProducts_ShouldReturnEmptyList_WhenNoProductsInStock() throws Exception {
        // Arrange
        when(productService.getAvailableProducts()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/products/available")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(productService, times(1)).getAvailableProducts();
    }

    @Test
    void createProduct_ShouldCreateProductSuccessfully() throws Exception {
        // Arrange
        Product newProduct = new Product();
        newProduct.setName("New Product");
        newProduct.setDescription("New Description");
        newProduct.setPrice(new BigDecimal("19.99"));
        newProduct.setCategory("Electronics");
        newProduct.setStockQuantity(20);

        Product savedProduct = new Product();
        savedProduct.setId(3L);
        savedProduct.setName("New Product");
        savedProduct.setDescription("New Description");
        savedProduct.setPrice(new BigDecimal("19.99"));
        savedProduct.setCategory("Electronics");
        savedProduct.setStockQuantity(20);

        when(productService.saveProduct(any(Product.class))).thenReturn(savedProduct);

        // Act & Assert
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.price").value(19.99))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.stockQuantity").value(20));

        verify(productService, times(1)).saveProduct(any(Product.class));
    }

    @Test
    void createProduct_ShouldReturnBadRequest_WhenInvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"invalid\" \"json\""))
                .andExpect(status().isBadRequest());

        verify(productService, never()).saveProduct(any(Product.class));
    }

    @Test
    void searchProducts_ShouldHandleEmptyKeyword() throws Exception {
        // Arrange
        List<Product> products = Arrays.asList(testProduct1, testProduct2);
        when(productService.searchProducts("")).thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("keyword", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2));

        verify(productService, times(1)).searchProducts("");
    }

    @Test
    void searchProducts_ShouldHandleWhitespaceKeyword() throws Exception {
        // Arrange
        String keyword = "   ";
        List<Product> products = Arrays.asList(testProduct1, testProduct2);
        when(productService.searchProducts(keyword)).thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("keyword", keyword)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2));

        verify(productService, times(1)).searchProducts(keyword);
    }

    @Test
    void getProductsByCategory_ShouldHandleSpecialCharactersInCategory() throws Exception {
        // Arrange
        String category = "Men's & Women's";
        when(productService.getProductsByCategory(category)).thenReturn(Arrays.asList(testProduct1));

        // Act & Assert
        mockMvc.perform(get("/api/products/category/{category}", category)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1));

        verify(productService, times(1)).getProductsByCategory(category);
    }

    @Test
    void createProduct_ShouldHandleZeroPrice() throws Exception {
        // Arrange
        Product newProduct = new Product();
        newProduct.setName("Free Product");
        newProduct.setPrice(BigDecimal.ZERO);
        newProduct.setCategory("Free");
        newProduct.setStockQuantity(100);

        Product savedProduct = new Product();
        savedProduct.setId(4L);
        savedProduct.setName("Free Product");
        savedProduct.setPrice(BigDecimal.ZERO);
        savedProduct.setCategory("Free");
        savedProduct.setStockQuantity(100);

        when(productService.saveProduct(any(Product.class))).thenReturn(savedProduct);

        // Act & Assert
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.price").value(0));

        verify(productService, times(1)).saveProduct(any(Product.class));
    }

    @Test
    void createProduct_ShouldHandleZeroStock() throws Exception {
        // Arrange
        Product newProduct = new Product();
        newProduct.setName("Out of Stock Product");
        newProduct.setPrice(new BigDecimal("29.99"));
        newProduct.setCategory("Limited");
        newProduct.setStockQuantity(0);

        Product savedProduct = new Product();
        savedProduct.setId(5L);
        savedProduct.setName("Out of Stock Product");
        savedProduct.setPrice(new BigDecimal("29.99"));
        savedProduct.setCategory("Limited");
        savedProduct.setStockQuantity(0);

        when(productService.saveProduct(any(Product.class))).thenReturn(savedProduct);

        // Act & Assert
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.stockQuantity").value(0));

        verify(productService, times(1)).saveProduct(any(Product.class));
    }
}
