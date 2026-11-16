package ru.itmo.soa.workers.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public final class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final List<String> FORWARDED_HEADERS = List.of(
            "Allow",
            "WWW-Authenticate",
            "Retry-After"
    );

    @Override
    public Response toResponse(Throwable exception) {
        int status = status(exception);
        String message = message(exception, status);

        Response.ResponseBuilder response = Response
                .status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(serialize(new ErrorBody(status, message)));

        copyHeaders(exception, response);
        return response.build();
    }

    private static int status(Throwable exception) {
        if (exception instanceof ApiException apiException) {
            return apiException.getStatus();
        }
        if (exception instanceof WebApplicationException webException) {
            return webException.getResponse().getStatus();
        }

        LOG.log(Level.SEVERE, "Unhandled workers service error", exception);
        return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
    }

    private static String message(Throwable exception, int status) {
        if (exception instanceof ApiException apiException) {
            return apiException.getMessage();
        }
        if (exception instanceof WebApplicationException webException) {
            return webException.getResponse().getStatusInfo().getReasonPhrase();
        }
        return status == 500 ? "Internal server error" : "Request failed";
    }

    private static void copyHeaders(
            Throwable exception,
            Response.ResponseBuilder response
    ) {
        if (!(exception instanceof WebApplicationException webException)) {
            return;
        }

        FORWARDED_HEADERS.forEach(header -> {
            Object value = webException.getResponse().getHeaders().getFirst(header);
            if (value != null) {
                response.header(header, value);
            }
        });
    }

    private static String serialize(ErrorBody error) {
        try {
            return MAPPER.writeValueAsString(error);
        } catch (JsonProcessingException exception) {
            return "{\"status\":500,\"message\":\"Internal server error\"}";
        }
    }

    private record ErrorBody(int status, String message) {
    }
}
