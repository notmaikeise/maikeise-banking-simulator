package io.github.notmaikeise.bankingsimulator.access.adapter.persistence;

import io.github.notmaikeise.bankingsimulator.access.application.UserRepository;
import io.github.notmaikeise.bankingsimulator.access.domain.BankUser;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaUserRepository implements UserRepository {
    private final EntityManager entityManager;

    public JpaUserRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BankUser> findByEmail(String email) {
        return entityManager.createQuery("select u from UserEntity u where u.email = :email", UserEntity.class)
                .setParameter("email", email).setMaxResults(1).getResultList().stream().findFirst()
                .map(u -> new BankUser(u.id, u.name, u.email, u.passwordHash));
    }

    @Override
    public void save(BankUser user) {
        entityManager.persist(new UserEntity(user.id(), user.name(), user.email(), user.passwordHash()));
    }
}
