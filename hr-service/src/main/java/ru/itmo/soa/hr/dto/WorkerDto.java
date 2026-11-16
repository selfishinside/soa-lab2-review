package ru.itmo.soa.hr.dto;

import java.time.LocalDate;

public record WorkerDto(
        Long id,
        String name,
        CoordinatesDto coordinates,
        LocalDate creationDate,
        Long salary,
        String position,
        String status,
        PersonDto person
) {
}
