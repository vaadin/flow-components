package com.vaadin.flow.component.datepicker.entity;

import java.time.LocalDate;

public class Person {
    private LocalDate birthDate;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}
