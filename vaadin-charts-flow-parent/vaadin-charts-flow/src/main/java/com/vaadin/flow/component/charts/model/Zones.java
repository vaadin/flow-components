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

import com.vaadin.flow.component.charts.model.style.Color;

/**
 * <p>
 * An array defining zones within a series. Zones can be applied to the X axis,
 * Y axis or Z axis for bubbles, according to the <code>zoneAxis</code> option.
 * </p>
 *
 * <p>
 * In
 * <a href="http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
 * >styled mode</a>, the color zones are styled with the
 * <code>.highcharts-zone-{n}</code> class, or custom classed from the
 * <code>className</code> option (<a href=
 * "http://jsfiddle.net/gh/get/library/pure/highcharts/highcharts/tree/master/samples/highcharts/css/color-zones/"
 * >view live demo</a>).
 * </p>
 */
public class Zones extends AbstractConfigurationObject {

    private String className;
    private Color color;
    private DashStyle dashStyle;
    private Color fillColor;
    private Number value;

    public Zones() {
    }

    /**
     * @see #setClassName(String)
     */
    public String getClassName() {
        return className;
    }

    /**
     * <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >Styled mode</a> only. A custom class name for the zone.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @see #setColor(Color)
     */
    public Color getColor() {
        return color;
    }

    /**
     * Defines the color of the series.
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * @see #setDashStyle(DashStyle)
     */
    public DashStyle getDashStyle() {
        return dashStyle;
    }

    /**
     * A name for the dash style to use for the graph.
     */
    public void setDashStyle(DashStyle dashStyle) {
        this.dashStyle = dashStyle;
    }

    /**
     * @see #setFillColor(Color)
     */
    public Color getFillColor() {
        return fillColor;
    }

    /**
     * Defines the fill color for the series (in area type series)
     */
    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    /**
     * @see #setValue(Number)
     */
    public Number getValue() {
        return value;
    }

    /**
     * The value up to where the zone extends, if undefined the zones stretches
     * to the last value in the series.
     * <p>
     * Defaults to: undefined
     */
    public void setValue(Number value) {
        this.value = value;
    }
}
