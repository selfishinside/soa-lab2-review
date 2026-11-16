package ru.itmo.soa.workers.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ru.itmo.soa.workers.dto.CoordinatesDto;
import ru.itmo.soa.workers.dto.PersonDto;
import ru.itmo.soa.workers.dto.WorkerRequestDto;
import ru.itmo.soa.workers.dto.WorkerSearchRequestDto;
import ru.itmo.soa.workers.exception.ApiException;
import ru.itmo.soa.workers.model.Color;
import ru.itmo.soa.workers.model.Country;
import ru.itmo.soa.workers.model.Position;
import ru.itmo.soa.workers.model.Status;

public final class Validation {

    public static final int INVALID_JSON_STATUS = 400;
    public static final int INVALID_ENTITY_STATUS = 422;
    public static final long POSTGRES_INTEGER_MIN = Integer.MIN_VALUE;
    public static final long POSTGRES_INTEGER_MAX = Integer.MAX_VALUE;
    public static final int POSTGRES_VARCHAR_LIMIT = 255;
    public static final int MAX_PAGE_SIZE = 100;

    private static final Set<String> WORKER_FIELDS = Set.of(
            "id",
            "name",
            "coordinates",
            "creationDate",
            "salary",
            "position",
            "status",
            "person"
    );

    private static final Set<String> COORDINATE_FIELDS = Set.of("x", "y");
    private static final Set<String> PERSON_FIELDS = Set.of(
            "passportID",
            "eyeColor",
            "nationality"
    );

    private static final Set<String> SEARCH_FIELDS = Set.of(
            "id",
            "name",
            "salaryMin",
            "salaryMax",
            "coordinatesXMin",
            "coordinatesXMax",
            "coordinatesYMin",
            "coordinatesYMax",
            "position",
            "status",
            "personPassportID",
            "personEyeColor",
            "personNationality",
            "creationDateFrom",
            "creationDateTo",
            "page",
            "size",
            "sort"
    );

    private static final Set<String> SORT_FIELDS = Set.of(
            "id",
            "name",
            "coordinates.x",
            "coordinates.y",
            "creationDate",
            "salary",
            "position",
            "status",
            "person.passportID",
            "person.eyeColor",
            "person.nationality"
    );

    private Validation() {
    }

    public static WorkerRequestDto worker(ObjectNode input) {
        int status = INVALID_ENTITY_STATUS;
        fields(input, WORKER_FIELDS, status);

        String name = requiredText(input.get("name"), status, "name");
        require(!name.isBlank(), status, "name must not be empty");
        requireLength(name, POSTGRES_VARCHAR_LIMIT, status, "name");

        ObjectNode coordinates = requiredObject(
                input.get("coordinates"),
                status,
                "coordinates"
        );
        fields(coordinates, COORDINATE_FIELDS, status);
        long x = integer(
                coordinates.get("x"),
                POSTGRES_INTEGER_MIN,
                225,
                status,
                "coordinates.x"
        );
        float y = finiteFloat(coordinates.get("y"), status, "coordinates.y");

        long salary = integer(
                input.get("salary"),
                1,
                POSTGRES_INTEGER_MAX,
                status,
                "salary"
        );

        Position position = optionalEnum(
                input,
                "position",
                Position.class,
                status
        );
        Status workerStatus = optionalEnum(
                input,
                "status",
                Status.class,
                status
        );
        PersonDto person = person(input.get("person"), status);

        return new WorkerRequestDto(
                name,
                new CoordinatesDto(x, y),
                salary,
                position,
                workerStatus,
                person
        );
    }

