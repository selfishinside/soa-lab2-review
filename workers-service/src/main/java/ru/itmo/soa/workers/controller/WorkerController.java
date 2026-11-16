package ru.itmo.soa.workers.controller;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import ru.itmo.soa.workers.exception.ApiException;
import ru.itmo.soa.workers.dto.WorkerRequestDto;
import ru.itmo.soa.workers.dto.WorkerResponseDto;
import ru.itmo.soa.workers.service.WorkerService;
import ru.itmo.soa.workers.validation.Validation;

@Path("/workers")
@Produces(MediaType.APPLICATION_JSON)
public final class WorkerController {

    @Inject
    private WorkerService service;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(String body, @Context UriInfo uri) {
        WorkerRequestDto request = Validation.worker(JsonSupport.parse(body, 422));
        WorkerResponseDto worker = service.create(request);

        return Response
                .fromResponse(JsonSupport.response(201, worker))
                .location(uri.getAbsolutePathBuilder().path(worker.id().toString()).build())
                .build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") String id) {
        return JsonSupport.response(200, service.get(id(id)));
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") String id, String body) {
        WorkerRequestDto request = Validation.worker(JsonSupport.parse(body, 422));
        WorkerResponseDto worker = service.update(
                id(id),
                request
        );
        return JsonSupport.response(200, worker);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        service.delete(id(id));
        return Response.noContent().build();
    }

    private static long id(String value) {
        return Validation.pathInteger(
                value,
                1,
                Validation.POSTGRES_INTEGER_MAX,
                400,
                "id"
        );
    }

    public static final class JsonSupport {

        private static final ObjectMapper MAPPER = new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);

        private JsonSupport() {
        }

        public static ObjectNode parse(String body, int shapeStatus) {
            if (body == null || body.isBlank()) {
                throw new ApiException(400, "Missing JSON body");
            }

            try {
                JsonNode node = MAPPER.readTree(body);
                if (node == null || !node.isObject()) {
                    throw new ApiException(shapeStatus, "Expected a JSON object");
                }
                return (ObjectNode) node;
            } catch (ApiException error) {
                throw error;
            } catch (JsonProcessingException error) {
                throw new ApiException(400, "Malformed JSON");
            }
        }

        public static Response response(int status, Object value) {
            try {
                return Response
                        .status(status)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .entity(MAPPER.writeValueAsString(value))
                        .build();
            } catch (JsonProcessingException error) {
                throw new IllegalStateException("Cannot serialize response", error);
            }
        }

    }
}
