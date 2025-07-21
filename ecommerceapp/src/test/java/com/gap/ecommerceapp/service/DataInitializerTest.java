package com.gap.ecommerceapp.service;

import com.gap.ecommerceapp.repository.ProductRepository;
import com.gap.ecommerceapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        // Remove unnecessary default stubbing - let each test set up its own mocks
    }

    @Test
    void run_InitializesDataWhenRepositoriesAreEmpty() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(productRepository.count()).thenReturn(0L);

        // When
        dataInitializer.run();

        // Then
        verify(userRepository, times(2)).count(); // Called twice: condition check + logging
        verify(productRepository, times(2)).count(); // Called twice: condition check + logging
        verify(userRepository).saveAll(anyList());
        verify(productRepository).saveAll(anyList());
    }

    @Test
    void run_SkipsInitializationWhenDataExists() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(5L);
        when(productRepository.count()).thenReturn(10L);

        // When
        dataInitializer.run();

        // Then
        verify(userRepository, times(1)).count(); // Only called once: condition check
        verify(productRepository, times(1)).count(); // Only called once: condition check
        verify(userRepository, never()).saveAll(anyList());
        verify(productRepository, never()).saveAll(anyList());
    }

    @Test
    void run_InitializesUsersOnlyWhenProductsExist() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(productRepository.count()).thenReturn(10L);

        // When
        dataInitializer.run();

        // Then
        verify(userRepository, times(2)).count(); // Called twice: condition check + logging
        verify(productRepository, times(1)).count(); // Only called once: condition check
        verify(userRepository).saveAll(anyList());
        verify(productRepository, never()).saveAll(anyList());
    }

    @Test
    void run_InitializesProductsOnlyWhenUsersExist() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(5L);
        when(productRepository.count()).thenReturn(0L);

        // When
        dataInitializer.run();

        // Then
        verify(userRepository, times(1)).count(); // Only called once: condition check
        verify(productRepository, times(2)).count(); // Called twice: condition check + logging
        verify(userRepository, never()).saveAll(anyList());
        verify(productRepository).saveAll(anyList());
    }

    @Test
    void implementsCommandLineRunner() {
        // Then
        assertTrue(dataInitializer instanceof CommandLineRunner);
    }
}
