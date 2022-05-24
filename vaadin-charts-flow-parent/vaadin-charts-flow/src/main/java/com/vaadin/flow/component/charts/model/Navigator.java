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
 * The navigator is a small series below the main series, displaying a view of
 * the entire data set. It provides tools to zoom in and out on parts of the
 * data as well as panning across the dataset.
 */
public class Navigator extends AbstractConfigurationObject {

    private Boolean adaptToUpdatedData;
    private Boolean enabled;
    private Handles handles;
    private Number height;
    private Number margin;
    private Color maskFill;
    private Boolean maskInside;
    private Boolean opposite;
    private Color outlineColor;
    private Number outlineWidth;
    private PlotOptionsSeries series;
    private XAxis xAxis;
    private YAxis yAxis;

    public Navigator() {
    }

    /**
     * @see #setAdaptToUpdatedData(Boolean)
     */
    public Boolean getAdaptToUpdatedData() {
        return adaptToUpdatedData;
    }

    /**
     * Whether the navigator and scrollbar should adapt to updated data in the
     * base X axis. When loading data async, as in the demo below, this should
     * be <code>false</code>. Otherwise new data will trigger navigator redraw,
     * which will cause unwanted looping. In the demo below, the data in the
     * navigator is set only once. On navigating, only the main chart content is
     * updated.
     * <p>
     * Defaults to: true
     */
    public void setAdaptToUpdatedData(Boolean adaptToUpdatedData) {
        this.adaptToUpdatedData = adaptToUpdatedData;
    }

    public Navigator(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Enable or disable the navigator.
     * <p>
     * Defaults to: true
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setHandles(Handles)
     */
    public Handles getHandles() {
        if (handles == null) {
            handles = new Handles();
        }
        return handles;
    }

    /**
     * <p>
     * Options for the handles for dragging the zoomed area.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the navigator handles are styled with the
     * <code>.highcharts-navigator-handle</code>,
     * <code>.highcharts-navigator-handle-left</code> and
     * <code>.highcharts-navigator-handle-right</code> classes.
     * </p>
     */
    public void setHandles(Handles handles) {
        this.handles = handles;
    }

    /**
     * @see #setHeight(Number)
     */
    public Number getHeight() {
        return height;
    }

    /**
     * The height of the navigator.
     * <p>
     * Defaults to: 40
     */
    public void setHeight(Number height) {
        this.height = height;
    }

    /**
     * @see #setMargin(Number)
     */
    public Number getMargin() {
        return margin;
    }

    /**
     * The distance from the nearest element, the X axis or X axis labels.
     * <p>
     * Defaults to: 25
     */
    public void setMargin(Number margin) {
        this.margin = margin;
    }

    /**
     * @see #setMaskFill(Color)
     */
    public Color getMaskFill() {
        return maskFill;
    }

    /**
     * The color of the mask covering the areas of the navigator series that are
     * currently not visible in the main series. The default color is bluish
     * with an opacity of 0.3 to see the series below.
     * <p>
     * Defaults to: rgba(102,133,194,0.3)
     */
    public void setMaskFill(Color maskFill) {
        this.maskFill = maskFill;
    }

    /**
     * @see #setMaskInside(Boolean)
     */
    public Boolean getMaskInside() {
        return maskInside;
    }

    /**
     * Whether the mask should be inside the range marking the zoomed range, or
     * outside. In Highstock 1.x it was always <code>false</code>.
     * <p>
     * Defaults to: true
     */
    public void setMaskInside(Boolean maskInside) {
        this.maskInside = maskInside;
    }

    /**
     * @see #setOpposite(Boolean)
     */
    public Boolean getOpposite() {
        return opposite;
    }

    /**
     * When the chart is inverted, whether to draw the navigator on the opposite
     * side.
     * <p>
     * Defaults to: false
     */
    public void setOpposite(Boolean opposite) {
        this.opposite = opposite;
    }

    /**
     * @see #setOutlineColor(Color)
     */
    public Color getOutlineColor() {
        return outlineColor;
    }

    /**
     * The color of the line marking the currently zoomed area in the navigator.
     * <p>
     * Defaults to: #cccccc
     */
    public void setOutlineColor(Color outlineColor) {
        this.outlineColor = outlineColor;
    }

    /**
     * @see #setOutlineWidth(Number)
     */
    public Number getOutlineWidth() {
        return outlineWidth;
    }

    /**
     * The width of the line marking the currently zoomed area in the navigator.
     * <p>
     * Defaults to: 2
     */
    public void setOutlineWidth(Number outlineWidth) {
        this.outlineWidth = outlineWidth;
    }

    /**
     * @see #setSeries(PlotOptionsSeries)
     */
    public PlotOptionsSeries getSeries() {
        return series;
    }

    /**
     * <p>
     * Options for the navigator series. Available options are the same as any
     * series, documented at
     * <a class="internal" href="#plotOptions.series">plotOptions</a> and
     * <a class="internal" href="#series">series</a>.
     * </p>
     *
     * <p>
     * Unless data is explicitly defined on navigator.series, the data is
     * borrowed from the first series in the chart.
     * </p>
     *
     * <p>
     * Default series options for the navigator series are:
     * </p>
     *
     * <pre>
     * series: {
     * 		type: 'areaspline',
     * 		color: '#4572A7',
     * 		fillOpacity: 0.05,
     * 		dataGrouping: {
     * 			smoothed: true
     * 		},
     * 		lineWidth: 1,
     * 		marker: {
     * 			enabled: false
     * 		}
     * 	}
     * </pre>
     */
    public void setSeries(PlotOptionsSeries series) {
        this.series = series;
    }

    /**
     * @see #setXAxis(XAxis)
     */
    public XAxis getXAxis() {
        if (xAxis == null) {
            xAxis = new XAxis();
        }
        return xAxis;
    }

    /**
     * Options for the navigator X axis. Available options are the same as any X
     * axis, documented at <a class="internal" href="#xAxis">xAxis</a>. Default
     * series options for the navigator xAxis are:
     *
     * <pre>
     * xAxis: {
     * 	    tickWidth: 0,
     * 	    lineWidth: 0,
     * 	    gridLineWidth: 1,
     * 	    tickPixelInterval: 200,
     * 	    labels: {
     * 	        align: 'left',
     * 	        style: {
     * 	            color: '#888'
     * 	        },
     * 	        x: 3,
     * 	        y: -4
     * 	    }
     * 	}
     * </pre>
     */
    public void setXAxis(XAxis xAxis) {
        this.xAxis = xAxis;
    }

    /**
     * @see #setYAxis(YAxis)
     */
    public YAxis getYAxis() {
        if (yAxis == null) {
            yAxis = new YAxis();
        }
        return yAxis;
    }

    /**
     * Options for the navigator Y axis. Available options are the same as any y
     * axis, documented at <a class="internal" href="#yAxis">yAxis</a>. Default
     * series options for the navigator yAxis are:
     *
     * <pre>
     * yAxis: {
     * 		gridLineWidth: 0,
     * 		startOnTick: false,
     * 		endOnTick: false,
     * 		minPadding: 0.1,
     * 		maxPadding: 0.1,
     * 		labels: {
     * 			enabled: false
     * 		},
     * 		title: {
     * 			text: null
     * 		},
     * 		tickWidth: 0
     * 	}
     * </pre>
     */
    public void setYAxis(YAxis yAxis) {
        this.yAxis = yAxis;
    }
}
