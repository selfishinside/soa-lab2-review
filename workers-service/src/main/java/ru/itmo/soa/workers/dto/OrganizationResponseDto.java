package ru.itmo.soa.workers.dto;

import java.util.List;

public record OrganizationResponseDto(
        Long id,
        List<Long> workerIds
) {
}
