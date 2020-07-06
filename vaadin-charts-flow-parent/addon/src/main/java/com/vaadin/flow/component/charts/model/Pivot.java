package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2018 Vaadin Ltd
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
