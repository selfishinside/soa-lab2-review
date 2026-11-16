package ru.itmo.soa.workers.repository.jpa;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import ru.itmo.soa.workers.model.OrganizationEntity;
import ru.itmo.soa.workers.model.WorkerEntity;
import ru.itmo.soa.workers.model.WorkerMembershipEntity;
import ru.itmo.soa.workers.repository.OrganizationRepository;

@ApplicationScoped
public class JpaOrganizationRepository implements OrganizationRepository {

    @PersistenceContext(unitName = "workersPU")
    private EntityManager entityManager;

    @Override
    public OrganizationEntity find(long id) {
        return entityManager.find(OrganizationEntity.class, id);
    }

    @Override
    public OrganizationEntity getOrCreate(long id) {
        OrganizationEntity organization = find(id);
        if (organization == null) {
            organization = new OrganizationEntity(id);
            entityManager.persist(organization);
        }
        return organization;
    }

    @Override
    public List<OrganizationEntity> findAll() {
        return entityManager
                .createQuery(
                        "select o from OrganizationEntity o order by o.id",
                        OrganizationEntity.class
                )
                .getResultList();
    }

    @Override
    public List<Long> workerIds(long organizationId) {
        return entityManager
                .createQuery(
                        "select m.workerId from WorkerMembershipEntity m "
                                + "where m.organizationId = :organization "
                                + "order by m.workerId",
                        Long.class
                )
                .setParameter("organization", organizationId)
                .getResultList();
    }

    @Override
    public List<Long> workerIdsForUpdate(long organizationId) {
        return entityManager
                .createQuery(
                        "select m from WorkerMembershipEntity m "
                                + "where m.organizationId = :organization "
                                + "order by m.workerId",
                        WorkerMembershipEntity.class
                )
                .setParameter("organization", organizationId)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList()
                .stream()
                .map(WorkerMembershipEntity::getWorkerId)
                .toList();
    }

    @Override
    public List<WorkerEntity> findWorkers(List<Long> workerIds) {
        if (workerIds.isEmpty()) {
            return List.of();
        }
        return entityManager
                .createQuery(
                        "select w from WorkerEntity w where w.id in :ids order by w.id",
                        WorkerEntity.class
                )
                .setParameter("ids", workerIds)
                .getResultList();
    }

    @Override
    public List<WorkerEntity> findWorkersForUpdate(List<Long> workerIds) {
        if (workerIds.isEmpty()) {
            return List.of();
        }
        return entityManager
                .createQuery(
                        "select w from WorkerEntity w where w.id in :ids order by w.id",
                        WorkerEntity.class
                )
                .setParameter("ids", workerIds)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();
    }

    @Override
    public WorkerEntity findWorkerForUpdate(long workerId) {
        entityManager.find(
                WorkerMembershipEntity.class,
                workerId,
                LockModeType.PESSIMISTIC_WRITE
        );
        return entityManager.find(
                WorkerEntity.class,
                workerId,
                LockModeType.PESSIMISTIC_WRITE
        );
    }

    @Override
    public void assign(long workerId, long organizationId) {
        entityManager.merge(new WorkerMembershipEntity(workerId, organizationId));
    }
}
