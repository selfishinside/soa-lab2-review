package ru.itmo.soa.workers.repository;

import java.util.List;
import ru.itmo.soa.workers.model.OrganizationEntity;
import ru.itmo.soa.workers.model.WorkerEntity;

public interface OrganizationRepository {

    OrganizationEntity find(long id);

    OrganizationEntity getOrCreate(long id);

    List<OrganizationEntity> findAll();

    List<Long> workerIds(long organizationId);

    List<Long> workerIdsForUpdate(long organizationId);

    List<WorkerEntity> findWorkers(List<Long> workerIds);

    List<WorkerEntity> findWorkersForUpdate(List<Long> workerIds);

    WorkerEntity findWorkerForUpdate(long workerId);

    void assign(long workerId, long organizationId);
}
