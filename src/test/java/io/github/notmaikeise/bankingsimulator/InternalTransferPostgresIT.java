package io.github.notmaikeise.bankingsimulator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.notmaikeise.bankingsimulator.access.application.RegisterUserService;
import io.github.notmaikeise.bankingsimulator.accounts.application.AccountQueryService;
import io.github.notmaikeise.bankingsimulator.accounts.application.DemoCreditService;
import io.github.notmaikeise.bankingsimulator.payments.application.TransferService;
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
class InternalTransferPostgresIT {
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
    @Autowired TransferService transfers;

    @Test
    void concurrentRetryDebitsAndCreditsExactlyOnce() throws Exception {
        UUID sender = register();
        UUID receiver = register();
        UUID receiverAccount = queries.forOwner(receiver).id();
        credits.credit(sender, UUID.randomUUID(), new BigDecimal("100.00"));
        UUID key = UUID.randomUUID();
        CountDownLatch start = new CountDownLatch(1);
        try (var workers = Executors.newFixedThreadPool(2)) {
            var one = workers.submit(() -> {
                start.await();
                return transfers.transfer(sender, key, receiverAccount, new BigDecimal("40.00"));
            });
            var two = workers.submit(() -> {
                start.await();
                return transfers.transfer(sender, key, receiverAccount, new BigDecimal("40.00"));
            });
            start.countDown();
            assertEquals(one.get(30, TimeUnit.SECONDS), two.get(30, TimeUnit.SECONDS));
        }
        assertEquals(new BigDecimal("60.00"), queries.forOwner(sender).balance().amount());
        assertEquals(new BigDecimal("40.00"), queries.forOwner(receiver).balance().amount());
        assertEquals(2, queries.statement(sender, 0, 20).size());
        assertEquals(1, queries.statement(receiver, 0, 20).size());
    }

    @Test
    void transfersInOppositeDirectionsKeepTheTotalAndDoNotDeadlock() throws Exception {
        UUID alice = register();
        UUID bob = register();
        UUID aliceAccount = queries.forOwner(alice).id();
        UUID bobAccount = queries.forOwner(bob).id();
        credits.credit(alice, UUID.randomUUID(), new BigDecimal("50.00"));
        credits.credit(bob, UUID.randomUUID(), new BigDecimal("50.00"));
        CountDownLatch start = new CountDownLatch(1);
        try (var workers = Executors.newFixedThreadPool(2)) {
            var one = workers.submit(() -> {
                start.await();
                return transfers.transfer(alice, UUID.randomUUID(), bobAccount, new BigDecimal("10.00"));
            });
            var two = workers.submit(() -> {
                start.await();
                return transfers.transfer(bob, UUID.randomUUID(), aliceAccount, new BigDecimal("20.00"));
            });
            start.countDown();
            one.get(30, TimeUnit.SECONDS);
            two.get(30, TimeUnit.SECONDS);
        }
        assertEquals(new BigDecimal("60.00"), queries.forOwner(alice).balance().amount());
        assertEquals(new BigDecimal("40.00"), queries.forOwner(bob).balance().amount());
        assertEquals(3, queries.statement(alice, 0, 20).size());
        assertEquals(3, queries.statement(bob, 0, 20).size());
    }

    private UUID register() {
        return registration.register("Test User", UUID.randomUUID() + "@example.test", "password123");
    }
}
