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
 * Configure a crosshair that follows either the mouse pointer or the hovered
 * point. By default, the crosshair is enabled on the X axis and disabled on Y
 * axis.
 * </p>
 *
 * <p>
 * In
 * <a href="http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
 * >styled mode</a>, the crosshairs are styled in the
 * <code>.highcharts-crosshair</code>, <code>.highcharts-crosshair-thin</code>
 * or <code>.highcharts-xaxis-category</code> classes.
 * </p>
 */
public class Crosshair extends AbstractConfigurationObject {

    private String className;
    private Color color;
    private DashStyle dashStyle;
    private Boolean snap;
    private Number width;
    private Number zIndex;
    private CrosshairLabel label;

    public Crosshair() {
    }

    /**
     * @see #setClassName(String)
     */
    public String getClassName() {
        return className;
    }

    /**
     * A class name for the crosshair, especially as a hook for styling.
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
     * The color of the crosshair. Defaults to <code>#cccccc</code> for numeric
     * and datetime axes, and <code>rgba(204,214,235,0.25)</code> for category
     * axes, where the crosshair by default highlights the whole category.
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
     * The dash style for the crosshair. See
     * <a href="#plotOptions.series.dashStyle">series.dashStyle</a> for possible
     * values.
     * <p>
     * Defaults to: Solid
     */
    public void setDashStyle(DashStyle dashStyle) {
        this.dashStyle = dashStyle;
    }

    /**
     * @see #setSnap(Boolean)
     */
    public Boolean getSnap() {
        return snap;
    }

    /**
     * Whether the crosshair should snap to the point or follow the pointer
     * independent of points.
     * <p>
     * Defaults to: true
     */
    public void setSnap(Boolean snap) {
        this.snap = snap;
    }

    /**
     * @see #setWidth(Number)
     */
    public Number getWidth() {
        return width;
    }

    /**
     * The pixel width of the crosshair. Defaults to 1 for numeric or datetime
     * axes, and for one category width for category axes.
     */
    public void setWidth(Number width) {
        this.width = width;
    }

    /**
     * @see #setZIndex(Number)
     */
    public Number getZIndex() {
        return zIndex;
    }

    /**
     * The Z index of the crosshair. Higher Z indices allow drawing the
     * crosshair on top of the series or behind the grid lines.
     * <p>
     * Defaults to: 2
     */
    public void setZIndex(Number zIndex) {
        this.zIndex = zIndex;
    }

    /**
     * @see #setLabel(CrosshairLabel)
     */
    public CrosshairLabel getLabel() {
        if (label == null) {
            label = new CrosshairLabel();
        }
        return label;
    }

    /**
     * <p>
     * A label on the axis next to the crosshair.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the label is styled with the
     * <code>.highcharts-crosshair-label</code> class.
     * </p>
     */
    public void setLabel(CrosshairLabel label) {
        this.label = label;
    }
}
