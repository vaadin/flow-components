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
 * The appearance of the point marker when selected. In order to allow a point
 * to be selected, set the <code>series.allowPointSelect</code> option to true.
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class Select extends AbstractConfigurationObject {

	private Boolean enabled;
	private Color fillColor;
	private Color lineColor;
	private Number lineWidth;
	private Number radius;

	public Select() {
	}

	public Select(Boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @see #setEnabled(Boolean)
	 */
	public Boolean getEnabled() {
		return enabled;
	}

	/**
	 * Enable or disable visible feedback for selection.
	 * <p>
	 * Defaults to: true
	 */
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @see #setFillColor(Color)
	 */
	public Color getFillColor() {
		return fillColor;
	}

	/**
	 * The fill color of the point marker.
	 */
	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

	/**
	 * @see #setLineColor(Color)
	 */
	public Color getLineColor() {
		return lineColor;
	}

	/**
	 * The color of the point marker's outline. When <code>null</code>, the
	 * series' or point's color is used.
	 * <p>
	 * Defaults to: #000000
	 */
	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	/**
	 * @see #setLineWidth(Number)
	 */
	public Number getLineWidth() {
		return lineWidth;
	}

	/**
	 * The width of the point marker's outline.
	 * <p>
	 * Defaults to: 0
	 */
	public void setLineWidth(Number lineWidth) {
		this.lineWidth = lineWidth;
	}

	/**
	 * @see #setRadius(Number)
	 */
	public Number getRadius() {
		return radius;
	}

	/**
	 * The radius of the point marker. In hover state, it defaults to the normal
	 * state's radius + 2.
	 */
	public void setRadius(Number radius) {
		this.radius = radius;
	}
}
