package io.github.notmaikeise.bankingsimulator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.notmaikeise.bankingsimulator.access.application.RegisterUserService;
import io.github.notmaikeise.bankingsimulator.accounts.application.AccountQueryService;
import io.github.notmaikeise.bankingsimulator.accounts.application.DemoCreditService;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest
class DemoCreditPostgresIT {
    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired RegisterUserService registration;
    @Autowired DemoCreditService credits;
    @Autowired AccountQueryService queries;

    @Test
    void simultaneousRetryCreatesOnlyOneEntry() throws Exception {
        UUID owner = registration.register("Test User", UUID.randomUUID() + "@example.test", "password123");
        UUID key = UUID.randomUUID();
        CountDownLatch start = new CountDownLatch(1);
        try (var workers = Executors.newFixedThreadPool(2)) {
            var first = workers.submit(() -> {
                start.await();
                return credits.credit(owner, key, new BigDecimal("10.00"));
            });
            var second = workers.submit(() -> {
                start.await();
                return credits.credit(owner, key, new BigDecimal("10.00"));
            });
            start.countDown();
            assertEquals(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS));
        }
        assertEquals(new BigDecimal("10.00"), queries.forOwner(owner).balance().amount());
        assertEquals(1, queries.statement(owner, 0, 20).size());
    }
}
