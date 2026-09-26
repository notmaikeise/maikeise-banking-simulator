package io.github.notmaikeise.bankingsimulator.accounts.application;

import io.github.notmaikeise.bankingsimulator.accounts.domain.AccountEntry;
import java.util.List;
import java.util.UUID;

public interface EntryRepository {
    void append(AccountEntry entry);
    List<AccountEntry> listForAccount(UUID accountId, int offset, int limit);
}
