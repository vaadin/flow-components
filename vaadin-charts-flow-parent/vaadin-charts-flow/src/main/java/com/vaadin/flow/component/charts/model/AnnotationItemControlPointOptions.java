package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Style;

public class AnnotationItemControlPointOptions
        extends AbstractConfigurationObject {

    private Number height;
    private Style style;
    private String symbol;
    private Boolean visible;
    private Number width;

    public Number getHeight() {
        return height;
    }

    public void setHeight(Number height) {
        this.height = height;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Number getWidth() {
        return width;
    }

    public void setWidth(Number width) {
        this.width = width;
    }
}
