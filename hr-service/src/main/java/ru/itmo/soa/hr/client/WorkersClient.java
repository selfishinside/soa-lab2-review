package ru.itmo.soa.hr.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.function.Supplier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import ru.itmo.soa.hr.dto.IndexResultDto;
import ru.itmo.soa.hr.dto.WorkerDto;
import ru.itmo.soa.hr.dto.WorkerUpdateDto;
import ru.itmo.soa.hr.exception.ApiException;

public final class WorkersClient {

    private final RestClient client;
    private final String baseUrl;

    public WorkersClient(RestClient client, URI baseUri) {
        this.client = client;
        this.baseUrl = baseUri.toString().replaceAll("/+$", "");
    }

    public WorkerDto indexWorker(long workerId, BigDecimal coefficient) {
        String path = "/workers/" + workerId;
        WorkerDto worker = call(() -> client
                .get()
                .uri(baseUrl + path)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(WorkerDto.class));

        if (worker == null || worker.salary() == null || worker.salary() <= 0) {
            throw unavailable();
        }

        WorkerUpdateDto update = new WorkerUpdateDto(
                worker.name(),
                worker.coordinates(),
                indexedSalary(worker.salary(), coefficient),
                worker.position(),
                worker.status(),
                worker.person()
        );

        WorkerDto updatedWorker = call(() -> client
                .put()
                .uri(baseUrl + path)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(update)
                .retrieve()
                .body(WorkerDto.class));

        if (updatedWorker == null) {
            throw unavailable();
        }
        return updatedWorker;
    }

    public IndexResultDto indexOrganization(
            long organizationId,
            BigDecimal coefficient
    ) {
        ResponseEntity<IndexResultDto> response = call(() -> client
                .post()
                .uri(baseUrl + "/internal/organizations/"
                        + organizationId
                        + "/index/"
                        + coefficient)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(IndexResultDto.class));

        if (response.getStatusCode().value() == 204) {
            return null;
        }
        if (response.getStatusCode().value() != 200 || response.getBody() == null) {
            throw unavailable();
        }
        return response.getBody();
    }

    private static long indexedSalary(long salary, BigDecimal coefficient) {
        try {
            BigDecimal result = BigDecimal.valueOf(salary).multiply(coefficient);
            if (result.compareTo(new BigDecimal("0.5")) < 0
                    || result.compareTo(new BigDecimal("2147483647.5")) >= 0) {
                throw new ArithmeticException();
            }
            return result.setScale(0, RoundingMode.HALF_UP).longValueExact();
        } catch (ArithmeticException exception) {
            throw new ApiException(
                    422,
                    "Indexed salary must be between 1 and 2147483647"
            );
        }
    }

    private static <T> T call(Supplier<T> request) {
        try {
            return request.get();
        } catch (RestClientResponseException exception) {
            int status = exception.getStatusCode().value();
            if (status == 404 || status == 422) {
                throw new ApiException(status, errorMessage(exception, status));
            }
            throw unavailable();
        } catch (RestClientException exception) {
            throw unavailable();
        }
    }

    private static String errorMessage(
            RestClientResponseException exception,
            int status
    ) {
        try {
            ErrorBody error = exception.getResponseBodyAs(ErrorBody.class);
            if (error != null && error.message() != null && !error.message().isBlank()) {
                return error.message();
            }
        } catch (Exception ignored) {
        }
        return status == 404
                ? "Worker or organization not found"
                : "Indexed salary violates constraints";
    }

    private static ApiException unavailable() {
        return new ApiException(
                503,
                "Workers service unavailable or returned an invalid response"
        );
    }

    private record ErrorBody(int status, String message) {
    }
}
