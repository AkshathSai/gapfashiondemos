package com.gap.ecommerceapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gap.ecommerceapp.model.User;
import com.gap.ecommerceapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setBankAccountNumber("1234567890");
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() throws Exception {
        // Arrange
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");

        List<User> users = Arrays.asList(testUser, user2);
        when(userService.getAllUsers()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].email").value("test@example.com"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].username").value("user2"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getAllUsers_ShouldReturnEmptyList_WhenNoUsers() throws Exception {
        // Arrange
        when(userService.getAllUsers()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() throws Exception {
        // Arrange
        Long userId = 1L;
        when(userService.findById(userId)).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.bankAccountNumber").value("1234567890"));

        verify(userService, times(1)).findById(userId);
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        // Arrange
        Long userId = 999L;
        when(userService.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findById(userId);
    }

    @Test
    void registerUser_ShouldRegisterSuccessfully() throws Exception {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("newpassword");
        newUser.setEmail("newuser@example.com");
        newUser.setBankAccountNumber("0987654321");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("newuser");
        savedUser.setPassword("newpassword");
        savedUser.setEmail("newuser@example.com");
        savedUser.setBankAccountNumber("0987654321");

        when(userService.registerUser(any(User.class))).thenReturn(savedUser);

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.bankAccountNumber").value("0987654321"));

        verify(userService, times(1)).registerUser(any(User.class));
    }

    @Test
    void registerUser_ShouldReturnBadRequest_WhenUsernameExists() throws Exception {
        // Arrange
        User newUser = new User();
        newUser.setUsername("existinguser");
        newUser.setPassword("password");
        newUser.setEmail("new@example.com");

        when(userService.registerUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Username already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).registerUser(any(User.class));
    }

    @Test
    void registerUser_ShouldReturnBadRequest_WhenEmailExists() throws Exception {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password");
        newUser.setEmail("existing@example.com");

        when(userService.registerUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Email already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).registerUser(any(User.class));
    }

    @Test
    void registerUser_ShouldReturnBadRequest_WhenInvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"invalid\" \"json\""))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(User.class));
    }

    @Test
    void loginUser_ShouldReturnUser_WhenCredentialsValid() throws Exception {
        // Arrange
        String username = "testuser";
        String password = "password123";
        when(userService.loginUser(username, password)).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .param("username", username)
                .param("password", password)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).loginUser(username, password);
    }

    @Test
    void loginUser_ShouldReturnUnauthorized_WhenCredentialsInvalid() throws Exception {
        // Arrange
        String username = "testuser";
        String password = "wrongpassword";
        when(userService.loginUser(username, password)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .param("username", username)
                .param("password", password)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(userService, times(1)).loginUser(username, password);
    }

    @Test
    void loginUser_ShouldReturnUnauthorized_WhenUserNotFound() throws Exception {
        // Arrange
        String username = "nonexistent";
        String password = "password";
        when(userService.loginUser(username, password)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .param("username", username)
                .param("password", password)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(userService, times(1)).loginUser(username, password);
    }

    @Test
    void loginUser_ShouldHandleMissingParameters() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).loginUser(anyString(), anyString());
    }

    @Test
    void loginUser_ShouldHandleEmptyUsername() throws Exception {
        // Arrange
        String username = "";
        String password = "password";
        when(userService.loginUser(username, password)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .param("username", username)
                .param("password", password)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(userService, times(1)).loginUser(username, password);
    }

    @Test
    void loginUser_ShouldHandleEmptyPassword() throws Exception {
        // Arrange
        String username = "testuser";
        String password = "";
        when(userService.loginUser(username, password)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .param("username", username)
                .param("password", password)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(userService, times(1)).loginUser(username, password);
    }

    @Test
    void registerUser_ShouldHandleNullBankAccountNumber() throws Exception {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password");
        newUser.setEmail("new@example.com");
        newUser.setBankAccountNumber(null);

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("newuser");
        savedUser.setBankAccountNumber(null);

        when(userService.registerUser(any(User.class))).thenReturn(savedUser);

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bankAccountNumber").doesNotExist());

        verify(userService, times(1)).registerUser(any(User.class));
    }

    @Test
    void registerUser_ShouldHandleEmptyBankAccountNumber() throws Exception {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password");
        newUser.setEmail("new@example.com");
        newUser.setBankAccountNumber("");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("newuser");
        savedUser.setBankAccountNumber("");

        when(userService.registerUser(any(User.class))).thenReturn(savedUser);

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bankAccountNumber").value(""));

        verify(userService, times(1)).registerUser(any(User.class));
    }

    @Test
    void getUserById_ShouldHandleNullId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/null")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).findById(any());
    }

    @Test
    void registerUser_ShouldHandleSpecialCharactersInName() throws Exception {
        // Arrange
        User newUser = new User();
        newUser.setUsername("user.name");
        newUser.setPassword("password");
        newUser.setEmail("user@example.com");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("user.name");

        when(userService.registerUser(any(User.class))).thenReturn(savedUser);

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("José"))
                .andExpect(jsonPath("$.lastName").value("O'Connor-Smith"));

        verify(userService, times(1)).registerUser(any(User.class));
    }
}
