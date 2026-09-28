package io.github.notmaikeise.bankingsimulator.access.application;

import io.github.notmaikeise.bankingsimulator.access.domain.BankUser;
import java.util.Optional;

public interface UserRepository {
    Optional<BankUser> findByEmail(String email);
    void save(BankUser user);
}
