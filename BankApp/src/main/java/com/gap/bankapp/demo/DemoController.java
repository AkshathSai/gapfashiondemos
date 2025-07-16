package com.gap.bankapp.demo;

import com.gap.bankapp.account.Account;
import com.gap.bankapp.account.AccountRepository;
import com.gap.bankapp.registration.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/demo")
public class DemoController {

    private static final String GAP_ECOMMERCE_BANK_ACCOUNT = "1349885778";
    final AccountRepository accountRepository;
    final RegistrationService registrationService;

    @PostMapping("/setup-demo")
    public ResponseEntity<String> setupDemo() {
        try {
            // Create demo accounts
            Account account1 = new Account("John Doe", "30", "john@example.com", "1234567890", "2349885777", new BigDecimal("50000"));
            Account account2 = new Account("Jane Smith", "25", "jane@example.com", "0987654321", "4352602652", new BigDecimal("25000"));
            // GAP ECOMMERCE BANK ACCOUNT
            Account account3 = new Account("GAP INC", "56", "gap@inc.com", "0687654321", GAP_ECOMMERCE_BANK_ACCOUNT, new BigDecimal("75000"));

            Account savedAccount1 = accountRepository.save(account1);
            Account savedAccount2 = accountRepository.save(account2);
            Account savedAccount3 = accountRepository.save(account3);

            return ResponseEntity.ok("Demo accounts created:\n" +
                    "Account 1: " + savedAccount1.getAccountNumber() + " (Balance: " + savedAccount1.getBalance() + ")\n" +
                    "Account 2: " + savedAccount2.getAccountNumber() + " (Balance: " + savedAccount2.getBalance() + ")\n" +
                    "GAP ECOMMERCE BANK ACCOUNT: " + savedAccount3.getAccountNumber() + " (Balance: " + savedAccount3.getBalance() + ")");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error setting up demo: " + e.getMessage());
        }
    }
}


