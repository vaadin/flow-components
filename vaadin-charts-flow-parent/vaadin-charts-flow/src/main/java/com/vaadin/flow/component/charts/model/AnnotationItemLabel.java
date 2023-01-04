package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Style;

public class AnnotationItemLabel extends AbstractConfigurationObject {

    private AnnotationItemLabelMockPointOptions point;
    private Style style;
    private String text;
    private Boolean useHTML;

    public AnnotationItemLabel(String text) {
        this.text = text;
    }

    public AnnotationItemLabelMockPointOptions getPoint() {
        return point;
    }

    public void setPoint(AnnotationItemLabelMockPointOptions point) {
        this.point = point;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getUseHTML() {
        return useHTML;
    }

    public void setUseHTML(Boolean useHTML) {
        this.useHTML = useHTML;
    }
}
