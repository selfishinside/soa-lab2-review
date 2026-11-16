package ru.itmo.soa.workers.repository;

import java.util.List;
import ru.itmo.soa.workers.model.WorkerEntity;

public interface WorkerRepository {

    WorkerEntity find(long id);

    WorkerEntity findForUpdate(long id);

    List<WorkerEntity> findAll();

    WorkerEntity save(WorkerEntity worker);

    void delete(WorkerEntity worker);

    void includeInOrganizationOne(long workerId);

    void deleteMembership(long workerId);
}
