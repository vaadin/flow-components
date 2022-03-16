package com.vaadin.flow.component.datepicker.entity;

import java.time.LocalDate;

public class Appointment {
    private LocalDate date;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
