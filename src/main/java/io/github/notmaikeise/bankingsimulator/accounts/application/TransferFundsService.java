package io.github.notmaikeise.bankingsimulator.accounts.application;

import io.github.notmaikeise.bankingsimulator.accounts.domain.Account;
import io.github.notmaikeise.bankingsimulator.accounts.domain.AccountEntry;
import io.github.notmaikeise.bankingsimulator.shared.application.ResourceNotFoundException;
import io.github.notmaikeise.bankingsimulator.shared.domain.Money;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TransferFundsService implements TransferFunds {
    private final AccountRepository accounts;
    private final EntryRepository entries;

    public TransferFundsService(AccountRepository accounts, EntryRepository entries) {
        this.accounts = accounts;
        this.entries = entries;
    }

    @Override
    public LockedTransfer lock(UUID sourceOwnerId, UUID destinationAccountId) {
        UUID sourceId = accounts.findIdByOwner(sourceOwnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (sourceId.equals(destinationAccountId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        // All callers lock both account rows in the same order, including opposite-direction Pix.
        UUID firstId = sourceId.compareTo(destinationAccountId) < 0 ? sourceId : destinationAccountId;
        UUID secondId = firstId.equals(sourceId) ? destinationAccountId : sourceId;
        Account first = accounts.findByIdForUpdate(firstId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found"));
        Account second = accounts.findByIdForUpdate(secondId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found"));
        Account source = first.id().equals(sourceId) ? first : second;
        Account destination = first.id().equals(destinationAccountId) ? first : second;

        return new LockedTransfer() {
            private boolean used;

            @Override public UUID sourceAccountId() { return sourceId; }
            @Override public UUID destinationAccountId() { return destinationAccountId; }

            @Override
            public Movement move(UUID transferId, Money amount, Instant occurredAt) {
                if (used) {
                    throw new IllegalStateException("Locked transfer already used");
                }
                used = true;
                source.debit(amount);
                destination.credit(amount);
                AccountEntry debit = AccountEntry.pixSent(sourceId, amount, transferId, occurredAt);
                AccountEntry credit = AccountEntry.pixReceived(destinationAccountId, amount, transferId, occurredAt);
                accounts.save(source);
                accounts.save(destination);
                entries.append(debit);
                entries.append(credit);
                return new Movement(debit.id(), credit.id(), source.balance());
            }
        };
    }
}
