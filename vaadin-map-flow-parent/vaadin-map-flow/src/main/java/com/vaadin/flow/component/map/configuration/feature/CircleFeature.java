package com.vaadin.flow.component.map.configuration.feature;

import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.Feature;

public class CircleFeature extends Feature {
    Coordinate coordinates;
    String fillColor = "white";
    String strokeColor = "black";
    float strokeWidth = 2;
    float radius = 8;

    public CircleFeature(Coordinate coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public String getType() {
        return Constants.VAADIN_FEATURE_CIRCLE;
    }

    public Coordinate getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinate coordinates) {
        this.coordinates = coordinates;
        notifyChange();
    }

    public String getFillColor() {
        return fillColor;
    }

    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
        notifyChange();
    }

    public String getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
        notifyChange();
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        notifyChange();
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
        notifyChange();
    }
}
