package io.github.notmaikeise.bankingsimulator.shared.application;

public class BusinessConflictException extends RuntimeException {
    public BusinessConflictException(String message) {
        super(message);
    }
}
