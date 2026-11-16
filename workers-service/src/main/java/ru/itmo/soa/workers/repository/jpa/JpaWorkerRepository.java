package ru.itmo.soa.workers.repository.jpa;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import ru.itmo.soa.workers.model.OrganizationEntity;
import ru.itmo.soa.workers.model.WorkerEntity;
import ru.itmo.soa.workers.model.WorkerMembershipEntity;
import ru.itmo.soa.workers.repository.WorkerRepository;

@ApplicationScoped
public class JpaWorkerRepository implements WorkerRepository {

    @PersistenceContext(unitName = "workersPU")
    private EntityManager entityManager;

    @Override
    public WorkerEntity find(long id) {
        return entityManager.find(WorkerEntity.class, id);
    }

    @Override
    public WorkerEntity findForUpdate(long id) {
        entityManager.find(
                WorkerMembershipEntity.class,
                id,
                LockModeType.PESSIMISTIC_WRITE
        );
        return entityManager.find(
                WorkerEntity.class,
                id,
                LockModeType.PESSIMISTIC_WRITE
        );
    }

    @Override
    public List<WorkerEntity> findAll() {
        return entityManager
                .createQuery("select w from WorkerEntity w order by w.id", WorkerEntity.class)
                .getResultList();
    }

    @Override
    public WorkerEntity save(WorkerEntity worker) {
        if (worker.getId() == null) {
            entityManager.persist(worker);
            entityManager.flush();
            return worker;
        }
        return entityManager.merge(worker);
    }

    @Override
    public void delete(WorkerEntity worker) {
        WorkerEntity managedWorker = entityManager.contains(worker)
                ? worker
                : entityManager.merge(worker);
        entityManager.remove(managedWorker);
    }

    @Override
    public void includeInOrganizationOne(long workerId) {
        OrganizationEntity organization = entityManager.find(OrganizationEntity.class, 1L);
        if (organization == null) {
            entityManager.persist(new OrganizationEntity(1L));
        }
        entityManager.merge(new WorkerMembershipEntity(workerId, 1L));
    }

    @Override
    public void deleteMembership(long workerId) {
        WorkerMembershipEntity membership = entityManager.find(
                WorkerMembershipEntity.class,
                workerId
        );
        if (membership != null) {
            entityManager.remove(membership);
        }
    }
}
