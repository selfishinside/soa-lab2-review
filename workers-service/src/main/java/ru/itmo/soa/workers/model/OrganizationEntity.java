package ru.itmo.soa.workers.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "organizations")
public class OrganizationEntity {
    @Id private Long id;

    public OrganizationEntity() { }
    public OrganizationEntity(Long id) { this.id = id; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}
