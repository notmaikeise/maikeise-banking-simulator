package io.github.notmaikeise.bankingsimulator.access.application;

public interface PasswordHasher {
    String hash(String rawPassword);
}
