package io.github.notmaikeise.bankingsimulator.accounts.application;

import io.github.notmaikeise.bankingsimulator.accounts.domain.Account;
import io.github.notmaikeise.bankingsimulator.accounts.domain.AccountEntry;
import io.github.notmaikeise.bankingsimulator.shared.application.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountQueryService {
    private final AccountRepository accounts;
    private final EntryRepository entries;

    public AccountQueryService(AccountRepository accounts, EntryRepository entries) {
        this.accounts = accounts;
        this.entries = entries;
    }

    @Transactional(readOnly = true)
    public Account forOwner(UUID ownerId) {
        return accounts.findByOwner(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    @Transactional(readOnly = true)
    public List<AccountEntry> statement(UUID ownerId, int page, int size) {
        if (page < 0 || size < 1 || size > 100 || (long) page * size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid statement pagination");
        }
        Account account = forOwner(ownerId);
        return entries.listForAccount(account.id(), page * size, size);
    }
}
