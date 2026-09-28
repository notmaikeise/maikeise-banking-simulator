package io.github.notmaikeise.bankingsimulator.accounts.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.notmaikeise.bankingsimulator.shared.domain.Money;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AccountTest {
    @Test
    void balanceNeverBecomesNegative() {
        Account account = Account.open(UUID.randomUUID());
        assertThrows(IllegalArgumentException.class, () -> account.debit(Money.positive(new BigDecimal("0.01"))));
        assertEquals(Money.zero(), account.balance());
    }

    @Test
    void rejectsFractionsOfCents() {
        assertThrows(ArithmeticException.class, () -> new Money(new BigDecimal("1.001")));
    }
}
