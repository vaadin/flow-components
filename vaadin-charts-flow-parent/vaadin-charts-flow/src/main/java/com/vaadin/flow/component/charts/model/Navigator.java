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


import javax.annotation.Generated;

/**
 * The navigator is a small series below the main series, displaying a view of
 * the entire data set. It provides tools to zoom in and out on parts of the
 * data as well as panning across the dataset.
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class Navigator extends AbstractConfigurationObject {

	private Boolean adaptToUpdatedData;
	private Boolean enabled;
	private Number height;
	private Number margin;
	private Boolean maskInside;
	private Boolean opposite;
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
	 * @see #setSeries(PlotOptionsSeries)
	 */
	public PlotOptionsSeries getSeries() {
		return series;
	}

	/**
	 * <p>
	 * Options for the navigator series. Available options are the same as any
	 * series, documented at <a class="internal"
	 * href="#plotOptions.series">plotOptions</a> and <a class="internal"
	 * href="#series">series</a>.
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
