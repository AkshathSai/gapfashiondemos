package com.gap.ecommerceapp.client;

import com.gap.ecommerceapp.dto.Transaction;
import com.gap.ecommerceapp.dto.TransferRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankServiceClientTest {

    @Mock
    private BankServiceClient bankServiceClient;

    private TransferRequest transferRequest;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transferRequest = new TransferRequest();
        transferRequest.setFromAccountNumber("1234567890");
        transferRequest.setToAccountNumber("0987654321");
        transferRequest.setAmount(new BigDecimal("100.00"));

        transaction = new Transaction();
        transaction.setId(123L);
        transaction.setFromAccountNumber("1234567890");
        transaction.setToAccountNumber("0987654321");
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTransactionType("TRANSFER");
        transaction.setDescription("Test transfer");
        transaction.setBalanceAfterTransaction(new BigDecimal("900.00"));
    }

    @Test
    void transferFunds_Success() {
        // Given
        ResponseEntity<Transaction> expectedResponse = new ResponseEntity<>(transaction, HttpStatus.OK);
        when(bankServiceClient.transferFunds(any(TransferRequest.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<Transaction> response = bankServiceClient.transferFunds(transferRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(123L, response.getBody().getId());
        assertEquals(new BigDecimal("100.00"), response.getBody().getAmount());
        verify(bankServiceClient).transferFunds(transferRequest);
    }

    @Test
    void transferFunds_Failure() {
        // Given
        ResponseEntity<Transaction> expectedResponse = new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        when(bankServiceClient.transferFunds(any(TransferRequest.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<Transaction> response = bankServiceClient.transferFunds(transferRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(bankServiceClient).transferFunds(transferRequest);
    }

    @Test
    void transferFunds_InternalServerError() {
        // Given
        ResponseEntity<Transaction> expectedResponse = new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        when(bankServiceClient.transferFunds(any(TransferRequest.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<Transaction> response = bankServiceClient.transferFunds(transferRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(bankServiceClient).transferFunds(transferRequest);
    }
}
