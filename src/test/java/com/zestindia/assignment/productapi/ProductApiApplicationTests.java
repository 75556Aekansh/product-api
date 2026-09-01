package com.zestindia.assignment.productapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "application.security.jwt.secret=VGhpc0lzQVN0cm9uZ0Rldk9ubHlTZWNyZXRLZXlGb3JKV1RUMjAyNg==",
        "application.security.jwt.access-token-expiration-ms=900000",
        "application.security.jwt.refresh-token-expiration-ms=604800000"
})
@ActiveProfiles("test")
class ProductApiApplicationTests {

    @Test
    void contextLoads() {
    }
}