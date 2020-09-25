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
