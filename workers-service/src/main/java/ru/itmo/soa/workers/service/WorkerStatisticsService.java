package ru.itmo.soa.workers.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import ru.itmo.soa.workers.exception.ApiException;
import ru.itmo.soa.workers.dto.WorkerResponseDto;
import ru.itmo.soa.workers.model.WorkerEntity;
import ru.itmo.soa.workers.repository.WorkerRepository;

@ApplicationScoped
@Transactional(Transactional.TxType.SUPPORTS)
public class WorkerStatisticsService {

    @Inject
    private WorkerRepository repository;

    public BigDecimal averageSalary() {
        List<WorkerEntity> workers = repository.findAll();
        if (workers.isEmpty()) {
            return null;
        }

        BigDecimal sum = workers.stream()
                .map(worker -> BigDecimal.valueOf(worker.getSalary()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(
                BigDecimal.valueOf(workers.size()),
                10,
                RoundingMode.HALF_UP
        ).stripTrailingZeros();
    }

    public WorkerResponseDto workerWithMaxPosition() {
        WorkerEntity worker = repository.findAll().stream()
                .filter(value -> value.getPosition() != null)
                .max((first, second) -> {
                    int positionComparison = first.getPosition().compareTo(second.getPosition());
                    if (positionComparison != 0) {
                        return positionComparison;
                    }
                    return Long.compare(second.getId(), first.getId());
                })
                .orElseThrow(() -> new ApiException(404, "No worker with a position"));

        return WorkerService.toDto(worker);
    }

    public long countWithSalaryBelow(long salary) {
        return repository.findAll().stream()
                .filter(worker -> worker.getSalary() < salary)
                .count();
    }
}
