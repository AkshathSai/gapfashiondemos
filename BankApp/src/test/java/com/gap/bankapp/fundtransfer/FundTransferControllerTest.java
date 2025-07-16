package com.gap.bankapp.fundtransfer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FundTransferController.class)
class FundTransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FundTransferService fundTransferService;

    @Autowired
    private ObjectMapper objectMapper;

    private Transaction testTransaction;
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        testTransaction = new Transaction(
                "1234567890",
                "0987654321",
                new BigDecimal("5000"),
                Transaction.TransactionType.TRANSFER,
                "Fund transfer from 1234567890 to 0987654321"
        );
        testTransaction.setId(1L);
        testTransaction.setTransactionDate(LocalDateTime.now());

        transferRequest = new TransferRequest();
        transferRequest.setFromAccountNumber("1234567890");
        transferRequest.setToAccountNumber("0987654321");
        transferRequest.setAmount(new BigDecimal("5000"));
    }

    @Test
    void transferFunds_ShouldReturnTransaction_WhenValidRequest() throws Exception {
        // Arrange
        when(fundTransferService.transferFunds(
                transferRequest.getFromAccountNumber(),
                transferRequest.getToAccountNumber(),
                transferRequest.getAmount()
        )).thenReturn(testTransaction);

        // Act & Assert
        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fromAccountNumber").value("1234567890"))
                .andExpect(jsonPath("$.toAccountNumber").value("0987654321"))
                .andExpect(jsonPath("$.amount").value(5000))
                .andExpect(jsonPath("$.type").value("TRANSFER"));

        verify(fundTransferService, times(1)).transferFunds(
                "1234567890", "0987654321", new BigDecimal("5000"));
    }

    @Test
    void transferFunds_ShouldReturnBadRequest_WhenServiceThrowsException() throws Exception {
        // Arrange
        when(fundTransferService.transferFunds(
                transferRequest.getFromAccountNumber(),
                transferRequest.getToAccountNumber(),
                transferRequest.getAmount()
        )).thenThrow(new IllegalArgumentException("Insufficient balance"));

        // Act & Assert
        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest());

        verify(fundTransferService, times(1)).transferFunds(
                "1234567890", "0987654321", new BigDecimal("5000"));
    }

    @Test
    void transferFunds_ShouldReturnBadRequest_WhenInvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"invalid\" \"json\""))
                .andExpect(status().isBadRequest());

        verify(fundTransferService, never()).transferFunds(anyString(), anyString(), any(BigDecimal.class));
    }

    @Test
    void transferFunds_ShouldHandleNullAmount() throws Exception {
        // Arrange
        transferRequest.setAmount(null);
        when(fundTransferService.transferFunds(
                transferRequest.getFromAccountNumber(),
                transferRequest.getToAccountNumber(),
                transferRequest.getAmount()
        )).thenThrow(new IllegalArgumentException("Amount cannot be null"));

        // Act & Assert
        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transferFunds_ShouldHandleZeroAmount() throws Exception {
        // Arrange
        transferRequest.setAmount(BigDecimal.ZERO);
        when(fundTransferService.transferFunds(
                transferRequest.getFromAccountNumber(),
                transferRequest.getToAccountNumber(),
                transferRequest.getAmount()
        )).thenReturn(testTransaction);

        // Act & Assert
        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk());

        verify(fundTransferService, times(1)).transferFunds(
                "1234567890", "0987654321", BigDecimal.ZERO);
    }

    @Test
    void getTransactionHistory_ShouldReturnTransactions() throws Exception {
        // Arrange
        String accountNumber = "1234567890";
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(fundTransferService.getTransactionHistory(accountNumber)).thenReturn(transactions);

        // Act & Assert
        mockMvc.perform(get("/api/transfers/history/{accountNumber}", accountNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fromAccountNumber").value("1234567890"))
                .andExpect(jsonPath("$[0].toAccountNumber").value("0987654321"));

        verify(fundTransferService, times(1)).getTransactionHistory(accountNumber);
    }

    @Test
    void getTransactionHistory_ShouldReturnEmptyList_WhenNoTransactions() throws Exception {
        // Arrange
        String accountNumber = "1234567890";
        when(fundTransferService.getTransactionHistory(accountNumber)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/transfers/history/{accountNumber}", accountNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(fundTransferService, times(1)).getTransactionHistory(accountNumber);
    }

    @Test
    void getMonthlyStatement_ShouldReturnTransactions() throws Exception {
        // Arrange
        String accountNumber = "1234567890";
        int year = 2024;
        int month = 12;
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(fundTransferService.getMonthlyStatement(accountNumber, year, month)).thenReturn(transactions);

        // Act & Assert
        mockMvc.perform(get("/api/transfers/statement/{accountNumber}", accountNumber)
                .param("year", String.valueOf(year))
                .param("month", String.valueOf(month))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(fundTransferService, times(1)).getMonthlyStatement(accountNumber, year, month);
    }

    @Test
    void getMonthlyStatement_ShouldReturnEmptyList_WhenNoTransactionsInMonth() throws Exception {
        // Arrange
        String accountNumber = "1234567890";
        int year = 2024;
        int month = 1;
        when(fundTransferService.getMonthlyStatement(accountNumber, year, month)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/transfers/statement/{accountNumber}", accountNumber)
                .param("year", String.valueOf(year))
                .param("month", String.valueOf(month))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(fundTransferService, times(1)).getMonthlyStatement(accountNumber, year, month);
    }

    @Test
    void getMonthlyStatement_ShouldHandleInvalidYearMonth() throws Exception {
        // Arrange
        String accountNumber = "1234567890";

        // Act & Assert
        mockMvc.perform(get("/api/transfers/statement/{accountNumber}", accountNumber)
                .param("year", "invalid")
                .param("month", "invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(fundTransferService, never()).getMonthlyStatement(anyString(), anyInt(), anyInt());
    }

    @Test
    void transferFunds_ShouldHandleMinimumBalanceViolation() throws Exception {
        // Arrange
        when(fundTransferService.transferFunds(
                transferRequest.getFromAccountNumber(),
                transferRequest.getToAccountNumber(),
                transferRequest.getAmount()
        )).thenThrow(new IllegalArgumentException("Cannot transfer: minimum balance of 10,000 must be maintained"));

        // Act & Assert
        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest());

        verify(fundTransferService, times(1)).transferFunds(
                "1234567890", "0987654321", new BigDecimal("5000"));
    }

    @Test
    void transferFunds_ShouldHandleAccountNotFound() throws Exception {
        // Arrange
        when(fundTransferService.transferFunds(
                transferRequest.getFromAccountNumber(),
                transferRequest.getToAccountNumber(),
                transferRequest.getAmount()
        )).thenThrow(new IllegalArgumentException("From account not found"));

        // Act & Assert
        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest());

        verify(fundTransferService, times(1)).transferFunds(
                "1234567890", "0987654321", new BigDecimal("5000"));
    }
}
