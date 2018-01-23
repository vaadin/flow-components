package com.vaadin.addon.charts.model;
/**
 * Options for the paging or navigation appearing when the legend is overflown.
 * Navigation works well on screen, but not in static exported images. One way
 * of working around that is to <a href=
 * "http://jsfiddle.net/gh/get/library/pure/highcharts/highcharts/tree/master/samples/highcharts/legend/navigation-enabled-false/"
 * >increase the chart height in export</a>.
 */
public class LegendNavigation extends AbstractConfigurationObject {

	private Boolean animation;
	private Number arrowSize;
	private Boolean enabled;

	public LegendNavigation() {
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
}