package ru.itmo.soa.workers.dto;

import java.math.BigDecimal;

public record IndexResultDto(
        Long orgId,
        BigDecimal coeff,
        long updatedCount
) {
}
