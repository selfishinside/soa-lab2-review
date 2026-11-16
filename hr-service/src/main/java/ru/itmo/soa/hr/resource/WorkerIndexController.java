package ru.itmo.soa.hr.resource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.soa.hr.dto.WorkerDto;
import ru.itmo.soa.hr.service.WorkerIndexService;

@RestController
public final class WorkerIndexController {

    private final WorkerIndexService service;

    public WorkerIndexController(WorkerIndexService service) {
        this.service = service;
    }

    @PostMapping("/index/{worker-id}/{coeff}")
    public ResponseEntity<WorkerDto> index(
            @PathVariable("worker-id") String workerId,
            @PathVariable("coeff") String coefficient
    ) {
        return ResponseEntity.ok(service.index(workerId, coefficient));
    }
}
