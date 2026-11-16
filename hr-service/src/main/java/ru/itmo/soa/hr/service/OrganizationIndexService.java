package ru.itmo.soa.hr.service;

import org.springframework.stereotype.Service;
import ru.itmo.soa.hr.client.WorkersClient;
import ru.itmo.soa.hr.dto.IndexResultDto;
import ru.itmo.soa.hr.validation.Validation;

@Service
public final class OrganizationIndexService {

    private final WorkersClient workersClient;

    public OrganizationIndexService(WorkersClient workersClient) {
        this.workersClient = workersClient;
    }

    public IndexResultDto index(String organizationId, String coefficient) {
        return workersClient.indexOrganization(
                Validation.organizationId(organizationId),
                Validation.coefficient(coefficient)
        );
    }
}
