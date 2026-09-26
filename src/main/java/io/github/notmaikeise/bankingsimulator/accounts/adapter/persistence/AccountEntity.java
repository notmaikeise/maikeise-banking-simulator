package io.github.notmaikeise.bankingsimulator.accounts.adapter.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class AccountEntity {
    @Id
    UUID id;
    @Column(name = "owner_id", nullable = false, unique = true)
    UUID ownerId;
    @Column(nullable = false, precision = 19, scale = 2)
    BigDecimal balance;

    protected AccountEntity() { }

    AccountEntity(UUID id, UUID ownerId, BigDecimal balance) {
        this.id = id;
        this.ownerId = ownerId;
        this.balance = balance;
    }
}
