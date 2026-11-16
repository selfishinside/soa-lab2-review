package ru.itmo.soa.hr.resource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.soa.hr.dto.IndexResultDto;
import ru.itmo.soa.hr.service.OrganizationIndexService;

@RestController
public final class OrganizationIndexController {

    private final OrganizationIndexService service;

    public OrganizationIndexController(OrganizationIndexService service) {
        this.service = service;
    }

    @PostMapping("/index/all/{org-id}/{coeff}")
    public ResponseEntity<IndexResultDto> index(
            @PathVariable("org-id") String organizationId,
            @PathVariable("coeff") String coefficient
    ) {
        IndexResultDto result = service.index(organizationId, coefficient);
        if (result == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }
}
