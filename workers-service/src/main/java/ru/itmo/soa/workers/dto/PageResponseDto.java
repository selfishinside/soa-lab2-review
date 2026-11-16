package ru.itmo.soa.workers.dto;

import java.util.List;

public record PageResponseDto<T>(
        int page,
        int size,
        long totalElements,
        long totalPages,
        List<T> content
) {
}
