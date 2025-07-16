package com.gap.bankapp.account;

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
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

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
    void getAllAccounts_ShouldReturnAllAccounts() {
        // Arrange
        Account account2 = new Account();
        account2.setAccountNumber("0987654321");
        account2.setName("Jane Smith");
        account2.setBalance(new BigDecimal("25000"));

        List<Account> expectedAccounts = Arrays.asList(testAccount, account2);
        when(accountRepository.findAll()).thenReturn(expectedAccounts);

        // Act
        List<Account> actualAccounts = accountService.getAllAccounts();

        // Assert
        assertEquals(2, actualAccounts.size());
        assertEquals(expectedAccounts, actualAccounts);
        verify(accountRepository, times(1)).findAll();
    }

    @Test
    void getAllAccounts_ShouldReturnEmptyList_WhenNoAccountsExist() {
        // Arrange
        when(accountRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Account> actualAccounts = accountService.getAllAccounts();

        // Assert
        assertTrue(actualAccounts.isEmpty());
        verify(accountRepository, times(1)).findAll();
    }

    @Test
    void getAccountByAccountNumber_ShouldReturnAccount_WhenAccountExists() {
        // Arrange
        String accountNumber = "1234567890";
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(testAccount));

        // Act
        Account actualAccount = accountService.getAccountByAccountNumber(accountNumber);

        // Assert
        assertNotNull(actualAccount);
        assertEquals(testAccount.getAccountNumber(), actualAccount.getAccountNumber());
        assertEquals(testAccount.getName(), actualAccount.getName());
        assertEquals(testAccount.getBalance(), actualAccount.getBalance());
        verify(accountRepository, times(1)).findByAccountNumber(accountNumber);
    }

    @Test
    void getAccountByAccountNumber_ShouldThrowException_WhenAccountNotFound() {
        // Arrange
        String accountNumber = "9999999999";
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.getAccountByAccountNumber(accountNumber)
        );

        assertEquals("Account not found", exception.getMessage());
        verify(accountRepository, times(1)).findByAccountNumber(accountNumber);
    }

    @Test
    void getAccountByAccountNumber_ShouldHandleNullAccountNumber() {
        // Arrange
        when(accountRepository.findByAccountNumber(null)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.getAccountByAccountNumber(null)
        );

        assertEquals("Account not found", exception.getMessage());
        verify(accountRepository, times(1)).findByAccountNumber(null);
    }

    @Test
    void getAccountByAccountNumber_ShouldHandleEmptyAccountNumber() {
        // Arrange
        String emptyAccountNumber = "";
        when(accountRepository.findByAccountNumber(emptyAccountNumber)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.getAccountByAccountNumber(emptyAccountNumber)
        );

        assertEquals("Account not found", exception.getMessage());
        verify(accountRepository, times(1)).findByAccountNumber(emptyAccountNumber);
    }
}
