package com.gap.bankapp.account;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer uId;

    private String name;
    private String age;
    private String email;
    private String phone;
    private String accountNumber;
    private BigDecimal balance;
    private LocalDateTime createdAt;

    public Account(String name, String age, String email, String phone, String accountNumber, BigDecimal balance) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.phone = phone;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.createdAt = LocalDateTime.now();
    }
}
