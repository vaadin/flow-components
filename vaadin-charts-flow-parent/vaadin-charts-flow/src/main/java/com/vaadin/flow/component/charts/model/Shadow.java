package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Color;

public class Shadow extends AbstractConfigurationObject {

    private Color color;
    private Number offsetX;
    private Number offsetY;
    private Number opacity;
    private Number width;

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Number getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(Number offsetX) {
        this.offsetX = offsetX;
    }

    public Number getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(Number offsetY) {
        this.offsetY = offsetY;
    }

    public Number getOpacity() {
        return opacity;
    }

    public void setOpacity(Number opacity) {
        this.opacity = opacity;
    }

    public Number getWidth() {
        return width;
    }

    public void setWidth(Number width) {
        this.width = width;
    }
}
