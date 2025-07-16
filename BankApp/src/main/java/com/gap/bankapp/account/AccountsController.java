package com.gap.bankapp.account;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AccountsController {

    final AccountService accountService;

    @GetMapping("/api/accounts")
    public ResponseEntity<List<Account>> getAllAccounts() {
        List<Account> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/api/accounts/{accountNumber}")
    public ResponseEntity<Account> getAccountByNumber(@PathVariable String accountNumber) {
        try {
            Account account = accountService.getAccountByAccountNumber(accountNumber);
            return ResponseEntity.ok(account);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/api/accounts/balance/{accountNumber}")
    public ResponseEntity<String> getBalance(@PathVariable String accountNumber) {
        try {
            Account account = accountService.getAccountByAccountNumber(accountNumber);
            return ResponseEntity.ok("Current balance: " + account.getBalance());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
