package com.gap.bankapp.registration;

import com.gap.bankapp.account.Account;
import com.gap.bankapp.account.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final AccountRepository accountRepository;

    public Account registerAccount(Account account) {
        // Generate unique 10-digit account number
        String accountNumber = generateUniqueAccountNumber();
        account.setAccountNumber(accountNumber);

        // Validate minimum balance
        if (account.getBalance().compareTo(new BigDecimal("10000")) < 0) {
            throw new IllegalArgumentException("Minimum balance should be 10,000");
        }

        return accountRepository.save(account);
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        Random random = new Random();

        do {
            // Generate 10-digit account number
            long number = 1000000000L + (long)(random.nextDouble() * 9000000000L);
            accountNumber = String.valueOf(number);
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }
}
