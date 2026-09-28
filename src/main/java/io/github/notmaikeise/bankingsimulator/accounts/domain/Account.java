package io.github.notmaikeise.bankingsimulator.accounts.domain;

import io.github.notmaikeise.bankingsimulator.shared.domain.Money;
import java.util.Objects;
import java.util.UUID;

public final class Account {
    private final UUID id;
    private final UUID ownerId;
    private Money balance;

    private Account(UUID id, UUID ownerId, Money balance) {
        this.id = Objects.requireNonNull(id);
        this.ownerId = Objects.requireNonNull(ownerId);
        this.balance = Objects.requireNonNull(balance);
    }

    public static Account open(UUID ownerId) {
        return new Account(UUID.randomUUID(), ownerId, Money.zero());
    }

    public static Account restore(UUID id, UUID ownerId, Money balance) {
        return new Account(id, ownerId, balance);
    }

    public void credit(Money amount) {
        requirePositive(amount);
        balance = balance.add(amount);
    }

    public void debit(Money amount) {
        requirePositive(amount);
        balance = balance.subtract(amount);
    }

    private static void requirePositive(Money amount) {
        if (Objects.requireNonNull(amount).amount().signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    public UUID id() { return id; }
    public UUID ownerId() { return ownerId; }
    public Money balance() { return balance; }
}
