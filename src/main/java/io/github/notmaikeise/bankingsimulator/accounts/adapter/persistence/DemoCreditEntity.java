package io.github.notmaikeise.bankingsimulator.accounts.adapter.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "demo_credit_requests", uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "idempotency_key"}))
public class DemoCreditEntity {
    @Id
    UUID id;
    @Column(name = "owner_id", nullable = false)
    UUID ownerId;
    @Column(name = "idempotency_key", nullable = false)
    UUID idempotencyKey;
    @Column(nullable = false, precision = 19, scale = 2)
    BigDecimal amount;
    @Column(name = "entry_id", nullable = false, unique = true)
    UUID entryId;
    @Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
    BigDecimal balanceAfter;
    @Column(name = "occurred_at", nullable = false)
    Instant occurredAt;

    protected DemoCreditEntity() { }

    DemoCreditEntity(UUID ownerId, UUID idempotencyKey, BigDecimal amount, UUID entryId,
                     BigDecimal balanceAfter, Instant occurredAt) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.idempotencyKey = idempotencyKey;
        this.amount = amount;
        this.entryId = entryId;
        this.balanceAfter = balanceAfter;
        this.occurredAt = occurredAt;
    }
}
