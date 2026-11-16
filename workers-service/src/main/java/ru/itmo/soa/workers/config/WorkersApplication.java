package ru.itmo.soa.workers.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.Set;
import ru.itmo.soa.workers.controller.IndexController;
import ru.itmo.soa.workers.controller.OrganizationController;
import ru.itmo.soa.workers.controller.WorkerController;
import ru.itmo.soa.workers.controller.WorkerSearchController;
import ru.itmo.soa.workers.controller.WorkerStatisticsController;
import ru.itmo.soa.workers.exception.GlobalExceptionMapper;

@ApplicationPath("/api/v1")
public final class WorkersApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
                WorkerController.class,
                WorkerSearchController.class,
                WorkerStatisticsController.class,
                OrganizationController.class,
                IndexController.class,
                GlobalExceptionMapper.class
        );
    }
}
