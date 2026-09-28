package io.github.notmaikeise.bankingsimulator.access.application;

import io.github.notmaikeise.bankingsimulator.access.domain.BankUser;
import io.github.notmaikeise.bankingsimulator.accounts.application.OpenAccount;
import io.github.notmaikeise.bankingsimulator.shared.application.BusinessConflictException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUserService {
    private final UserRepository users;
    private final PasswordHasher passwords;
    private final OpenAccount accounts;

    public RegisterUserService(UserRepository users, PasswordHasher passwords, OpenAccount accounts) {
        this.users = users;
        this.passwords = passwords;
        this.accounts = accounts;
    }

    @Transactional
    public UUID register(String name, String email, String password) {
        if (password == null || password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must contain 8 characters and at most 72 UTF-8 bytes");
        }
        BankUser user = BankUser.register(name, email, passwords.hash(password));
        if (users.findByEmail(user.email()).isPresent()) {
            throw new BusinessConflictException("Email already registered");
        }
        users.save(user);
        accounts.open(user.id());
        return user.id();
    }
}
