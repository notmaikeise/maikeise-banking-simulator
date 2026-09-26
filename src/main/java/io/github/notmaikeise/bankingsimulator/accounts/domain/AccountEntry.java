package io.github.notmaikeise.bankingsimulator.accounts.domain;

import io.github.notmaikeise.bankingsimulator.shared.domain.Money;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record AccountEntry(UUID id, UUID accountId, Kind kind, Money amount, Instant occurredAt) {
    public enum Kind { DEMO_CREDIT }

    public AccountEntry {
        Objects.requireNonNull(id);
        Objects.requireNonNull(accountId);
        Objects.requireNonNull(kind);
        Objects.requireNonNull(amount);
        Objects.requireNonNull(occurredAt);
        if (amount.amount().signum() <= 0) {
            throw new IllegalArgumentException("Entry amount must be positive");
        }
    }

    public static AccountEntry demoCredit(UUID accountId, Money amount, Instant occurredAt) {
        return new AccountEntry(UUID.randomUUID(), accountId, Kind.DEMO_CREDIT, amount, occurredAt);
    }
}
