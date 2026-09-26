package io.github.notmaikeise.bankingsimulator.accounts.application;

import io.github.notmaikeise.bankingsimulator.accounts.domain.Account;
import io.github.notmaikeise.bankingsimulator.accounts.domain.AccountEntry;
import io.github.notmaikeise.bankingsimulator.accounts.application.DemoCreditRepository.Receipt;
import io.github.notmaikeise.bankingsimulator.shared.application.BusinessConflictException;
import io.github.notmaikeise.bankingsimulator.shared.application.ResourceNotFoundException;
import io.github.notmaikeise.bankingsimulator.shared.domain.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoCreditService {
    private final AccountRepository accounts;
    private final EntryRepository entries;
    private final DemoCreditRepository requests;

    public DemoCreditService(AccountRepository accounts, EntryRepository entries, DemoCreditRepository requests) {
        this.accounts = accounts;
        this.entries = entries;
        this.requests = requests;
    }

    @Transactional
    public Receipt credit(UUID ownerId, UUID key, BigDecimal rawAmount) {
        if (key == null) {
            throw new IllegalArgumentException("Idempotency key is required");
        }
        Money amount = Money.positive(rawAmount);
        // Lock first: concurrent requests for the same owner serialize before checking the key.
        Account account = accounts.findByOwnerForUpdate(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        var previous = requests.find(ownerId, key);
        if (previous.isPresent()) {
            if (!previous.get().amount().equals(amount)) {
                throw new BusinessConflictException("Idempotency key was used with another amount");
            }
            return previous.get();
        }
        account.credit(amount);
        AccountEntry entry = AccountEntry.demoCredit(account.id(), amount,
                Instant.now().truncatedTo(ChronoUnit.MICROS));
        Receipt receipt = new Receipt(entry.id(), amount, account.balance(), entry.occurredAt());
        accounts.save(account);
        entries.append(entry);
        requests.record(ownerId, key, receipt);
        return receipt;
    }
}
