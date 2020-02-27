package com.vaadin.flow.component.datetimepicker.demo.entity;

import java.time.LocalDateTime;

public class Appointment {
    private LocalDateTime dateTime;

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
