package io.github.notmaikeise.bankingsimulator.accounts.application;

import java.util.UUID;

/** Public Accounts operation invoked by Access after registering a user. */
public interface OpenAccount {
    UUID open(UUID ownerId);
}
