package com.vaadin.flow.component.combobox.test.entity;

public class Ticket {

    private int row;
    private int seat;

    public Ticket(int row, int seat) {
        this.row = row;
        this.seat = seat;
    }

    public int getRow() {
        return row;
    }

    public int getSeat() {
        return seat;
    }

    @Override
    public String toString() {
        return String.format("Row %d / Seat %d", row, seat);
    }
}
