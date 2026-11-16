package ru.itmo.soa.workers.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import ru.itmo.soa.workers.controller.WorkerController.JsonSupport;
import ru.itmo.soa.workers.dto.WorkerSearchRequestDto;
import ru.itmo.soa.workers.exception.ApiException;
import ru.itmo.soa.workers.service.WorkerSearchService;
import ru.itmo.soa.workers.validation.Validation;
import java.util.List;
import java.util.Set;

@Path("/workers")
@Produces(MediaType.APPLICATION_JSON)
public final class WorkerSearchController {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);

    @Inject
    private WorkerSearchService service;

    @GET
    public Response list(@Context UriInfo uri) {
        WorkerSearchRequestDto filter = query(uri.getQueryParameters());
        return JsonSupport.response(200, service.search(filter));
    }

    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response search(String body) {
        WorkerSearchRequestDto filter = Validation.search(JsonSupport.parse(body, 400));
        return JsonSupport.response(200, service.search(filter));
    }

    public static WorkerSearchRequestDto query(MultivaluedMap<String, String> query) {
        ObjectNode search = MAPPER.createObjectNode();

        if (query.containsKey("search")) {
            Validation.require(
                    query.get("search").size() == 1,
                    400,
                    "Repeated query parameter: search"
            );
            search = parseSearch(query.getFirst("search"));
        }

        query.forEach((key, values) -> Validation.require(
                Set.of("search", "page", "size").contains(key),
                400,
                "Unknown query parameter: " + key
        ));

        overrideInteger(query, search, "page", 1, Integer.MAX_VALUE);
        overrideInteger(query, search, "size", 1, Validation.MAX_PAGE_SIZE);
        return Validation.search(search);
    }

    private static ObjectNode parseSearch(String value) {
        try {
            JsonNode parsed = MAPPER.readTree(value);
            Validation.require(
                    parsed != null && parsed.isObject(),
                    400,
                    "search must be a JSON object"
            );
            return (ObjectNode) parsed;
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiException(400, "Malformed search JSON");
        }
    }

    private static void overrideInteger(
            MultivaluedMap<String, String> query,
            ObjectNode search,
            String parameter,
            long minimum,
            long maximum
    ) {
        if (!query.containsKey(parameter)) {
            return;
        }

        List<String> values = query.get(parameter);
        Validation.require(
                values.size() == 1,
                400,
                "Repeated query parameter: " + parameter
        );
        search.put(
                parameter,
                Validation.pathInteger(
                        values.get(0),
                        minimum,
                        maximum,
                        400,
                        parameter
                )
        );
    }
}
