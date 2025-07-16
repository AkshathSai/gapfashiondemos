package com.gap.bankapp.fundtransfer;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transfers")
public class FundTransferController {

    private final FundTransferService fundTransferService;

    @PostMapping
    public ResponseEntity<Transaction> transferFunds(@RequestBody TransferRequest request) {
        try {
            Transaction transaction = fundTransferService.transferFunds(
                    request.getFromAccountNumber(),
                    request.getToAccountNumber(),
                    request.getAmount()
            );
            return ResponseEntity.ok(transaction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/history/{accountNumber}")
    public ResponseEntity<List<Transaction>> getTransactionHistory(@PathVariable String accountNumber) {
        List<Transaction> transactions = fundTransferService.getTransactionHistory(accountNumber);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/statement/{accountNumber}")
    public ResponseEntity<List<Transaction>> getMonthlyStatement(
            @PathVariable String accountNumber,
            @RequestParam int year,
            @RequestParam int month) {
        List<Transaction> transactions = fundTransferService.getMonthlyStatement(accountNumber, year, month);
        return ResponseEntity.ok(transactions);
    }
}
