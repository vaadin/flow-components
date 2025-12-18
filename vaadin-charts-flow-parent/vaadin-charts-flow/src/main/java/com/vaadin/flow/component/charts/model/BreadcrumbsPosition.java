/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * Positioning options for Breadcrumbs navigation.
 */
public class BreadcrumbsPosition extends AbstractConfigurationObject {
    private String align;
    private String verticalAlign;
    private Number x;
    private Number y;

    /**
     * Horizontal alignment of the breadcrumbs (e.g. "left", "center", "right").
     */
    public String getAlign() {
        return align;
    }

    public void setAlign(String align) {
        this.align = align;
    }

    /**
     * Vertical alignment of the breadcrumbs (e.g. "top", "middle", "bottom").
     */
    public String getVerticalAlign() {
        return verticalAlign;
    }

    public void setVerticalAlign(String verticalAlign) {
        this.verticalAlign = verticalAlign;
    }

    /**
     * Horizontal pixel offset.
     */
    public Number getX() {
        return x;
    }

    public void setX(Number x) {
        this.x = x;
    }

    /**
     * Vertical pixel offset.
     */
    public Number getY() {
        return y;
    }

    public void setY(Number y) {
        this.y = y;
    }
}
