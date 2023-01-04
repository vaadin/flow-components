package com.vaadin.flow.component.charts.model;

/**
 * Contains coordinates for {@link AnnotationItemLabel}
 */
public class AnnotationItemLabelMockPointOptions
        extends AbstractConfigurationObject {

    private Number x;
    private Number y;

    /**
     * Constructs an AnnotationItemLabelMockPointOptions with the given
     * coordinates
     *
     * @param x
     *            Horizontal offset
     * @param y
     *            Vertical offset
     */
    public AnnotationItemLabelMockPointOptions(Number x, Number y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @see #setX(Number)
     */
    public Number getX() {
        return x;
    }

    /**
     * Sets the horizontal offset of the label within chart
     */
    public void setX(Number x) {
        this.x = x;
    }

    /**
     * @see #setY(Number)
     */
    public Number getY() {
        return y;
    }

    /**
     * Sets the vertical offset of the label within chart
     */
    public void setY(Number y) {
        this.y = y;
    }
}
