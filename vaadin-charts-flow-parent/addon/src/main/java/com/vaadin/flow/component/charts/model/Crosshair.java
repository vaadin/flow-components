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
 * Configure a crosshair that follows either the mouse pointer or the hovered
 * point. By default, the crosshair is enabled on the X axis and disabled on Y
 * axis.
 * </p>
 * 
 * <p>
 * In <a
 * href="http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
 * >styled mode</a>, the crosshairs are styled in the
 * <code>.highcharts-crosshair</code>, <code>.highcharts-crosshair-thin</code>
 * or <code>.highcharts-xaxis-category</code> classes.
 * </p>
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class Crosshair extends AbstractConfigurationObject {

	private String className;
	private Boolean snap;
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
