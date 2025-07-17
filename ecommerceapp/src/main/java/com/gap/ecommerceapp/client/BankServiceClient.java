package com.gap.ecommerceapp.client;

import com.gap.ecommerceapp.dto.Account;
import com.gap.ecommerceapp.dto.Transaction;
import com.gap.ecommerceapp.dto.TransferRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "BankApp")
public interface BankServiceClient {

    @GetMapping("/api/accounts/{accountNumber}")
    ResponseEntity<Account> getAccountByNumber(@PathVariable String accountNumber);

    @GetMapping("/api/accounts/balance/{accountNumber}")
    ResponseEntity<String> getBalance(@PathVariable String accountNumber);

    @PostMapping("/api/transfers")
    ResponseEntity<Transaction> transferFunds(@RequestBody TransferRequest request);
}
