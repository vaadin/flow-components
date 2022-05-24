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
 * An array of lines stretching across the plot area, marking a specific value
 * on one of the axes.
 * </p>
 *
 * <p>
 * In
 * <a href="http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
 * >styled mode</a>, the plot lines are styled by the
 * <code>.highcharts-plot-line</code> class in addition to the
 * <code>className</code> option.
 * </p>
 */
public class PlotLine extends AbstractConfigurationObject {

    private String className;
    private Color color;
    private DashStyle dashStyle;
    private String id;
    private Label label;
    private Number value;
    private Number width;
    private Number zIndex;

    public PlotLine() {
    }

    /**
     * @see #setClassName(String)
     */
    public String getClassName() {
        return className;
    }

    /**
     * A custom class name, in addition to the default
     * <code>highcharts-plot-line</code>, to apply to each individual line.
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
     * The color of the line.
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
     * The dashing or dot style for the plot line. For possible values see
     * <a href=
     * "http://jsfiddle.net/gh/get/library/pure/highcharts/highcharts/tree/master/samples/highcharts/plotoptions/series-dashstyle-all/"
     * >this overview</a>.
     * <p>
     * Defaults to: Solid
     */
    public void setDashStyle(DashStyle dashStyle) {
        this.dashStyle = dashStyle;
    }

    /**
     * @see #setId(String)
     */
    public String getId() {
        return id;
    }

    /**
     * An id used for identifying the plot line in Axis.removePlotLine.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @see #setLabel(Label)
     */
    public Label getLabel() {
        if (label == null) {
            label = new Label();
        }
        return label;
    }

    /**
     * Text labels for the plot bands
     */
    public void setLabel(Label label) {
        this.label = label;
    }

    /**
     * @see #setValue(Number)
     */
    public Number getValue() {
        return value;
    }

    /**
     * The position of the line in axis units.
     */
    public void setValue(Number value) {
        this.value = value;
    }

    /**
     * @see #setWidth(Number)
     */
    public Number getWidth() {
        return width;
    }

    /**
     * The width or thickness of the plot line.
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
     * The z index of the plot line within the chart.
     */
    public void setZIndex(Number zIndex) {
        this.zIndex = zIndex;
    }

    public PlotLine(Number value, Number width, Color color) {
        this.value = value;
        this.width = width;
        this.color = color;
    }
}
