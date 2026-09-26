package io.github.notmaikeise.bankingsimulator.accounts.adapter.persistence;

import io.github.notmaikeise.bankingsimulator.accounts.application.AccountRepository;
import io.github.notmaikeise.bankingsimulator.accounts.domain.Account;
import io.github.notmaikeise.bankingsimulator.shared.domain.Money;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaAccountRepository implements AccountRepository {
    private final EntityManager entityManager;

    public JpaAccountRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void save(Account account) {
        AccountEntity entity = entityManager.find(AccountEntity.class, account.id());
        if (entity == null) {
            entityManager.persist(new AccountEntity(account.id(), account.ownerId(), account.balance().amount()));
        } else {
            entity.balance = account.balance().amount();
        }
    }

    @Override
    public Optional<Account> findByOwner(UUID ownerId) {
        return entityManager.createQuery("select a from AccountEntity a where a.ownerId = :ownerId", AccountEntity.class)
                .setParameter("ownerId", ownerId).getResultStream().findFirst().map(this::toDomain);
    }

    @Override
    public Optional<Account> findByOwnerForUpdate(UUID ownerId) {
        return entityManager.createQuery("select a from AccountEntity a where a.ownerId = :ownerId", AccountEntity.class)
                .setParameter("ownerId", ownerId).setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultStream().findFirst().map(this::toDomain);
    }

    private Account toDomain(AccountEntity entity) {
        return Account.restore(entity.id, entity.ownerId, new Money(entity.balance));
    }
}
