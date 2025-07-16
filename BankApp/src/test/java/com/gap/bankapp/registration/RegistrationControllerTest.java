package com.gap.bankapp.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gap.bankapp.account.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegistrationController.class)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationService registrationService;

    @Autowired
    private ObjectMapper objectMapper;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setName("John Doe");
        testAccount.setBalance(new BigDecimal("15000"));
        testAccount.setEmail("john@example.com");
        testAccount.setPhone("1234567890");
    }

    @Test
    void registerAccount_ShouldReturnCreated_WhenValidAccount() throws Exception {
        // Arrange
        Account savedAccount = new Account();
        savedAccount.setAccountNumber("1234567890");
        savedAccount.setName("John Doe");
        savedAccount.setBalance(new BigDecimal("15000"));
        savedAccount.setEmail("john@example.com");
        savedAccount.setPhone("1234567890");

        when(registrationService.registerAccount(any(Account.class))).thenReturn(savedAccount);

        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAccount)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/accounts/1234567890"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.balance").value(15000))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.phone").value("1234567890"));

        verify(registrationService, times(1)).registerAccount(any(Account.class));
    }

    @Test
    void registerAccount_ShouldReturnBadRequest_WhenMinimumBalanceViolated() throws Exception {
        // Arrange
        when(registrationService.registerAccount(any(Account.class)))
                .thenThrow(new IllegalArgumentException("Minimum balance should be 10,000"));

        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAccount)))
                .andExpect(status().isBadRequest());

        verify(registrationService, times(1)).registerAccount(any(Account.class));
    }

    @Test
    void registerAccount_ShouldReturnBadRequest_WhenInvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"invalid\" \"json\""))
                .andExpect(status().isBadRequest());

        verify(registrationService, never()).registerAccount(any(Account.class));
    }

    @Test
    void registerAccount_ShouldHandleZeroBalance() throws Exception {
        // Arrange
        testAccount.setBalance(BigDecimal.ZERO);
        when(registrationService.registerAccount(any(Account.class)))
                .thenThrow(new IllegalArgumentException("Minimum balance should be 10,000"));

        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAccount)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerAccount_ShouldHandleMinimumBalance() throws Exception {
        // Arrange
        testAccount.setBalance(new BigDecimal("10000"));
        Account savedAccount = new Account();
        savedAccount.setAccountNumber("1234567890");
        savedAccount.setName("John Doe");
        savedAccount.setBalance(new BigDecimal("10000"));
        savedAccount.setEmail("john@example.com");
        savedAccount.setPhone("1234567890");

        when(registrationService.registerAccount(any(Account.class))).thenReturn(savedAccount);

        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAccount)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balance").value(10000));

        verify(registrationService, times(1)).registerAccount(any(Account.class));
    }

    @Test
    void registerAccount_ShouldHandleLargeBalance() throws Exception {
        // Arrange
        testAccount.setBalance(new BigDecimal("1000000"));
        Account savedAccount = new Account();
        savedAccount.setAccountNumber("1234567890");
        savedAccount.setName("John Doe");
        savedAccount.setBalance(new BigDecimal("1000000"));
        savedAccount.setEmail("john@example.com");
        savedAccount.setPhone("1234567890");

        when(registrationService.registerAccount(any(Account.class))).thenReturn(savedAccount);

        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAccount)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balance").value(1000000));

        verify(registrationService, times(1)).registerAccount(any(Account.class));
    }

    @Test
    void registerAccount_ShouldHandleEmptyPhone() throws Exception {
        // Arrange
        testAccount.setPhone("");
        Account savedAccount = new Account();
        savedAccount.setAccountNumber("1234567890");
        savedAccount.setName("John Doe");
        savedAccount.setBalance(new BigDecimal("15000"));
        savedAccount.setEmail("john@example.com");
        savedAccount.setPhone("");

        when(registrationService.registerAccount(any(Account.class))).thenReturn(savedAccount);

        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAccount)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.phone").value(""));

        verify(registrationService, times(1)).registerAccount(any(Account.class));
    }

    @Test
    void registerAccount_ShouldHandleSpecialCharactersInName() throws Exception {
        // Arrange
        testAccount.setName("John O'Doe-Smith Jr.");
        Account savedAccount = new Account();
        savedAccount.setAccountNumber("1234567890");
        savedAccount.setName("John O'Doe-Smith Jr.");
        savedAccount.setBalance(new BigDecimal("15000"));
        savedAccount.setEmail("john@example.com");
        savedAccount.setPhone("1234567890");

        when(registrationService.registerAccount(any(Account.class))).thenReturn(savedAccount);

        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAccount)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John O'Doe-Smith Jr."));

        verify(registrationService, times(1)).registerAccount(any(Account.class));
    }
}
