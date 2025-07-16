package com.gap.bankapp.fundtransfer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByFromAccountNumberOrToAccountNumberOrderByTransactionDateDesc(
            String fromAccountNumber, String toAccountNumber);

    @Query("SELECT t FROM Transaction t WHERE (t.fromAccountNumber = :accountNumber OR t.toAccountNumber = :accountNumber) " +
           "AND YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findTransactionsByAccountNumberAndYearAndMonth(
            @Param("accountNumber") String accountNumber,
            @Param("year") int year,
            @Param("month") int month);
}
