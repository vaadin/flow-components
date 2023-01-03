/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Style;

import java.util.ArrayList;
import java.util.List;

public class AnnotationItem extends AbstractConfigurationObject {

    private AnnotationItemAnimation animation;
    private AnnotationItemControlPointOptions controlPointOptions;
    private Boolean crop;
    private AnnotationItemDraggable draggable;
    private String id;
    private AnnotationItemLabelOptions labelOptions;
    private List<AnnotationItemLabel> labels;
    private AnnotationItemShapeOptions shapeOptions;
    private List<AnnotationItemShape> shapes;
    private Boolean visible;
    private Number zIndex;

    public AnnotationItemAnimation getAnimation() {
        return animation;
    }

    public void setAnimation(AnnotationItemAnimation animation) {
        this.animation = animation;
    }

    public AnnotationItemControlPointOptions getControlPointOptions() {
        return controlPointOptions;
    }

    public void setControlPointOptions(
            AnnotationItemControlPointOptions controlPointOptions) {
        this.controlPointOptions = controlPointOptions;
    }

    public Boolean getCrop() {
        return crop;
    }

    public void setCrop(Boolean crop) {
        this.crop = crop;
    }

    public AnnotationItemDraggable getDraggable() {
        return draggable;
    }

    public void setDraggable(AnnotationItemDraggable draggable) {
        this.draggable = draggable;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AnnotationItemLabelOptions getLabelOptions() {
        return labelOptions;
    }

    public void setLabelOptions(AnnotationItemLabelOptions labelOptions) {
        this.labelOptions = labelOptions;
    }

    public List<AnnotationItemLabel> getLabels() {
        return labels;
    }

    public void setLabels(List<AnnotationItemLabel> labels) {
        this.labels = labels;
    }

    public void addLabel(AnnotationItemLabel label) {
        if (labels == null) {
            labels = new ArrayList<>();
        }
        labels.add(label);
    }

    public AnnotationItemShapeOptions getShapeOptions() {
        return shapeOptions;
    }

    public void setShapeOptions(AnnotationItemShapeOptions shapeOptions) {
        this.shapeOptions = shapeOptions;
    }

    public List<AnnotationItemShape> getShapes() {
        return shapes;
    }

    public void setShapes(List<AnnotationItemShape> shapes) {
        this.shapes = shapes;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Number getZIndex() {
        return zIndex;
    }

    public void setZIndex(Number zIndex) {
        this.zIndex = zIndex;
    }
}
