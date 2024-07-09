/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * <p>
 * Options for the pivot or the center point of the gauge.
 * </p>
 *
 * <p>
 * In
 * <a href="http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
 * >styled mode</a>, the pivot is styled with the
 * <code>.highcharts-gauge-series .highcharts-pivot</code> rule.
 * </p>
 */
public class Pivot extends AbstractConfigurationObject {

    private Number radius;

    public Pivot() {
    }

    /**
     * @see #setRadius(Number)
     */
    public Number getRadius() {
        return radius;
    }

    /**
     * The pixel radius of the pivot.
     * <p>
     * Defaults to: 5
     */
    public void setRadius(Number radius) {
        this.radius = radius;
    }
}
