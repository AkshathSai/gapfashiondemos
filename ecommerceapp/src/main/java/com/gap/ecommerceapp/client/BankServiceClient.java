package com.gap.ecommerceapp.client;

import com.gap.ecommerceapp.dto.Transaction;
import com.gap.ecommerceapp.dto.TransferRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//@FeignClient(name = "BankApp")
@FeignClient(name = "BankApp", url = "${bank.service.url}")
public interface BankServiceClient {

    @PostMapping("/api/transfers")
    ResponseEntity<Transaction> transferFunds(@RequestBody TransferRequest request);
}
