package ru.itmo.soa.workers.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class CoordinatesEmbeddable {

    @NotNull
    @Max(225)
    @Column(nullable = false)
    private Long x;

    @Column(nullable = false)
    @NotNull
    private Float y;

    public CoordinatesEmbeddable() {
    }

    public CoordinatesEmbeddable(Long x, Float y) {
        this.x = x;
        this.y = y;
    }

    public Long getX() {
        return x;
    }

    public void setX(Long x) {
        this.x = x;
    }

    public Float getY() {
        return y;
    }

    public void setY(Float y) {
        this.y = y;
    }
}
