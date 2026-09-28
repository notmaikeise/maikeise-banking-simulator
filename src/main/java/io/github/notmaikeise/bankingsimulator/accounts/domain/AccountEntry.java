package io.github.notmaikeise.bankingsimulator.accounts.domain;

import io.github.notmaikeise.bankingsimulator.shared.domain.Money;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record AccountEntry(UUID id, UUID accountId, Kind kind, Money amount,
                           UUID referenceId, Instant occurredAt) {
    public enum Kind { DEMO_CREDIT, PIX_SENT, PIX_RECEIVED }

    public AccountEntry {
        Objects.requireNonNull(id);
        Objects.requireNonNull(accountId);
        Objects.requireNonNull(kind);
        Objects.requireNonNull(amount);
        Objects.requireNonNull(occurredAt);
        if (amount.amount().signum() <= 0) {
            throw new IllegalArgumentException("Entry amount must be positive");
        }
        if ((kind == Kind.DEMO_CREDIT) != (referenceId == null)) {
            throw new IllegalArgumentException("Pix entries require a transfer reference");
        }
    }

    public static AccountEntry demoCredit(UUID accountId, Money amount, Instant occurredAt) {
        return new AccountEntry(UUID.randomUUID(), accountId, Kind.DEMO_CREDIT, amount, null, occurredAt);
    }

    public static AccountEntry pixSent(UUID accountId, Money amount, UUID transferId, Instant occurredAt) {
        return new AccountEntry(UUID.randomUUID(), accountId, Kind.PIX_SENT, amount,
                Objects.requireNonNull(transferId), occurredAt);
    }

    public static AccountEntry pixReceived(UUID accountId, Money amount, UUID transferId, Instant occurredAt) {
        return new AccountEntry(UUID.randomUUID(), accountId, Kind.PIX_RECEIVED, amount,
                Objects.requireNonNull(transferId), occurredAt);
    }
}
