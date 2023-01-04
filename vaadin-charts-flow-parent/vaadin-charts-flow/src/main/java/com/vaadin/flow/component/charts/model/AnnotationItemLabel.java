package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Style;

/**
 * Label that can be positioned anywhere in the chart area
 */
public class AnnotationItemLabel extends AbstractConfigurationObject {

    private AnnotationItemLabelMockPointOptions point;
    private Style style;
    private String text;
    private Boolean useHTML;

    /**
     * Constructs an AnnotationItemLabel with the given text
     *
     * @param text
     *            The text to be displayed
     */
    public AnnotationItemLabel(String text) {
        this.text = text;
    }

    /**
     * @see #setPoint(AnnotationItemLabelMockPointOptions)
     */
    public AnnotationItemLabelMockPointOptions getPoint() {
        return point;
    }

    /**
     * Sets the {@link AnnotationItemLabelMockPointOptions} that contains the
     * coordinate data for the label
     */
    public void setPoint(AnnotationItemLabelMockPointOptions point) {
        this.point = point;
    }

    /**
     * @see #setStyle(Style)
     */
    public Style getStyle() {
        return style;
    }

    /**
     * Label style options
     */
    public void setStyle(Style style) {
        this.style = style;
    }

    /**
     * @see #setText(String)
     */
    public String getText() {
        return text;
    }

    /**
     * Value to be displayed
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @see #setUseHTML(Boolean)
     */
    public Boolean getUseHTML() {
        return useHTML;
    }

    /**
     * Whether to enable HTML parsing for the label contents
     */
    public void setUseHTML(Boolean useHTML) {
        this.useHTML = useHTML;
    }
}
