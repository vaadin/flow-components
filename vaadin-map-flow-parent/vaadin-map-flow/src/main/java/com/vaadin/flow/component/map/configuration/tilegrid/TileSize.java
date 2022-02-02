package com.vaadin.flow.component.map.configuration.tilegrid;

public class TileSize {
    public TileSize(double width, double height) {
        this.width = width;
        this.height = height;
    }

    private double width;
    private double height;

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

}
