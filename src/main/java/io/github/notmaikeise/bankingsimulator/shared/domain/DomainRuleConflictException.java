package io.github.notmaikeise.bankingsimulator.shared.domain;

/** A valid command that the current business state cannot fulfill. */
public class DomainRuleConflictException extends IllegalArgumentException {
    public DomainRuleConflictException(String message) {
        super(message);
    }
}
