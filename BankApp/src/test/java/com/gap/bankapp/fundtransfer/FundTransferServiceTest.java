package com.gap.bankapp.fundtransfer;

import com.gap.bankapp.account.Account;
import com.gap.bankapp.account.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundTransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private FundTransferService fundTransferService;

    private Account fromAccount;
    private Account toAccount;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        fromAccount = new Account();
        fromAccount.setAccountNumber("1234567890");
        fromAccount.setName("John Doe");
        fromAccount.setBalance(new BigDecimal("50000"));

        toAccount = new Account();
        toAccount.setAccountNumber("0987654321");
        toAccount.setName("Jane Smith");
        toAccount.setBalance(new BigDecimal("25000"));

        testTransaction = new Transaction(
                "1234567890",
                "0987654321",
                new BigDecimal("5000"),
                Transaction.TransactionType.TRANSFER,
                "Fund transfer from 1234567890 to 0987654321"
        );
        testTransaction.setId(1L);
        testTransaction.setTransactionDate(LocalDateTime.now());
    }

    @Test
    void transferFunds_ShouldTransferSuccessfully_WhenValidRequest() {
        // Arrange
        BigDecimal transferAmount = new BigDecimal("5000");
        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("0987654321")).thenReturn(Optional.of(toAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act
        Transaction result = fundTransferService.transferFunds("1234567890", "0987654321", transferAmount);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("45000"), fromAccount.getBalance());
        assertEquals(new BigDecimal("30000"), toAccount.getBalance());
        verify(accountRepository, times(1)).save(fromAccount);
        verify(accountRepository, times(1)).save(toAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void transferFunds_ShouldThrowException_WhenFromAccountNotFound() {
        // Arrange
        BigDecimal transferAmount = new BigDecimal("5000");
        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> fundTransferService.transferFunds("1234567890", "0987654321", transferAmount)
        );

        assertEquals("From account not found", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transferFunds_ShouldThrowException_WhenToAccountNotFound() {
        // Arrange
        BigDecimal transferAmount = new BigDecimal("5000");
        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("0987654321")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> fundTransferService.transferFunds("1234567890", "0987654321", transferAmount)
        );

        assertEquals("To account not found", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transferFunds_ShouldThrowException_WhenInsufficientBalance() {
        // Arrange
        BigDecimal transferAmount = new BigDecimal("60000");
        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("0987654321")).thenReturn(Optional.of(toAccount));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> fundTransferService.transferFunds("1234567890", "0987654321", transferAmount)
        );

        assertEquals("Insufficient balance", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transferFunds_ShouldThrowException_WhenMinimumBalanceViolated() {
        // Arrange
        BigDecimal transferAmount = new BigDecimal("45000"); // Would leave only 5000, below minimum 10000
        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("0987654321")).thenReturn(Optional.of(toAccount));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> fundTransferService.transferFunds("1234567890", "0987654321", transferAmount)
        );

        assertEquals("Cannot transfer: minimum balance of 10,000 must be maintained", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transferFunds_ShouldAllowTransfer_WhenExactlyMinimumBalanceRemains() {
        // Arrange
        BigDecimal transferAmount = new BigDecimal("40000"); // Would leave exactly 10000
        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("0987654321")).thenReturn(Optional.of(toAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act
        Transaction result = fundTransferService.transferFunds("1234567890", "0987654321", transferAmount);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("10000"), fromAccount.getBalance());
        assertEquals(new BigDecimal("65000"), toAccount.getBalance());
        verify(accountRepository, times(1)).save(fromAccount);
        verify(accountRepository, times(1)).save(toAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void transferFunds_ShouldHandleZeroAmount() {
        // Arrange
        BigDecimal transferAmount = BigDecimal.ZERO;
        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("0987654321")).thenReturn(Optional.of(toAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act
        Transaction result = fundTransferService.transferFunds("1234567890", "0987654321", transferAmount);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("50000"), fromAccount.getBalance()); // No change
        assertEquals(new BigDecimal("25000"), toAccount.getBalance()); // No change
    }

    @Test
    void getTransactionHistory_ShouldReturnTransactions() {
        // Arrange
        String accountNumber = "1234567890";
        List<Transaction> expectedTransactions = Arrays.asList(testTransaction);
        when(transactionRepository.findByFromAccountNumberOrToAccountNumberOrderByTransactionDateDesc(
                accountNumber, accountNumber)).thenReturn(expectedTransactions);

        // Act
        List<Transaction> result = fundTransferService.getTransactionHistory(accountNumber);

        // Assert
        assertEquals(1, result.size());
        assertEquals(expectedTransactions, result);
        verify(transactionRepository, times(1))
                .findByFromAccountNumberOrToAccountNumberOrderByTransactionDateDesc(accountNumber, accountNumber);
    }

    @Test
    void getTransactionHistory_ShouldReturnEmptyList_WhenNoTransactions() {
        // Arrange
        String accountNumber = "1234567890";
        when(transactionRepository.findByFromAccountNumberOrToAccountNumberOrderByTransactionDateDesc(
                accountNumber, accountNumber)).thenReturn(Arrays.asList());

        // Act
        List<Transaction> result = fundTransferService.getTransactionHistory(accountNumber);

        // Assert
        assertTrue(result.isEmpty());
        verify(transactionRepository, times(1))
                .findByFromAccountNumberOrToAccountNumberOrderByTransactionDateDesc(accountNumber, accountNumber);
    }

    @Test
    void getMonthlyStatement_ShouldReturnTransactions() {
        // Arrange
        String accountNumber = "1234567890";
        int year = 2024;
        int month = 12;
        List<Transaction> expectedTransactions = Arrays.asList(testTransaction);
        when(transactionRepository.findTransactionsByAccountNumberAndYearAndMonth(
                accountNumber, year, month)).thenReturn(expectedTransactions);

        // Act
        List<Transaction> result = fundTransferService.getMonthlyStatement(accountNumber, year, month);

        // Assert
        assertEquals(1, result.size());
        assertEquals(expectedTransactions, result);
        verify(transactionRepository, times(1))
                .findTransactionsByAccountNumberAndYearAndMonth(accountNumber, year, month);
    }

    @Test
    void getMonthlyStatement_ShouldReturnEmptyList_WhenNoTransactionsInMonth() {
        // Arrange
        String accountNumber = "1234567890";
        int year = 2024;
        int month = 1;
        when(transactionRepository.findTransactionsByAccountNumberAndYearAndMonth(
                accountNumber, year, month)).thenReturn(Arrays.asList());

        // Act
        List<Transaction> result = fundTransferService.getMonthlyStatement(accountNumber, year, month);

        // Assert
        assertTrue(result.isEmpty());
        verify(transactionRepository, times(1))
                .findTransactionsByAccountNumberAndYearAndMonth(accountNumber, year, month);
    }

    @Test
    void transferFunds_ShouldHandleSameAccount() {
        // Arrange
        BigDecimal transferAmount = new BigDecimal("5000");
        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(fromAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act
        Transaction result = fundTransferService.transferFunds("1234567890", "1234567890", transferAmount);

        // Assert
        assertNotNull(result);
        // Balance should remain the same for same account transfer
        assertEquals(new BigDecimal("50000"), fromAccount.getBalance());
        verify(accountRepository, times(2)).save(fromAccount); // Saved twice (as from and to)
    }
}