    public static WorkerSearchRequestDto search(ObjectNode input) {
        int status = INVALID_JSON_STATUS;
        fields(input, SEARCH_FIELDS, status);
        rejectExplicitNulls(input, status);

        Long id = optionalInteger(input, "id", 1, POSTGRES_INTEGER_MAX, status);
        String name = optionalText(input, "name", status);
        requireOptionalLength(name, POSTGRES_VARCHAR_LIMIT, status, "name");

        Long salaryMin = optionalInteger(
                input,
                "salaryMin",
                1,
                POSTGRES_INTEGER_MAX,
                status
        );
        Long salaryMax = optionalInteger(
                input,
                "salaryMax",
                1,
                POSTGRES_INTEGER_MAX,
                status
        );
        requireRange(salaryMin, salaryMax, status, "salaryMin must not exceed salaryMax");

        Long xMin = optionalInteger(
                input,
                "coordinatesXMin",
                POSTGRES_INTEGER_MIN,
                225,
                status
        );
        Long xMax = optionalInteger(
                input,
                "coordinatesXMax",
                POSTGRES_INTEGER_MIN,
                225,
                status
        );
        requireRange(xMin, xMax, status, "coordinatesXMin must not exceed coordinatesXMax");

        Float yMin = optionalFloat(input, "coordinatesYMin", status);
        Float yMax = optionalFloat(input, "coordinatesYMax", status);
        requireRange(yMin, yMax, status, "coordinatesYMin must not exceed coordinatesYMax");

        String passport = optionalText(input, "personPassportID", status);
        requireOptionalLength(
                passport,
                POSTGRES_VARCHAR_LIMIT,
                status,
                "personPassportID"
        );

        LocalDate dateFrom = optionalDate(input, "creationDateFrom", status);
        LocalDate dateTo = optionalDate(input, "creationDateTo", status);
        requireRange(dateFrom, dateTo, status, "creationDateFrom must not exceed creationDateTo");

        Integer page = optionalInt(input, "page", 1, Integer.MAX_VALUE, status);
        Integer size = optionalInt(input, "size", 1, MAX_PAGE_SIZE, status);

        return new WorkerSearchRequestDto(
                id,
                name,
                salaryMin,
                salaryMax,
                xMin,
                xMax,
                yMin,
                yMax,
                enumArray(input, "position", Position.class, status),
                enumArray(input, "status", Status.class, status),
                passport,
                enumArray(input, "personEyeColor", Color.class, status),
                enumArray(input, "personNationality", Country.class, status),
                dateFrom,
                dateTo,
                page,
                size,
                sort(input, status)
        );
    }

    public static long pathInteger(
            String value,
            long minimum,
            long maximum,
            int status,
            String name
    ) {
        require(
                value != null && value.matches("-?[0-9]+"),
                status,
                name + " must be an integer"
        );
        try {
            long number = Long.parseLong(value);
            require(
                    number >= minimum && number <= maximum,
                    status,
                    name + " is out of range"
            );
            return number;
        } catch (NumberFormatException exception) {
            throw new ApiException(status, name + " is out of range");
        }
    }

    public static BigDecimal positiveCoefficient(String value, int status) {
        require(
                value != null && !value.isBlank() && value.length() <= 64,
                status,
                "Invalid coefficient"
        );
        try {
            BigDecimal coefficient = new BigDecimal(value);
            double doubleValue = coefficient.doubleValue();
            require(
                    coefficient.signum() > 0
                            && Double.isFinite(doubleValue)
                            && doubleValue > 0,
                    status,
                    "Invalid coefficient"
            );
            return coefficient;
        } catch (NumberFormatException exception) {
            throw new ApiException(status, "Invalid coefficient");
        }
    }

    public static void require(boolean condition, int status, String message) {
        if (!condition) {
            throw new ApiException(status, message);
        }
    }

    public static void fields(ObjectNode node, Set<String> allowed, int status) {
        node.fieldNames().forEachRemaining(name -> require(
                allowed.contains(name),
                status,
                "Unknown field: " + name
        ));
    }

    public static long integer(
            JsonNode value,
            long minimum,
            long maximum,
            int status,
            String name
    ) {
        require(
                value != null && value.isIntegralNumber() && value.canConvertToLong(),
                status,
                name + " must be an integer"
        );
        long number = value.longValue();
        require(
                number >= minimum && number <= maximum,
                status,
                name + " is out of range"
        );
        return number;
    }

    private static PersonDto person(JsonNode value, int status) {
        if (value == null || value.isNull()) {
            return null;
        }

        ObjectNode person = requiredObject(value, status, "person");
        fields(person, PERSON_FIELDS, status);

        String passport = requiredText(person.get("passportID"), status, "person.passportID");
        requireLength(passport, POSTGRES_VARCHAR_LIMIT, status, "person.passportID");

        return new PersonDto(
                passport,
                optionalEnum(person, "eyeColor", Color.class, status),
                optionalEnum(person, "nationality", Country.class, status)
        );
    }

    private static ObjectNode requiredObject(JsonNode value, int status, String name) {
        require(value != null && value.isObject(), status, name + " must be an object");
        return (ObjectNode) value;
    }

