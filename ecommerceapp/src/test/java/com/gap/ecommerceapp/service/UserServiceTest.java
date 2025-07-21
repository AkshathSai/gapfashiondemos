package com.gap.ecommerceapp.service;

import com.gap.ecommerceapp.exception.ResourceNotFoundException;
import com.gap.ecommerceapp.model.User;
import com.gap.ecommerceapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("password123");
    }

    @Test
    void getAllUsers_Success() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
        verify(userRepository).findAll();
    }

    @Test
    void findById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        verify(userRepository).findById(1L);
    }

    @Test
    void findById_UserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findById(1L);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findById(1L);
    }

    @Test
    void registerUser_Success() {
        // Given
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.registerUser(testUser);

        // Then
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(userRepository).save(testUser);
    }

    @Test
    void registerUser_EmailExists_ThrowsException() {
        // Given
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(testUser));
        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginUser_Success() {
        // Given
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.loginUser("john.doe@example.com", "password123");

        // Then
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        verify(userRepository).findByEmail("john.doe@example.com");
    }

    @Test
    void loginUser_InvalidCredentials_ReturnsEmpty() {
        // Given
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.loginUser("john.doe@example.com", "wrongpassword");

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findByEmail("john.doe@example.com");
    }

    @Test
    void loginUser_UserNotFound_ReturnsEmpty() {
        // Given
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.loginUser("john.doe@example.com", "password123");

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findByEmail("john.doe@example.com");
    }
}
