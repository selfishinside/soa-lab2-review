package ru.itmo.soa.hr.service;

import org.springframework.stereotype.Service;
import ru.itmo.soa.hr.client.WorkersClient;
import ru.itmo.soa.hr.dto.WorkerDto;
import ru.itmo.soa.hr.validation.Validation;

@Service
public final class WorkerIndexService {

    private final WorkersClient workersClient;

    public WorkerIndexService(WorkersClient workersClient) {
        this.workersClient = workersClient;
    }

    public WorkerDto index(String workerId, String coefficient) {
        return workersClient.indexWorker(
                Validation.workerId(workerId),
                Validation.coefficient(coefficient)
        );
    }
}
