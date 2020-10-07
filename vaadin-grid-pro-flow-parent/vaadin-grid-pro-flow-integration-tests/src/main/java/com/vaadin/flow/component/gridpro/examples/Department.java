package com.vaadin.flow.component.gridpro.examples;

import java.util.Random;

public enum Department {
    SERVICES("services"), MARKETING("marketing"), SALES("sales");

    private String stringRepresentation;

    private Department(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }

    public static Department getRandomDepartment() {
        Random random = new Random();
        return Department.values()[random.nextInt(values().length)];
    }

    public String getStringRepresentation() {
        return stringRepresentation;
    }

    @Override
    public String toString() {
        return getStringRepresentation();
    }
}
