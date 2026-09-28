package io.github.notmaikeise.bankingsimulator.payments.application;

import io.github.notmaikeise.bankingsimulator.payments.domain.Transfer;
import java.util.Optional;
import java.util.UUID;

public interface TransferRepository {
    Optional<Transfer> findByOwnerAndKey(UUID ownerId, UUID key);
    void record(Transfer transfer);
}
