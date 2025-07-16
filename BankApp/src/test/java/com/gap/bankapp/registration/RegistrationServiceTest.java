package com.gap.bankapp.registration;

import com.gap.bankapp.account.Account;
import com.gap.bankapp.account.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private RegistrationService registrationService;

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
    void registerAccount_ShouldRegisterSuccessfully_WhenValidAccount() {
        // Arrange
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setUId(1);
            return account;
        });

        // Act
        Account result = registrationService.registerAccount(testAccount);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAccountNumber());
        assertEquals(10, result.getAccountNumber().length()); // Should be 10 digits
        assertEquals("John Doe", result.getName());
        assertEquals(new BigDecimal("15000"), result.getBalance());
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    void registerAccount_ShouldThrowException_WhenBalanceBelowMinimum() {
        // Arrange
        testAccount.setBalance(new BigDecimal("5000")); // Below minimum 10,000

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> registrationService.registerAccount(testAccount)
        );

        assertEquals("Minimum balance should be 10,000", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void registerAccount_ShouldThrowException_WhenBalanceIsExactlyMinimum() {
        // Arrange
        testAccount.setBalance(new BigDecimal("10000")); // Exactly minimum
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // Act
        Account result = registrationService.registerAccount(testAccount);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("10000"), result.getBalance());
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    void registerAccount_ShouldThrowException_WhenBalanceIsZero() {
        // Arrange
        testAccount.setBalance(BigDecimal.ZERO);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> registrationService.registerAccount(testAccount)
        );

        assertEquals("Minimum balance should be 10,000", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void registerAccount_ShouldThrowException_WhenBalanceIsNull() {
        // Arrange
        testAccount.setBalance(null);

        // Act & Assert
        assertThrows(
            NullPointerException.class,
            () -> registrationService.registerAccount(testAccount)
        );

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void registerAccount_ShouldGenerateUniqueAccountNumber_WhenDuplicateExists() {
        // Arrange
        when(accountRepository.existsByAccountNumber(anyString()))
                .thenReturn(true)  // First generated number exists
                .thenReturn(false); // Second generated number is unique
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // Act
        Account result = registrationService.registerAccount(testAccount);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAccountNumber());
        assertEquals(10, result.getAccountNumber().length());
        verify(accountRepository, atLeast(2)).existsByAccountNumber(anyString());
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    void registerAccount_ShouldHandleMultipleDuplicateAttempts() {
        // Arrange
        when(accountRepository.existsByAccountNumber(anyString()))
                .thenReturn(true)   // First attempt - duplicate
                .thenReturn(true)   // Second attempt - duplicate
                .thenReturn(true)   // Third attempt - duplicate
                .thenReturn(false); // Fourth attempt - unique
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // Act
        Account result = registrationService.registerAccount(testAccount);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAccountNumber());
        verify(accountRepository, times(4)).existsByAccountNumber(anyString());
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    void registerAccount_ShouldGenerateValidAccountNumber() {
        // Arrange
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            return account;
        });

        // Act
        Account result = registrationService.registerAccount(testAccount);

        // Assert
        String accountNumber = result.getAccountNumber();
        assertNotNull(accountNumber);
        assertEquals(10, accountNumber.length());
        assertTrue(accountNumber.matches("\\d{10}")); // Should be exactly 10 digits
        assertTrue(Long.parseLong(accountNumber) >= 1000000000L); // Should be >= 10^9
        assertTrue(Long.parseLong(accountNumber) <= 9999999999L); // Should be <= 10^10 - 1
    }

    @Test
    void registerAccount_ShouldHandleLargeBalance() {
        // Arrange
        testAccount.setBalance(new BigDecimal("1000000000")); // Very large balance
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // Act
        Account result = registrationService.registerAccount(testAccount);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("1000000000"), result.getBalance());
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    void registerAccount_ShouldPreserveAccountDetails() {
        // Arrange
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Account result = registrationService.registerAccount(testAccount);

        // Assert
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("1234567890", result.getPhone());
        assertEquals(new BigDecimal("15000"), result.getBalance());
        assertNotNull(result.getAccountNumber());
    }
}
