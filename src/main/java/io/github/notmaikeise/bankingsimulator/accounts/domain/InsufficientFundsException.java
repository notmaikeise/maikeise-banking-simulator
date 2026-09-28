package io.github.notmaikeise.bankingsimulator.accounts.domain;

import io.github.notmaikeise.bankingsimulator.shared.domain.DomainRuleConflictException;

public final class InsufficientFundsException extends DomainRuleConflictException {
    public InsufficientFundsException() {
        super("Insufficient funds");
    }
}
