package ru.itmo.soa.workers.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import ru.itmo.soa.workers.dto.IndexResultDto;
import ru.itmo.soa.workers.exception.ApiException;
import ru.itmo.soa.workers.repository.OrganizationRepository;
import ru.itmo.soa.workers.validation.Validation;

@ApplicationScoped
public class IndexService {
    @Inject
    private OrganizationRepository repository;
    @Transactional(Transactional.TxType.REQUIRED)
    public IndexResultDto index(long organizationId, String coefficientText) {
        if (repository.find(organizationId) == null) {
            throw new ApiException(404, "Organization not found");
        }
        BigDecimal coefficient = Validation.positiveCoefficient(coefficientText, 400);
        var workerIds = repository.workerIdsForUpdate(organizationId);
        var workers = repository.findWorkersForUpdate(workerIds);
        for (var worker : workers) {
            BigDecimal salary = BigDecimal.valueOf(worker.getSalary())
                    .multiply(coefficient)
                    .setScale(0, RoundingMode.HALF_UP);
            Validation.require(salary.signum() > 0 && salary.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) <= 0,
                    422, "Indexed salary must be between 1 and 2147483647");
            worker.setSalary(salary.longValueExact());
        }
        return new IndexResultDto(organizationId, coefficient, workers.size());
    }
}
