/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2019 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */

/**
 * <p>
 * Options for the dial or arrow pointer of the gauge.
 * </p>
 *
 * <p>
 * In
 * <a href="http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
 * >styled mode</a>, the dial is styled with the
 * <code>.highcharts-gauge-series .highcharts-dial</code> rule.
 * </p>
 */
public class Dial extends AbstractConfigurationObject {

    private String baseLength;
    private Number baseWidth;
    private String radius;
    private String rearLength;
    private Number topWidth;

    public Dial() {
    }

    /**
     * @see #setBaseLength(String)
     */
    public String getBaseLength() {
        return baseLength;
    }

    /**
     * The length of the dial's base part, relative to the total radius or
     * length of the dial.
     * <p>
     * Defaults to: 70%
     */
    public void setBaseLength(String baseLength) {
        this.baseLength = baseLength;
    }

    /**
     * @see #setBaseWidth(Number)
     */
    public Number getBaseWidth() {
        return baseWidth;
    }

    /**
     * The pixel width of the base of the gauge dial. The base is the part
     * closest to the pivot, defined by baseLength.
     * <p>
     * Defaults to: 3
     */
    public void setBaseWidth(Number baseWidth) {
        this.baseWidth = baseWidth;
    }

    /**
     * @see #setRadius(String)
     */
    public String getRadius() {
        return radius;
    }

    /**
     * The radius or length of the dial, in percentages relative to the radius
     * of the gauge itself.
     * <p>
     * Defaults to: 80%
     */
    public void setRadius(String radius) {
        this.radius = radius;
    }

    /**
     * @see #setRearLength(String)
     */
    public String getRearLength() {
        return rearLength;
    }

    /**
     * The length of the dial's rear end, the part that extends out on the other
     * side of the pivot. Relative to the dial's length.
     * <p>
     * Defaults to: 10%
     */
    public void setRearLength(String rearLength) {
        this.rearLength = rearLength;
    }

    /**
     * @see #setTopWidth(Number)
     */
    public Number getTopWidth() {
        return topWidth;
    }

    /**
     * The width of the top of the dial, closest to the perimeter. The pivot
     * narrows in from the base to the top.
     * <p>
     * Defaults to: 1
     */
    public void setTopWidth(Number topWidth) {
        this.topWidth = topWidth;
    }
}
