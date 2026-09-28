package io.github.notmaikeise.bankingsimulator.payments.application;

import io.github.notmaikeise.bankingsimulator.accounts.application.TransferFunds;
import io.github.notmaikeise.bankingsimulator.payments.domain.Transfer;
import io.github.notmaikeise.bankingsimulator.shared.application.BusinessConflictException;
import io.github.notmaikeise.bankingsimulator.shared.domain.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {
    private final TransferFunds accounts;
    private final TransferRepository transfers;

    public TransferService(TransferFunds accounts, TransferRepository transfers) {
        this.accounts = accounts;
        this.transfers = transfers;
    }

    @Transactional
    public Transfer transfer(UUID ownerId, UUID key, UUID destinationAccountId, BigDecimal rawAmount) {
        Objects.requireNonNull(ownerId);
        if (key == null || destinationAccountId == null) {
            throw new IllegalArgumentException("Destination account and idempotency key are required");
        }
        Money amount = Money.positive(rawAmount);

        // A completed request can be answered without locking accounts; a concurrent in-flight
        // request is checked again after locking, before either balance is changed.
        var completed = transfers.findByOwnerAndKey(ownerId, key);
        if (completed.isPresent()) {
            if (!completed.get().matches(destinationAccountId, amount)) {
                throw new BusinessConflictException("Idempotency key was used with another transfer");
            }
            return completed.get();
        }

        // Locking precedes the second lookup so concurrent retries see the committed result.
        TransferFunds.LockedTransfer locked = accounts.lock(ownerId, destinationAccountId);
        var previous = transfers.findByOwnerAndKey(ownerId, key);
        if (previous.isPresent()) {
            if (!previous.get().matches(destinationAccountId, amount)) {
                throw new BusinessConflictException("Idempotency key was used with another transfer");
            }
            return previous.get();
        }

        UUID transferId = UUID.randomUUID();
        Instant occurredAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        TransferFunds.Movement movement = locked.move(transferId, amount, occurredAt);
        Transfer transfer = new Transfer(transferId, ownerId, key, locked.sourceAccountId(),
                locked.destinationAccountId(), amount, movement.sourceEntryId(),
                movement.destinationEntryId(), movement.sourceBalanceAfter(), occurredAt);
        transfers.record(transfer);
        return transfer;
    }
}
