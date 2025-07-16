package com.gap.bankapp.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BankDataInitializer implements CommandLineRunner {

    final DemoController demoController;

    @Override
    public void run(String... args) throws Exception {
        demoController.setupDemo();
    }
}
