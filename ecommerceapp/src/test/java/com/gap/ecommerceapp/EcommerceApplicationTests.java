package com.gap.ecommerceapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class EcommerceApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures that the Spring application context loads successfully
        // and all beans are properly configured
    }

    @Test
    void mainMethodTest() {
        // Test that the main method can be called without exceptions
        EcommerceApplication.main(new String[]{});
    }
}
