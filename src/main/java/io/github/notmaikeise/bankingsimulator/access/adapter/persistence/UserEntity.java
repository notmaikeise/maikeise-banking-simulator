package io.github.notmaikeise.bankingsimulator.access.adapter.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "bank_users")
public class UserEntity {
    @Id
    UUID id;
    @Column(nullable = false, length = 120)
    String name;
    @Column(nullable = false, unique = true, length = 254)
    String email;
    @Column(name = "password_hash", nullable = false, length = 100)
    String passwordHash;

    protected UserEntity() { }

    UserEntity(UUID id, String name, String email, String passwordHash) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }
}
