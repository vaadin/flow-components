package com.vaadin.flow.component.charts.model;

import javax.annotation.Generated;
/**
 * The appearance of the point marker when selected. In order to allow a point
 * to be selected, set the <code>series.allowPointSelect</code> option to true.
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class Select extends AbstractConfigurationObject {

	private Boolean enabled;
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