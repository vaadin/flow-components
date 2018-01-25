package com.vaadin.flow.component.charts.model;

import javax.annotation.Generated;
/**
 * The loading options control the appearance of the loading screen that covers
 * the plot area on chart operations. This screen only appears after an explicit
 * call to <code>chart.showLoading()</code>. It is a utility for developers to
 * communicate to the end user that something is going on, for example while
 * retrieving new data via an XHR connection. The "Loading..." text itself is
 * not part of this configuration object, but part of the <code>lang</code>
 * object.
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class Loading extends AbstractConfigurationObject {

	private Number hideDuration;
	private Number showDuration;

	public Loading() {
	}

	/**
	 * @see #setHideDuration(Number)
	 */
	public Number getHideDuration() {
		return hideDuration;
	}

	/**
	 * The duration in milliseconds of the fade out effect.
	 * <p>
	 * Defaults to: 100
	 */
	public void setHideDuration(Number hideDuration) {
		this.hideDuration = hideDuration;
	}

	/**
	 * @see #setShowDuration(Number)
	 */
	public Number getShowDuration() {
		return showDuration;
	}

	/**
	 * The duration in milliseconds of the fade in effect.
	 * <p>
	 * Defaults to: 100
	 */
	public void setShowDuration(Number showDuration) {
		this.showDuration = showDuration;
	}
}