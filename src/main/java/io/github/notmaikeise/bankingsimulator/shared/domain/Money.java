package io.github.notmaikeise.bankingsimulator.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** A non-negative amount in BRL. Operation inputs use positive(). */
public record Money(BigDecimal amount) {
    public Money {
        Objects.requireNonNull(amount, "amount");
        amount = amount.setScale(2, RoundingMode.UNNECESSARY);
        if (amount.signum() < 0 || amount.precision() - amount.scale() > 17) {
            throw new IllegalArgumentException("Invalid BRL amount");
        }
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public static Money positive(BigDecimal amount) {
        Money money = new Money(amount);
        if (money.amount.signum() == 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        return money;
    }

    public Money add(Money other) {
        return new Money(amount.add(other.amount));
    }

    public Money subtract(Money other) {
        return new Money(amount.subtract(other.amount));
    }

    public String currency() {
        return "BRL";
    }
}
