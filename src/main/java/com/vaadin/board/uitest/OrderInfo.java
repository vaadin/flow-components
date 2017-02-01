package com.vaadin.board.uitest;

import java.util.Random;

public class OrderInfo {

    private String due;

    private String description;

    private String status;

    private static Random r = new Random(123);

    public OrderInfo() {
    }

    public OrderInfo(String due, String description, String status) {
        super();
        this.due = due;
        this.description = description;
        this.status = status;
    }

    public String getDue() {
        return due;
    }

    public void setDue(String due) {
        this.due = due;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static OrderInfo create() {
        return new OrderInfo("Today", "Matti " + r.nextInt(20),
                r.nextBoolean() ? "Available" : "Picked up");
    }

}
