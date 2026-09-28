package io.github.notmaikeise.bankingsimulator.payments.adapter.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "internal_transfers", uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "idempotency_key"}))
public class TransferEntity {
    @Id UUID id;
    @Column(name = "owner_id", nullable = false) UUID ownerId;
    @Column(name = "idempotency_key", nullable = false) UUID idempotencyKey;
    @Column(name = "source_account_id", nullable = false) UUID sourceAccountId;
    @Column(name = "destination_account_id", nullable = false) UUID destinationAccountId;
    @Column(nullable = false, precision = 19, scale = 2) BigDecimal amount;
    @Column(name = "source_entry_id", nullable = false, unique = true) UUID sourceEntryId;
    @Column(name = "destination_entry_id", nullable = false, unique = true) UUID destinationEntryId;
    @Column(name = "source_balance_after", nullable = false, precision = 19, scale = 2) BigDecimal sourceBalanceAfter;
    @Column(name = "occurred_at", nullable = false) Instant occurredAt;

    protected TransferEntity() { }

    TransferEntity(UUID id, UUID ownerId, UUID idempotencyKey, UUID sourceAccountId,
                   UUID destinationAccountId, BigDecimal amount, UUID sourceEntryId,
                   UUID destinationEntryId, BigDecimal sourceBalanceAfter, Instant occurredAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.idempotencyKey = idempotencyKey;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.sourceEntryId = sourceEntryId;
        this.destinationEntryId = destinationEntryId;
        this.sourceBalanceAfter = sourceBalanceAfter;
        this.occurredAt = occurredAt;
    }
}
