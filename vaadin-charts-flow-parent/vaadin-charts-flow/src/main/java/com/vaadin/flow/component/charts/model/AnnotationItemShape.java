package com.vaadin.flow.component.charts.model;

import java.util.List;

public class AnnotationItemShape extends AbstractConfigurationObject {

    // TODO handle serialization of shapeOptions fields
    private AnnotationItemShapeOptions shapeOptions;
    private String markerEnd;
    private String markerStart;
    private AnnotationItemLabelMockPointOptions point;
    private List<AnnotationItemLabelMockPointOptions> points;

    public AnnotationItemShapeOptions getShapeOptions() {
        return shapeOptions;
    }

    public void setShapeOptions(AnnotationItemShapeOptions shapeOptions) {
        this.shapeOptions = shapeOptions;
    }

    public String getMarkerEnd() {
        return markerEnd;
    }

    public void setMarkerEnd(String markerEnd) {
        this.markerEnd = markerEnd;
    }

    public String getMarkerStart() {
        return markerStart;
    }

    public void setMarkerStart(String markerStart) {
        this.markerStart = markerStart;
    }

    public AnnotationItemLabelMockPointOptions getPoint() {
        return point;
    }

    public void setPoint(AnnotationItemLabelMockPointOptions point) {
        this.point = point;
    }

    public List<AnnotationItemLabelMockPointOptions> getPoints() {
        return points;
    }

    public void setPoints(List<AnnotationItemLabelMockPointOptions> points) {
        this.points = points;
    }
}
