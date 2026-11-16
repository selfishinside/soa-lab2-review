package ru.itmo.soa.workers.dto;

import ru.itmo.soa.workers.model.Color;
import ru.itmo.soa.workers.model.Country;

public record PersonDto(
        String passportID,
        Color eyeColor,
        Country nationality
) {
}
