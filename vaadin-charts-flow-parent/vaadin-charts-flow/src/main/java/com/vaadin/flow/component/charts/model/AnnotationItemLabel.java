package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Style;

/**
 * Label that can be positioned anywhere in the chart area
 */
public class AnnotationItemLabel extends AbstractConfigurationObject {

    private AnnotationItemLabelPoint point;
    private Style style;
    private String text;
    private Boolean useHTML;

    /**
     * Constructs an AnnotationItemLabel with the given text
     *
     * @param text
     *            Text to be displayed
     */
    public AnnotationItemLabel(String text) {
        this.text = text;
    }

    /**
     * @see #setPoint(AnnotationItemLabelPoint)
     */
    public AnnotationItemLabelPoint getPoint() {
        return point;
    }

    /**
     * Sets the {@link AnnotationItemLabelPoint} that contains the coordinate
     * data for the label
     *
     * @param point
     *            Label point options
     */
    public void setPoint(AnnotationItemLabelPoint point) {
        this.point = point;
    }

    /**
     * @see #setStyle(Style)
     */
    public Style getStyle() {
        return style;
    }

    /**
     * Sets the label style options
     *
     * @param style
     *            Label style options
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
     * Sets the text to be displayed
     *
     * @param text
     *            Text to be displayed
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
     *
     * @param useHTML
     *            Whether to enable HTML
     */
    public void setUseHTML(Boolean useHTML) {
        this.useHTML = useHTML;
    }
}
