package ru.itmo.soa.workers.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "workers")
public class WorkerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String name;

    @NotNull
    @Embedded
    private CoordinatesEmbeddable coordinates;

    @NotNull
    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date creationDate;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Long salary;

    @Enumerated(EnumType.STRING)
    private Position position;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Embedded
    private PersonEmbeddable person;

    public WorkerEntity() {
    }

    public WorkerEntity(Long id, String name, CoordinatesEmbeddable coordinates, Date creationDate,
                        Long salary, Position position, Status status, PersonEmbeddable person) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.salary = salary;
        this.position = position;
        this.status = status;
        this.person = person;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CoordinatesEmbeddable getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(CoordinatesEmbeddable coordinates) {
        this.coordinates = coordinates;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Long getSalary() {
        return salary;
    }

    public void setSalary(Long salary) {
        this.salary = salary;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public PersonEmbeddable getPerson() {
        return person;
    }

    public void setPerson(PersonEmbeddable person) {
        this.person = person;
    }
}
