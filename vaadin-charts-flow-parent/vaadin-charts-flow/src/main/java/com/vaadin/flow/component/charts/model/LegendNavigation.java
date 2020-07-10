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
import com.vaadin.flow.component.charts.model.style.Style;

/**
 * Options for the paging or navigation appearing when the legend is overflown.
 * Navigation works well on screen, but not in static exported images. One way
 * of working around that is to <a href=
 * "http://jsfiddle.net/gh/get/library/pure/highcharts/highcharts/tree/master/samples/highcharts/legend/navigation-enabled-false/"
 * >increase the chart height in export</a>.
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class LegendNavigation extends AbstractConfigurationObject {

	private Color activeColor;
	private Boolean animation;
	private Number arrowSize;
	private Boolean enabled;
	private Color inactiveColor;
	private Style style;

	public LegendNavigation() {
	}

	/**
	 * @see #setActiveColor(Color)
	 */
	public Color getActiveColor() {
		return activeColor;
	}

	/**
	 * The color for the active up or down arrow in the legend page navigation.
	 * <p>
	 * Defaults to: #003399
	 */
	public void setActiveColor(Color activeColor) {
		this.activeColor = activeColor;
	}

	/**
	 * @see #setAnimation(Boolean)
	 */
	public Boolean getAnimation() {
		return animation;
	}

	/**
	 * How to animate the pages when navigating up or down. A value of
	 * <code>true</code> applies the default navigation given in the
	 * chart.animation option. Additional options can be given as an object
	 * containing values for easing and duration. .
	 * <p>
	 * Defaults to: true
	 */
	public void setAnimation(Boolean animation) {
		this.animation = animation;
	}

	/**
	 * @see #setArrowSize(Number)
	 */
	public Number getArrowSize() {
		return arrowSize;
	}

	/**
	 * The pixel size of the up and down arrows in the legend paging navigation.
	 * .
	 * <p>
	 * Defaults to: 12
	 */
	public void setArrowSize(Number arrowSize) {
		this.arrowSize = arrowSize;
	}

	public LegendNavigation(Boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @see #setEnabled(Boolean)
	 */
	public Boolean getEnabled() {
		return enabled;
	}

	/**
	 * <p>
	 * Whether to enable the legend navigation. In most cases, disabling the
	 * navigation results in an unwanted overflow.
	 * </p>
	 * 
	 * <p>
	 * See also the <a href=
	 * "http://www.highcharts.com/plugin-registry/single/8/Adapt-Chart-To-Legend"
	 * >adapt chart to legend</a> plugin for a solution to extend the chart
	 * height to make room for the legend, optionally in exported charts only.
	 * </p>
	 * <p>
	 * Defaults to: true
	 */
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @see #setInactiveColor(Color)
	 */
	public Color getInactiveColor() {
		return inactiveColor;
	}

	/**
	 * The color of the inactive up or down arrow in the legend page navigation.
	 * .
	 * <p>
	 * Defaults to: #cccccc
	 */
	public void setInactiveColor(Color inactiveColor) {
		this.inactiveColor = inactiveColor;
	}

	/**
	 * @see #setStyle(Style)
	 */
	public Style getStyle() {
		if (style == null) {
			style = new Style();
		}
		return style;
	}

	/**
	 * Text styles for the legend page navigation.
	 */
	public void setStyle(Style style) {
		this.style = style;
	}
}
