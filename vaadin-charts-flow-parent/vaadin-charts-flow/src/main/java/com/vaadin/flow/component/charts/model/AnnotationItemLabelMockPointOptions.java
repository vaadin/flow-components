package com.vaadin.flow.component.charts.model;

public class AnnotationItemLabelMockPointOptions
        extends AbstractConfigurationObject {

    private Number x;
    private String xAxis;
    private Number y;
    private String yAxis;

    public AnnotationItemLabelMockPointOptions(Number x, Number y) {
        this.x = x;
        this.y = y;
    }

    public AnnotationItemLabelMockPointOptions(String xAxis, String yAxis) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
    }

    public Number getX() {
        return x;
    }

    public void setX(Number x) {
        this.x = x;
    }

    public String getXAxis() {
        return xAxis;
    }

    public void setXAxis(String xAxis) {
        this.xAxis = xAxis;
    }

    public Number getY() {
        return y;
    }

    public void setY(Number y) {
        this.y = y;
    }

    public String getYAxis() {
        return yAxis;
    }

    public void setYAxis(String yAxis) {
        this.yAxis = yAxis;
    }
}
