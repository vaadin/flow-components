package com.vaadin.flow.component.grid.demo.entity;

import java.time.LocalDate;

public class Task {

    private int id;
    private String name;
    private LocalDate dueDate;

    public Task(int id, String name, LocalDate dueDate) {
        this.id = id;
        this.name = name;
        this.dueDate = dueDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        return name;
    }
}
