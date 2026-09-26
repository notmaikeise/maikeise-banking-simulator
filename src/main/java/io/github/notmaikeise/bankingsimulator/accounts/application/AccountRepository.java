package io.github.notmaikeise.bankingsimulator.accounts.application;

import io.github.notmaikeise.bankingsimulator.accounts.domain.Account;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    void save(Account account);
    Optional<Account> findByOwner(UUID ownerId);
    Optional<Account> findByOwnerForUpdate(UUID ownerId);
}
