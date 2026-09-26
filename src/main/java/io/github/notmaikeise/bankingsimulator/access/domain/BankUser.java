package io.github.notmaikeise.bankingsimulator.access.domain;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public record BankUser(UUID id, String name, String email, String passwordHash) {
    public BankUser {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        Objects.requireNonNull(email);
        Objects.requireNonNull(passwordHash);
        name = name.trim();
        email = email.trim().toLowerCase(Locale.ROOT);
        if (name.isBlank() || name.length() > 120 || email.length() > 254
                || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$") || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Invalid user data");
        }
    }

    public static BankUser register(String name, String email, String passwordHash) {
        return new BankUser(UUID.randomUUID(), name, email, passwordHash);
    }
}
