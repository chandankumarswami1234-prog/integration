package com.socialapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:social_media_test}",
        "app.jwt.secret=test-secret-for-context-load-only-32-characters-min"
})
class SocialAppApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the Spring context (security, JPA, JWT beans) wires up correctly.
    }
}