    private static String requiredText(JsonNode value, int status, String name) {
        require(value != null && value.isTextual(), status, name + " must be a string");
        return value.textValue();
    }

    private static String optionalText(ObjectNode input, String field, int status) {
        if (!input.has(field)) {
            return null;
        }
        return requiredText(input.get(field), status, field);
    }

    private static void requireLength(
            String value,
            int maximum,
            int status,
            String name
    ) {
        int length = value.codePointCount(0, value.length());
        require(length <= maximum, status, name + " must contain at most " + maximum + " characters");
    }

    private static void requireOptionalLength(
            String value,
            int maximum,
            int status,
            String name
    ) {
        if (value != null) {
            requireLength(value, maximum, status, name);
        }
    }

    private static float finiteFloat(JsonNode value, int status, String name) {
        require(value != null && value.isNumber(), status, name + " must be a number");
        BigDecimal decimal = value.decimalValue();
        float number = decimal.floatValue();
        require(
                Float.isFinite(number)
                        && (decimal.signum() == 0 || number != 0.0f),
                status,
                name + " must be a finite float"
        );
        return number;
    }

    private static Float optionalFloat(ObjectNode input, String field, int status) {
        return input.has(field) ? finiteFloat(input.get(field), status, field) : null;
    }

    private static Long optionalInteger(
            ObjectNode input,
            String field,
            long minimum,
            long maximum,
            int status
    ) {
        return input.has(field)
                ? integer(input.get(field), minimum, maximum, status, field)
                : null;
    }

    private static Integer optionalInt(
            ObjectNode input,
            String field,
            int minimum,
            int maximum,
            int status
    ) {
        Long value = optionalInteger(input, field, minimum, maximum, status);
        return value == null ? null : value.intValue();
    }

    private static LocalDate optionalDate(ObjectNode input, String field, int status) {
        if (!input.has(field)) {
            return null;
        }

        String value = requiredText(input.get(field), status, field);
        require(
                value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"),
                status,
                field + " must use YYYY-MM-DD"
        );
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new ApiException(status, field + " is not a valid date");
        }
    }

    private static <E extends Enum<E>> E optionalEnum(
            ObjectNode input,
            String field,
            Class<E> type,
            int status
    ) {
        if (!input.has(field) || input.get(field).isNull()) {
            return null;
        }
        return enumValue(input.get(field), field, type, status);
    }

    private static <E extends Enum<E>> E enumValue(
            JsonNode value,
            String field,
            Class<E> type,
            int status
    ) {
        require(value != null && value.isTextual(), status, field + " must be a string");
        try {
            return Enum.valueOf(type, value.textValue());
        } catch (IllegalArgumentException exception) {
            throw new ApiException(status, "Invalid value for " + field);
        }
    }

    private static <E extends Enum<E>> List<E> enumArray(
            ObjectNode input,
            String field,
            Class<E> type,
            int status
    ) {
        if (!input.has(field)) {
            return null;
        }

        JsonNode value = input.get(field);
        require(value.isArray(), status, field + " must be an array");
        List<E> result = new ArrayList<>();
        value.forEach(item -> result.add(enumValue(item, field, type, status)));
        return List.copyOf(result);
    }

    private static List<String> sort(ObjectNode input, int status) {
        if (!input.has("sort")) {
            return null;
        }

        JsonNode value = input.get("sort");
        require(value.isArray(), status, "sort must be an array");

        List<String> result = new ArrayList<>();
        Set<String> usedFields = new HashSet<>();
        value.forEach(item -> {
            String specification = requiredText(item, status, "sort entry");
            String field = specification.startsWith("-")
                    ? specification.substring(1)
                    : specification;
            require(SORT_FIELDS.contains(field), status, "Invalid sort field: " + specification);
            require(usedFields.add(field), status, "Repeated sort field: " + field);
            result.add(specification);
        });
        return List.copyOf(result);
    }

    private static void rejectExplicitNulls(ObjectNode input, int status) {
        input.fields().forEachRemaining(entry -> require(
                !entry.getValue().isNull(),
                status,
                entry.getKey() + " must not be null"
        ));
    }

    private static <T extends Comparable<T>> void requireRange(
            T minimum,
            T maximum,
            int status,
            String message
    ) {
        if (minimum != null && maximum != null) {
            require(minimum.compareTo(maximum) <= 0, status, message);
        }
    }
}
