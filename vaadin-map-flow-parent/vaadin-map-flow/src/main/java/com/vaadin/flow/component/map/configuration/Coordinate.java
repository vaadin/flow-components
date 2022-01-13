package com.vaadin.flow.component.map.configuration;

public class Coordinate {
    private final double x;
    private final double y;

    public Coordinate() {
        this(0, 0);
    }

    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
