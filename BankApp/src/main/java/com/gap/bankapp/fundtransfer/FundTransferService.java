package com.gap.bankapp.fundtransfer;

import com.gap.bankapp.account.Account;
import com.gap.bankapp.account.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FundTransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction transferFunds(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        // Validate accounts exist
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("From account not found"));

        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("To account not found"));

        // Validate sufficient balance
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        // Check minimum balance after transfer (should remain at least 10,000)
        BigDecimal remainingBalance = fromAccount.getBalance().subtract(amount);
        if (remainingBalance.compareTo(new BigDecimal("10000")) < 0) {
            throw new IllegalArgumentException("Cannot transfer: minimum balance of 10,000 must be maintained");
        }

        // Perform transfer
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // Record transaction
        Transaction transaction = new Transaction(
                fromAccountNumber,
                toAccountNumber,
                amount,
                Transaction.TransactionType.TRANSFER,
                "Fund transfer from " + fromAccountNumber + " to " + toAccountNumber
        );

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactionHistory(String accountNumber) {
        return transactionRepository.findByFromAccountNumberOrToAccountNumberOrderByTransactionDateDesc(
                accountNumber, accountNumber);
    }

    public List<Transaction> getMonthlyStatement(String accountNumber, int year, int month) {
        return transactionRepository.findTransactionsByAccountNumberAndYearAndMonth(
                accountNumber, year, month);
    }
}
