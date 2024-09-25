/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * Positioning options for {@link DrillUpButton}
 */
public class ButtonPosition extends AbstractConfigurationObject {
    private VerticalAlign verticalAlign;
    private HorizontalAlign align;
    private Number x;
    private Number y;

    /**
     * @see #setVerticalAlign(VerticalAlign)
     * @return the verticalAlign
     */
    public VerticalAlign getVerticalAlign() {
        return verticalAlign;
    }

    /**
     * Sets the vertical alignment of the button. Can be one of
     * {@link VerticalAlign#TOP}, {@link VerticalAlign#MIDDLE} and
     * {@link VerticalAlign#BOTTOM}.
     *
     * @param verticalAlign
     *            the align to set
     */
    public void setVerticalAlign(VerticalAlign verticalAlign) {
        this.verticalAlign = verticalAlign;
    }

    /**
     * @see #setHorizontalAlign(HorizontalAlign)
     * @return the horizontal alignment
     */
    public HorizontalAlign getHorizontalAlign() {
        return align;
    }

    /**
     * The horizontal alignment of the button. Can be one of
     * {@link HorizontalAlign#LEFT}, {@link HorizontalAlign#CENTER} and
     * {@link HorizontalAlign#RIGHT}. .
     *
     * @param horizontalAlign
     *            the alignment to set
     */
    public void setHorizontalAlign(HorizontalAlign horizontalAlign) {
        align = horizontalAlign;
    }

    /**
     * @see #setX(Number)
     * @return the X position of the button
     */
    public Number getX() {
        return x;
    }

    /**
     * The X position of the button.
     *
     * @param x
     *            the X position to set
     */
    public void setX(Number x) {
        this.x = x;
    }

    /**
     *
     * @see #setY(Number)
     * @return the Y position of the button
     */
    public Number getY() {
        return y;
    }

    /**
     * The Y position of the button.
     *
     * @param y
     *            the Y position to set
     */
    public void setY(Number y) {
        this.y = y;
    }

}
