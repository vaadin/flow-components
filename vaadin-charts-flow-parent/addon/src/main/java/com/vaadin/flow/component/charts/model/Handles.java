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
 * Options for the handles for dragging the zoomed area.
 * </p>
 * 
 * <p>
 * In <a
 * href="http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
 * >styled mode</a>, the navigator handles are styled with the
 * <code>.highcharts-navigator-handle</code>,
 * <code>.highcharts-navigator-handle-left</code> and
 * <code>.highcharts-navigator-handle-right</code> classes.
 * </p>
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class Handles extends AbstractConfigurationObject {

	private Color backgroundColor;
	private Color borderColor;

	public Handles() {
	}

	/**
	 * @see #setBackgroundColor(Color)
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * The fill for the handle.
	 * <p>
	 * Defaults to: #f2f2f2
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
	 * The stroke for the handle border and the stripes inside.
	 * <p>
	 * Defaults to: #999999
	 */
	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}
}
