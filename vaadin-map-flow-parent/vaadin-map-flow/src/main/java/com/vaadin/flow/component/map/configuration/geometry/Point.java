package com.vaadin.flow.component.map.configuration.geometry;

import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.Coordinate;

public class Point extends SimpleGeometry {

    private Coordinate coordinates;

    @Override
    public String getType() {
        return Constants.OL_GEOMETRY_POINT;
    }

    public Point(Coordinate coordinates) {
        this.coordinates = coordinates;
    }

    public Coordinate getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinate coordinates) {
        this.coordinates = coordinates;
        notifyChange();
    }
}
