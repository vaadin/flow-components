package com.vaadin.flow.data.bean;

public enum Gender {
    MALE("Male"), FEMALE("Female"), UNKNOWN("Unknown");

    private String stringRepresentation;

    private Gender(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }

    public String getStringRepresentation() {
        return stringRepresentation;
    }

    @Override
    public String toString() {
        return getStringRepresentation();
    }
}
