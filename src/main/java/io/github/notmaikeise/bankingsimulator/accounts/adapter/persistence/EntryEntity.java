package io.github.notmaikeise.bankingsimulator.accounts.adapter.persistence;

import io.github.notmaikeise.bankingsimulator.accounts.domain.AccountEntry.Kind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_entries")
public class EntryEntity {
    @Id
    UUID id;
    @Column(name = "account_id", nullable = false)
    UUID accountId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    Kind kind;
    @Column(nullable = false, precision = 19, scale = 2)
    BigDecimal amount;
    @Column(name = "reference_id")
    UUID referenceId;
    @Column(name = "occurred_at", nullable = false)
    Instant occurredAt;

    protected EntryEntity() { }

    EntryEntity(UUID id, UUID accountId, Kind kind, BigDecimal amount, UUID referenceId, Instant occurredAt) {
        this.id = id;
        this.accountId = accountId;
        this.kind = kind;
        this.amount = amount;
        this.referenceId = referenceId;
        this.occurredAt = occurredAt;
    }
}
