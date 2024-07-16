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
    private String id;
    private Label label;
    private Number value;
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

    public PlotLine(Number value) {
        this.value = value;
    }
}
