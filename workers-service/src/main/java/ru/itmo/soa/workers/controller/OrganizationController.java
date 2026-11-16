package ru.itmo.soa.workers.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import ru.itmo.soa.workers.controller.WorkerController.JsonSupport;
import ru.itmo.soa.workers.service.OrganizationService;
import ru.itmo.soa.workers.dto.WorkerSearchRequestDto;
import ru.itmo.soa.workers.validation.Validation;

@Path("/organizations")
@Produces(MediaType.APPLICATION_JSON)
public final class OrganizationController {

    @Inject
    private OrganizationService service;

    @GET
    public Response list() {
        return JsonSupport.response(200, service.list());
    }

    @GET
    @Path("/{organizationId}/workers")
    public Response workers(
            @PathParam("organizationId") String organizationId,
            @Context UriInfo uri
    ) {
        long id = Validation.pathInteger(
                organizationId,
                1,
                Long.MAX_VALUE,
                400,
                "organizationId"
        );
        WorkerSearchRequestDto filter = WorkerSearchController.query(uri.getQueryParameters());

        return JsonSupport.response(200, service.workers(id, filter));
    }

    @PUT
    @Path("/{organizationId}/workers/{workerId}")
    public Response assign(
            @PathParam("organizationId") String organizationId,
            @PathParam("workerId") String workerId
    ) {
        long parsedOrganizationId = Validation.pathInteger(
                organizationId,
                1,
                Long.MAX_VALUE,
                400,
                "organizationId"
        );
        long parsedWorkerId = Validation.pathInteger(
                workerId,
                1,
                Validation.POSTGRES_INTEGER_MAX,
                400,
                "workerId"
        );

        service.assign(parsedOrganizationId, parsedWorkerId);
        return Response.noContent().build();
    }
}
