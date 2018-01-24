package com.vaadin.flow.component.charts.model;
/**
 * In Highcharts 1.0, the appearance of all markers belonging to the hovered
 * series. For settings on the hover state of the individual point, see <a
 * href="#plotOptions.series.marker.states.hover">marker.states.hover</a>.
 */
public class Marker extends AbstractConfigurationObject {

	private Boolean enabled;
	private Number height;
	private Number radius;
	private States states;
	private Number width;
	private MarkerSymbol symbol;

	public Marker() {
	}

	public Marker(Boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @see #setEnabled(Boolean)
	 */
	public Boolean getEnabled() {
		return enabled;
	}

	/**
	 * Enable or disable the point marker. If <code>null</code>, the markers are
	 * hidden when the data is dense, and shown for more widespread data points.
	 * <p>
	 * Defaults to: null
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
	 * Image markers only. Set the image width explicitly. When using this
	 * option, a <code>width</code> must also be set.
	 * <p>
	 * Defaults to: null
	 */
	public void setHeight(Number height) {
		this.height = height;
	}

	/**
	 * @see #setRadius(Number)
	 */
	public Number getRadius() {
		return radius;
	}

	/**
	 * The radius of the point marker.
	 * <p>
	 * Defaults to: 4
	 */
	public void setRadius(Number radius) {
		this.radius = radius;
	}

	/**
	 * @see #setStates(States)
	 */
	public States getStates() {
		if (states == null) {
			states = new States();
		}
		return states;
	}

	public void setStates(States states) {
		this.states = states;
	}

	/**
	 * @see #setWidth(Number)
	 */
	public Number getWidth() {
		return width;
	}

	/**
	 * Image markers only. Set the image width explicitly. When using this
	 * option, a <code>height</code> must also be set.
	 * <p>
	 * Defaults to: null
	 */
	public void setWidth(Number width) {
		this.width = width;
	}

	/**
	 * @see #setSymbol(MarkerSymbol)
	 */
	public MarkerSymbol getSymbol() {
		return symbol;
	}

	public void setSymbol(MarkerSymbol symbol) {
		this.symbol = symbol;
	}
}