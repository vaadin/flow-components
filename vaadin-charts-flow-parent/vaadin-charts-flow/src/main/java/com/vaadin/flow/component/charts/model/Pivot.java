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
import com.vaadin.flow.component.charts.model.style.Color;
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

	private Color backgroundColor;
	private Color borderColor;
	private Number borderWidth;
	private Number radius;

	public Pivot() {
	}

	/**
	 * @see #setBackgroundColor(Color)
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * The background color or fill of the pivot.
	 * <p>
	 * Defaults to: #000000
	 */
	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	/**
	 * @see #setBorderColor(Color)
	 */
	public Color getBorderColor() {
		return borderColor;
	}

	/**
	 * The border or stroke color of the pivot. In able to change this, the
	 * borderWidth must also be set to something other than the default 0.
	 * <p>
	 * Defaults to: #cccccc
	 */
	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	/**
	 * @see #setBorderWidth(Number)
	 */
	public Number getBorderWidth() {
		return borderWidth;
	}

	/**
	 * The border or stroke width of the pivot.
	 * <p>
	 * Defaults to: 0
	 */
	public void setBorderWidth(Number borderWidth) {
		this.borderWidth = borderWidth;
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
