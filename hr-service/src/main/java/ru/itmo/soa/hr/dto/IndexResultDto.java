package ru.itmo.soa.hr.dto;

import java.math.BigDecimal;

public record IndexResultDto(
        Long orgId,
        BigDecimal coeff,
        long updatedCount
) {
}
