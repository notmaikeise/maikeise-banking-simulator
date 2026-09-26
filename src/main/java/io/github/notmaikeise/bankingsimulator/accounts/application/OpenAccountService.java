package io.github.notmaikeise.bankingsimulator.accounts.application;

import io.github.notmaikeise.bankingsimulator.accounts.domain.Account;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class OpenAccountService implements OpenAccount {
    private final AccountRepository accounts;

    public OpenAccountService(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    public UUID open(UUID ownerId) {
        Account account = Account.open(ownerId);
        accounts.save(account);
        return account.id();
    }
}
