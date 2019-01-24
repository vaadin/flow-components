package com.vaadin.flow.component.gridpro.examples;

import java.util.Random;

public enum Gender {
    MALE("Male"), FEMALE("Female"), UNKNOWN("Unknown");

    private String stringRepresentation;

    private Gender(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }

    public static Gender getRandomCGender() {
        Random random = new Random();
        return Gender.values()[random.nextInt(values().length)];
    }

    public String getStringRepresentation() {
        return stringRepresentation;
    }

    @Override
    public String toString() {
        return getStringRepresentation();
    }
}
