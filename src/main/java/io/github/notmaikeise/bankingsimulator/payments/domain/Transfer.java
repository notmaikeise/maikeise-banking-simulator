package io.github.notmaikeise.bankingsimulator.payments.domain;

import io.github.notmaikeise.bankingsimulator.shared.domain.Money;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Completed internal Pix. The request key and outcome are stored atomically with both entries. */
public record Transfer(UUID id, UUID ownerId, UUID idempotencyKey, UUID sourceAccountId,
                       UUID destinationAccountId, Money amount, UUID sourceEntryId,
                       UUID destinationEntryId, Money sourceBalanceAfter, Instant occurredAt) {
    public Transfer {
        Objects.requireNonNull(id);
        Objects.requireNonNull(ownerId);
        Objects.requireNonNull(idempotencyKey);
        Objects.requireNonNull(sourceAccountId);
        Objects.requireNonNull(destinationAccountId);
        Objects.requireNonNull(amount);
        Objects.requireNonNull(sourceEntryId);
        Objects.requireNonNull(destinationEntryId);
        Objects.requireNonNull(sourceBalanceAfter);
        Objects.requireNonNull(occurredAt);
        if (sourceAccountId.equals(destinationAccountId) || sourceEntryId.equals(destinationEntryId)
                || amount.amount().signum() <= 0) {
            throw new IllegalArgumentException("Invalid internal transfer");
        }
    }

    public boolean matches(UUID destination, Money requestedAmount) {
        return destinationAccountId.equals(destination) && amount.equals(requestedAmount);
    }
}
