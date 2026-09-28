package io.github.notmaikeise.bankingsimulator.accounts.adapter.persistence;

import io.github.notmaikeise.bankingsimulator.accounts.application.EntryRepository;
import io.github.notmaikeise.bankingsimulator.accounts.domain.AccountEntry;
import io.github.notmaikeise.bankingsimulator.shared.domain.Money;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaEntryRepository implements EntryRepository {
    private final EntityManager entityManager;

    public JpaEntryRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void append(AccountEntry entry) {
        entityManager.persist(new EntryEntity(entry.id(), entry.accountId(), entry.kind(),
                entry.amount().amount(), entry.occurredAt()));
    }

    @Override
    public List<AccountEntry> listForAccount(UUID accountId, int offset, int limit) {
        return entityManager.createQuery("select e from EntryEntity e where e.accountId = :accountId "
                        + "order by e.occurredAt desc, e.id desc", EntryEntity.class)
                .setParameter("accountId", accountId).setFirstResult(offset).setMaxResults(limit)
                .getResultList().stream().map(e -> new AccountEntry(e.id, e.accountId, e.kind,
                        new Money(e.amount), e.occurredAt)).toList();
    }
}
