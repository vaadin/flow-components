package com.vaadin.flow.component.charts.model;

public enum AnnotationItemDraggable implements ChartEnum {

    X("x"), XY("xy"), Y("y"), DISABLED("");

    private String value;

    private AnnotationItemDraggable(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
