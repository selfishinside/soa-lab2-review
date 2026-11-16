package ru.itmo.soa.workers.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import ru.itmo.soa.workers.dto.PageResponseDto;
import ru.itmo.soa.workers.dto.WorkerResponseDto;
import ru.itmo.soa.workers.dto.WorkerSearchRequestDto;
import ru.itmo.soa.workers.repository.WorkerRepository;

@ApplicationScoped
@Transactional(Transactional.TxType.SUPPORTS)
public class WorkerSearchService {

    @Inject
    private WorkerRepository repository;

    public PageResponseDto<WorkerResponseDto> search(WorkerSearchRequestDto criteria) {
        List<WorkerResponseDto> workers = repository.findAll().stream()
                .map(WorkerService::toDto)
                .toList();
        return apply(criteria, workers);
    }

    public static PageResponseDto<WorkerResponseDto> apply(
            WorkerSearchRequestDto criteria,
            List<WorkerResponseDto> workers
    ) {
        int page = criteria.page() == null ? 1 : criteria.page();
        int size = criteria.size() == null ? 20 : criteria.size();

        List<WorkerResponseDto> matches = workers.stream()
                .filter(worker -> matches(worker, criteria))
                .sorted(comparator(criteria.sort()))
                .toList();

        long offset = ((long) page - 1) * size;
        List<WorkerResponseDto> content = matches.stream()
                .skip(offset)
                .limit(size)
                .toList();

        long totalElements = matches.size();
        long totalPages = (totalElements + size - 1) / size;
        return new PageResponseDto<>(
                page,
                size,
                totalElements,
                totalPages,
                content
        );
    }

    private static boolean matches(
            WorkerResponseDto worker,
            WorkerSearchRequestDto filter
    ) {
        if (filter.id() != null && !filter.id().equals(worker.id())) {
            return false;
        }
        if (filter.name() != null && !worker.name().contains(filter.name())) {
            return false;
        }
        if (!inside(worker.salary(), filter.salaryMin(), filter.salaryMax())) {
            return false;
        }
        if (!inside(worker.coordinates().x(), filter.coordinatesXMin(), filter.coordinatesXMax())) {
            return false;
        }
        if (!inside(worker.coordinates().y(), filter.coordinatesYMin(), filter.coordinatesYMax())) {
            return false;
        }
        if (!inside(worker.creationDate(), filter.creationDateFrom(), filter.creationDateTo())) {
            return false;
        }
        if (!matchesArray(worker.position(), filter.position())) {
            return false;
        }
        if (!matchesArray(worker.status(), filter.status())) {
            return false;
        }

        if (filter.personPassportID() != null
                && (worker.person() == null
                || !filter.personPassportID().equals(worker.person().passportID()))) {
            return false;
        }
        if (!matchesArray(
                worker.person() == null ? null : worker.person().eyeColor(),
                filter.personEyeColor()
        )) {
            return false;
        }
        return matchesArray(
                worker.person() == null ? null : worker.person().nationality(),
                filter.personNationality()
        );
    }

    private static <T extends Comparable<T>> boolean inside(T value, T minimum, T maximum) {
        return (minimum == null || value.compareTo(minimum) >= 0)
                && (maximum == null || value.compareTo(maximum) <= 0);
    }

    private static <T> boolean matchesArray(T value, List<T> allowed) {
        return allowed == null || allowed.isEmpty() || allowed.contains(value);
    }

    private static Comparator<WorkerResponseDto> comparator(List<String> sort) {
        return (first, second) -> {
            if (sort != null) {
                for (String specification : sort) {
                    int result = compare(first, second, specification);
                    if (result != 0) {
                        return result;
                    }
                }
            }
            return first.id().compareTo(second.id());
        };
    }

    private static int compare(
            WorkerResponseDto first,
            WorkerResponseDto second,
            String specification
    ) {
        boolean descending = specification.startsWith("-");
        String field = descending ? specification.substring(1) : specification;

        Comparable<?> firstValue = fieldValue(first, field);
        Comparable<?> secondValue = fieldValue(second, field);
        int result = compareNullable(firstValue, secondValue);
        return descending && firstValue != null && secondValue != null ? -result : result;
    }

    private static Comparable<?> fieldValue(WorkerResponseDto worker, String field) {
        return switch (field) {
            case "id" -> worker.id();
            case "name" -> worker.name();
            case "coordinates.x" -> worker.coordinates().x();
            case "coordinates.y" -> worker.coordinates().y();
            case "creationDate" -> worker.creationDate();
            case "salary" -> worker.salary();
            case "position" -> worker.position();
            case "status" -> worker.status();
            case "person.passportID" -> worker.person() == null
                    ? null
                    : worker.person().passportID();
            case "person.eyeColor" -> worker.person() == null
                    ? null
                    : worker.person().eyeColor();
            case "person.nationality" -> worker.person() == null
                    ? null
                    : worker.person().nationality();
            default -> throw new IllegalStateException("Unsupported sort field: " + field);
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static int compareNullable(Comparable first, Comparable second) {
        if (first == null || second == null) {
            if (first == second) {
                return 0;
            }
            return first == null ? 1 : -1;
        }
        return first.compareTo(second);
    }
}
