package ru.itmo.soa.workers.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import ru.itmo.soa.workers.controller.WorkerController.JsonSupport;
import ru.itmo.soa.workers.service.WorkerStatisticsService;
import ru.itmo.soa.workers.validation.Validation;

@Path("/workers")
@Produces(MediaType.APPLICATION_JSON)
public final class WorkerStatisticsController {

    @Inject
    private WorkerStatisticsService service;

    @GET
    @Path("/salary/average")
    public Response averageSalary() {
        BigDecimal average = service.averageSalary();
        if (average == null) {
            return Response.noContent().build();
        }
        return JsonSupport.response(200, average);
    }

    @GET
    @Path("/position/max")
    public Response workerWithMaxPosition() {
        return JsonSupport.response(200, service.workerWithMaxPosition());
    }

    @GET
    @Path("/salary/below/{salary}/number")
    public Response countWithSalaryBelow(@PathParam("salary") String salary) {
        long value = Validation.pathInteger(
                salary,
                Validation.POSTGRES_INTEGER_MIN,
                Validation.POSTGRES_INTEGER_MAX,
                400,
                "salary"
        );
        return JsonSupport.response(200, service.countWithSalaryBelow(value));
    }
}
