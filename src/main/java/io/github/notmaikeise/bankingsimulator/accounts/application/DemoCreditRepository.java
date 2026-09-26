package io.github.notmaikeise.bankingsimulator.accounts.application;

import io.github.notmaikeise.bankingsimulator.shared.domain.Money;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface DemoCreditRepository {
    Optional<Receipt> find(UUID ownerId, UUID key);
    void record(UUID ownerId, UUID key, Receipt receipt);

    record Receipt(UUID entryId, Money amount, Money balanceAfter, Instant occurredAt) { }
}
