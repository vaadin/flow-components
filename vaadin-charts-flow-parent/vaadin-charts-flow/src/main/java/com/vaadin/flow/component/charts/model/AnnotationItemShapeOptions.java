package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Color;

public class AnnotationItemShapeOptions extends AbstractConfigurationObject {
    private DashStyle dashStyle;
    private Color fill;
    private Number height;
    private Number r;
    private Number ry;
    private Number snap;
    private String src;
    private Color stroke;
    private Number strokeWidth;
    private String type;
    private Number width;
    private Number xAxis;
    private Number yAxis;

    public DashStyle getDashStyle() {
        return dashStyle;
    }

    public void setDashStyle(DashStyle dashStyle) {
        this.dashStyle = dashStyle;
    }

    public Color getFill() {
        return fill;
    }

    public void setFill(Color fill) {
        this.fill = fill;
    }

    public Number getHeight() {
        return height;
    }

    public void setHeight(Number height) {
        this.height = height;
    }

    public Number getR() {
        return r;
    }

    public void setR(Number r) {
        this.r = r;
    }

    public Number getRy() {
        return ry;
    }

    public void setRy(Number ry) {
        this.ry = ry;
    }

    public Number getSnap() {
        return snap;
    }

    public void setSnap(Number snap) {
        this.snap = snap;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public Color getStroke() {
        return stroke;
    }

    public void setStroke(Color stroke) {
        this.stroke = stroke;
    }

    public Number getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(Number strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Number getWidth() {
        return width;
    }

    public void setWidth(Number width) {
        this.width = width;
    }

    public Number getXAxis() {
        return xAxis;
    }

    public void setXAxis(Number xAxis) {
        this.xAxis = xAxis;
    }

    public Number getYAxis() {
        return yAxis;
    }

    public void setYAxis(Number yAxis) {
        this.yAxis = yAxis;
    }
}
