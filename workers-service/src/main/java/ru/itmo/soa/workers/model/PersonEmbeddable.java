package ru.itmo.soa.workers.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class PersonEmbeddable {

    @NotNull
    @Column(length = 255)
    private String passportID;

    @Enumerated(EnumType.STRING)
    private Color eyeColor;

    @Enumerated(EnumType.STRING)
    private Country nationality;

    public PersonEmbeddable() {
    }
    public PersonEmbeddable(String passportID, Color eyeColor, Country nationality) {
        this.passportID = passportID;
        this.eyeColor = eyeColor;
        this.nationality = nationality;
    }

    public String getPassportID() {
        return passportID;
    }

    public void setPassportID(String passportID) {
        this.passportID = passportID;
    }

    public Color getEyeColor() {
        return eyeColor;
    }

    public void setEyeColor(Color eyeColor) {
        this.eyeColor = eyeColor;
    }

    public Country getNationality() {
        return nationality;
    }

    public void setNationality(Country nationality) {
        this.nationality = nationality;
    }
}
