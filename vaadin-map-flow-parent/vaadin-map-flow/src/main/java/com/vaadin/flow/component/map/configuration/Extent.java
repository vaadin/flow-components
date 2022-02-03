package com.vaadin.flow.component.map.configuration;

/**
 * Defines an area within a map using min/max coordinates, which are by default
 * in EPSG:3857 format
 */
public class Extent {
    private final double minX;
    private final double minY;
    private final double maxX;
    private final double maxY;

    public Extent(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public Extent() {
        this(0, 0, 0, 0);
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

}
