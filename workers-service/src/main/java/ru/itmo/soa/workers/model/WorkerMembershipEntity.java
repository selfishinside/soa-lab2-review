package ru.itmo.soa.workers.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "worker_memberships")
public class WorkerMembershipEntity {
    @Id private Long workerId;
    @Column(nullable = false) private Long organizationId;

    public WorkerMembershipEntity() { }
    public WorkerMembershipEntity(Long workerId, Long organizationId) {
        this.workerId = workerId;
        this.organizationId = organizationId;
    }
    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }
    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
}
