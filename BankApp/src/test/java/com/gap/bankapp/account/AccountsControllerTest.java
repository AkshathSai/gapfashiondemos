package com.gap.bankapp.account;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountsController.class)
class AccountsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setAccountNumber("1234567890");
        testAccount.setName("John Doe");
        testAccount.setBalance(new BigDecimal("50000"));
        testAccount.setEmail("john@example.com");
        testAccount.setPhone("1234567890");
    }

    @Test
    void getAllAccounts_ShouldReturnAllAccounts() throws Exception {
        // Arrange
        Account account2 = new Account();
        account2.setAccountNumber("0987654321");
        account2.setName("Jane Smith");
        account2.setBalance(new BigDecimal("25000"));

        List<Account> accounts = Arrays.asList(testAccount, account2);
        when(accountService.getAllAccounts()).thenReturn(accounts);

        // Act & Assert
        mockMvc.perform(get("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].accountNumber").value("1234567890"))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].balance").value(50000))
                .andExpect(jsonPath("$[1].accountNumber").value("0987654321"))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"))
                .andExpect(jsonPath("$[1].balance").value(25000));

        verify(accountService, times(1)).getAllAccounts();
    }

    @Test
    void getAllAccounts_ShouldReturnEmptyList_WhenNoAccountsExist() throws Exception {
        // Arrange
        when(accountService.getAllAccounts()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(accountService, times(1)).getAllAccounts();
    }

    @Test
    void getAccountByNumber_ShouldReturnAccount_WhenAccountExists() throws Exception {
        // Arrange
        String accountNumber = "1234567890";
        when(accountService.getAccountByAccountNumber(accountNumber)).thenReturn(testAccount);

        // Act & Assert
        mockMvc.perform(get("/api/accounts/{accountNumber}", accountNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.balance").value(50000))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.phone").value("1234567890"));

        verify(accountService, times(1)).getAccountByAccountNumber(accountNumber);
    }

    @Test
    void getAccountByNumber_ShouldReturnNotFound_WhenAccountNotExists() throws Exception {
        // Arrange
        String accountNumber = "9999999999";
        when(accountService.getAccountByAccountNumber(accountNumber))
                .thenThrow(new IllegalArgumentException("Account not found"));

        // Act & Assert
        mockMvc.perform(get("/api/accounts/{accountNumber}", accountNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).getAccountByAccountNumber(accountNumber);
    }

    @Test
    void getBalance_ShouldReturnBalance_WhenAccountExists() throws Exception {
        // Arrange
        String accountNumber = "1234567890";
        when(accountService.getAccountByAccountNumber(accountNumber)).thenReturn(testAccount);

        // Act & Assert
        mockMvc.perform(get("/api/accounts/balance/{accountNumber}", accountNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Current balance: 50000"));

        verify(accountService, times(1)).getAccountByAccountNumber(accountNumber);
    }

    @Test
    void getBalance_ShouldReturnNotFound_WhenAccountNotExists() throws Exception {
        // Arrange
        String accountNumber = "9999999999";
        when(accountService.getAccountByAccountNumber(accountNumber))
                .thenThrow(new IllegalArgumentException("Account not found"));

        // Act & Assert
        mockMvc.perform(get("/api/accounts/balance/{accountNumber}", accountNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).getAccountByAccountNumber(accountNumber);
    }

    @Test
    void getAccountByNumber_ShouldHandleSpecialCharactersInAccountNumber() throws Exception {
        // Arrange
        String accountNumber = "123-456-7890";
        when(accountService.getAccountByAccountNumber(accountNumber)).thenReturn(testAccount);

        // Act & Assert
        mockMvc.perform(get("/api/accounts/{accountNumber}", accountNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"));

        verify(accountService, times(1)).getAccountByAccountNumber(accountNumber);
    }

    @Test
    void getBalance_ShouldHandleZeroBalance() throws Exception {
        // Arrange
        String accountNumber = "1234567890";
        testAccount.setBalance(BigDecimal.ZERO);
        when(accountService.getAccountByAccountNumber(accountNumber)).thenReturn(testAccount);

        // Act & Assert
        mockMvc.perform(get("/api/accounts/balance/{accountNumber}", accountNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Current balance: 0"));

        verify(accountService, times(1)).getAccountByAccountNumber(accountNumber);
    }
}
