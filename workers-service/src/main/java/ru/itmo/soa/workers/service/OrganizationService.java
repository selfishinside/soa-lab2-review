package ru.itmo.soa.workers.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import ru.itmo.soa.workers.dto.OrganizationResponseDto;
import ru.itmo.soa.workers.dto.PageResponseDto;
import ru.itmo.soa.workers.dto.WorkerResponseDto;
import ru.itmo.soa.workers.dto.WorkerSearchRequestDto;
import ru.itmo.soa.workers.exception.ApiException;
import ru.itmo.soa.workers.repository.OrganizationRepository;

@ApplicationScoped
public class OrganizationService {

    @Inject
    private OrganizationRepository repository;

    @Transactional(Transactional.TxType.REQUIRED)
    public List<OrganizationResponseDto> list() {
        repository.getOrCreate(1L);
        return repository.findAll().stream()
                .map(organization -> new OrganizationResponseDto(
                        organization.getId(),
                        List.copyOf(repository.workerIds(organization.getId()))
                ))
                .toList();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public PageResponseDto<WorkerResponseDto> workers(
            long organizationId,
            WorkerSearchRequestDto filter
    ) {
        requireOrganization(organizationId);
        List<WorkerResponseDto> workers = repository
                .findWorkers(repository.workerIds(organizationId))
                .stream()
                .map(WorkerService::toDto)
                .toList();
        return WorkerSearchService.apply(filter, workers);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void assign(long organizationId, long workerId) {
        if (repository.findWorkerForUpdate(workerId) == null) {
            throw new ApiException(404, "Worker not found");
        }

        repository.getOrCreate(organizationId);
        repository.assign(workerId, organizationId);
    }

    private void requireOrganization(long organizationId) {
        if (repository.find(organizationId) == null) {
            throw new ApiException(404, "Organization not found");
        }
    }
}
