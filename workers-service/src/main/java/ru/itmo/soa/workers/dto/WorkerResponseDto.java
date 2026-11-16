package ru.itmo.soa.workers.dto;

import java.time.LocalDate;
import ru.itmo.soa.workers.model.Position;
import ru.itmo.soa.workers.model.Status;

public record WorkerResponseDto(
        Long id,
        String name,
        CoordinatesDto coordinates,
        LocalDate creationDate,
        Long salary,
        Position position,
        Status status,
        PersonDto person
) {
}
