package ru.itmo.soa.workers.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.itmo.soa.workers.controller.WorkerController.JsonSupport;
import ru.itmo.soa.workers.dto.IndexResultDto;
import ru.itmo.soa.workers.service.IndexService;
import ru.itmo.soa.workers.validation.Validation;

@Path("/internal/organizations")
@Produces(MediaType.APPLICATION_JSON)
public final class IndexController {

    @Inject
    private IndexService service;

    @POST
    @Path("/{organizationId}/index/{coefficient}")
    public Response index(
            @PathParam("organizationId") String organizationId,
            @PathParam("coefficient") String coefficient
    ) {
        long id = Validation.pathInteger(
                organizationId,
                1,
                Long.MAX_VALUE,
                400,
                "organizationId"
        );
        IndexResultDto result = service.index(id, coefficient);

        if (result.updatedCount() == 0) {
            return Response.noContent().build();
        }
        return JsonSupport.response(200, result);
    }
}
