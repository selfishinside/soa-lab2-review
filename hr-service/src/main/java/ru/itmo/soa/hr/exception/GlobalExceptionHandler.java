package ru.itmo.soa.hr.exception;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public final class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class.getName());

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorBody> handleApiException(ApiException exception) {
        return response(exception.getStatus(), exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorBody> handleUnexpectedException(Exception exception) {
        LOG.log(Level.SEVERE, "Unhandled HR service error", exception);
        return response(500, "Internal server error");
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception,
            Object body,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        HttpStatus knownStatus = HttpStatus.resolve(status.value());
        String message = knownStatus == null
                ? "Request failed"
                : knownStatus.getReasonPhrase();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.putAll(headers);
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(
                new ErrorBody(status.value(), message),
                responseHeaders,
                status
        );
    }

    private static ResponseEntity<ErrorBody> response(int status, String message) {
        return ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorBody(status, message));
    }

    public record ErrorBody(int status, String message) {
    }
}
