package ru.itmo.soa.workers.dto;

import ru.itmo.soa.workers.model.Position;
import ru.itmo.soa.workers.model.Status;

public record WorkerRequestDto (
        String name,
        CoordinatesDto coordinates,
        Long salary,
        Position position,
        Status status,
        PersonDto person
) {
}
