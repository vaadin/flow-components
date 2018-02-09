package com.vaadin.flow.component.charts.model;

import javax.annotation.Generated;
/**
 * <p>
 * Options for the pivot or the center point of the gauge.
 * </p>
 * 
 * <p>
 * In <a
 * href="http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
 * >styled mode</a>, the pivot is styled with the
 * <code>.highcharts-gauge-series .highcharts-pivot</code> rule.
 * </p>
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
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