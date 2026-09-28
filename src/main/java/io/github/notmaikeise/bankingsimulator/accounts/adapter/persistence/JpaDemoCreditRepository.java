package io.github.notmaikeise.bankingsimulator.accounts.adapter.persistence;

import io.github.notmaikeise.bankingsimulator.accounts.application.DemoCreditRepository;
import io.github.notmaikeise.bankingsimulator.shared.domain.Money;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaDemoCreditRepository implements DemoCreditRepository {
    private final EntityManager entityManager;

    public JpaDemoCreditRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Receipt> find(UUID ownerId, UUID key) {
        return entityManager.createQuery("select r from DemoCreditEntity r "
                        + "where r.ownerId = :ownerId and r.idempotencyKey = :key", DemoCreditEntity.class)
                .setParameter("ownerId", ownerId).setParameter("key", key)
                .getResultStream().findFirst()
                .map(r -> new Receipt(r.entryId, new Money(r.amount), new Money(r.balanceAfter), r.occurredAt));
    }

    @Override
    public void record(UUID ownerId, UUID key, Receipt receipt) {
        entityManager.persist(new DemoCreditEntity(ownerId, key, receipt.amount().amount(),
                receipt.entryId(), receipt.balanceAfter().amount(), receipt.occurredAt()));
    }
}
