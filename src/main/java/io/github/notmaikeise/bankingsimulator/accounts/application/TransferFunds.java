package io.github.notmaikeise.bankingsimulator.accounts.application;

import io.github.notmaikeise.bankingsimulator.shared.domain.Money;
import java.time.Instant;
import java.util.UUID;

/** Public Accounts operation for a local transfer; the caller owns the transaction. */
public interface TransferFunds {
    LockedTransfer lock(UUID sourceOwnerId, UUID destinationAccountId);

    interface LockedTransfer {
        UUID sourceAccountId();
        UUID destinationAccountId();
        Movement move(UUID transferId, Money amount, Instant occurredAt);
    }

    record Movement(UUID sourceEntryId, UUID destinationEntryId, Money sourceBalanceAfter) { }
}
