package ru.itmo.soa.hr.dto;

public record WorkerUpdateDto(
        String name,
        CoordinatesDto coordinates,
        Long salary,
        String position,
        String status,
        PersonDto person
) {
}
