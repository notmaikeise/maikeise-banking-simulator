package io.github.notmaikeise.bankingsimulator.payments.adapter.persistence;

import io.github.notmaikeise.bankingsimulator.payments.application.TransferRepository;
import io.github.notmaikeise.bankingsimulator.payments.domain.Transfer;
import io.github.notmaikeise.bankingsimulator.shared.domain.Money;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaTransferRepository implements TransferRepository {
    private final EntityManager entityManager;

    public JpaTransferRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Transfer> findByOwnerAndKey(UUID ownerId, UUID key) {
        return entityManager.createQuery("select t from TransferEntity t "
                        + "where t.ownerId = :ownerId and t.idempotencyKey = :key", TransferEntity.class)
                .setParameter("ownerId", ownerId).setParameter("key", key)
                .getResultStream().findFirst().map(t -> new Transfer(t.id, t.ownerId, t.idempotencyKey,
                        t.sourceAccountId, t.destinationAccountId, new Money(t.amount), t.sourceEntryId,
                        t.destinationEntryId, new Money(t.sourceBalanceAfter), t.occurredAt));
    }

    @Override
    public void record(Transfer transfer) {
        entityManager.persist(new TransferEntity(transfer.id(), transfer.ownerId(), transfer.idempotencyKey(),
                transfer.sourceAccountId(), transfer.destinationAccountId(), transfer.amount().amount(),
                transfer.sourceEntryId(), transfer.destinationEntryId(),
                transfer.sourceBalanceAfter().amount(), transfer.occurredAt()));
    }
}
