package com.gap.bankapp.registration;

import com.gap.bankapp.account.Account;
import com.gap.bankapp.account.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/api/accounts")
    public ResponseEntity<Account> registerAccount(@Valid @RequestBody Account account) {
        try {
            Account savedAccount = registrationService.registerAccount(account);
            return ResponseEntity.created(
                    URI.create("/api/accounts/" + savedAccount.getAccountNumber()))
                    .body(savedAccount);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
