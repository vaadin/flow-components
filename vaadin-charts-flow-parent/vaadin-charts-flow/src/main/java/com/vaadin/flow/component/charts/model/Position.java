package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

/**
 * Position configuration for the credits label. Supported properties are align,
 * verticalAlign, x and y. Defaults to
 *
 * position: { align: 'right', x: -10, verticalAlign: 'bottom', y: -5 }
 */
public class Position extends AbstractConfigurationObject {
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
     * Sets the vertical alignment of the credits. Can be one of
     * {@link VerticalAlign#TOP}, {@link VerticalAlign#MIDDLE} and
     * {@link VerticalAlign#BOTTOM}. Defaults to {@link VerticalAlign#TOP}.
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
     * The horizontal alignment of the credits. Can be one of
     * {@link HorizontalAlign#LEFT}, {@link HorizontalAlign#CENTER} and
     * {@link HorizontalAlign#RIGHT}. Defaults to {@link HorizontalAlign#CENTER}
     * .
     *
     * @param horizontalAlign
     *            the alignment to set
     */
    public void setHorizontalAlign(HorizontalAlign horizontalAlign) {
        align = horizontalAlign;
    }

    /**
     * @see #setX(Number)
     * @return the X position of the credits
     */
    public Number getX() {
        return x;
    }

    /**
     * The X position of the credits. Defaults to -10.
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
     * @return the Y position of the credits
     */
    public Number getY() {
        return y;
    }

    /**
     * The Y position of the credits. Defaults to -5.
     *
     * @param y
     *            the Y position to set
     */
    public void setY(Number y) {
        this.y = y;
    }

}
