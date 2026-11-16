package ru.itmo.soa.workers.dto;

import java.time.LocalDate;
import java.util.List;
import ru.itmo.soa.workers.model.Color;
import ru.itmo.soa.workers.model.Country;
import ru.itmo.soa.workers.model.Position;
import ru.itmo.soa.workers.model.Status;

public record WorkerSearchRequestDto(
        Long id,
        String name,
        Long salaryMin,
        Long salaryMax,
        Long coordinatesXMin,
        Long coordinatesXMax,
        Float coordinatesYMin,
        Float coordinatesYMax,
        List<Position> position,
        List<Status> status,
        String personPassportID,
        List<Color> personEyeColor,
        List<Country> personNationality,
        LocalDate creationDateFrom,
        LocalDate creationDateTo,
        Integer page,
        Integer size,
        List<String> sort
) {
}
