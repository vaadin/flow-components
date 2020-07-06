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
 * Under which conditions the rule applies.
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class Condition extends AbstractConfigurationObject {

	private String _fn_callback;
	private Number maxHeight;
	private Number maxWidth;
	private Number minHeight;
	private Number minWidth;

	public Condition() {
	}

	public String getCallback() {
		return _fn_callback;
	}

	public void setCallback(String _fn_callback) {
		this._fn_callback = _fn_callback;
	}

	/**
	 * @see #setMaxHeight(Number)
	 */
	public Number getMaxHeight() {
		return maxHeight;
	}

	/**
	 * The responsive rule applies if the chart height is less than this.
	 */
	public void setMaxHeight(Number maxHeight) {
		this.maxHeight = maxHeight;
	}

	/**
	 * @see #setMaxWidth(Number)
	 */
	public Number getMaxWidth() {
		return maxWidth;
	}

	/**
	 * The responsive rule applies if the chart width is less than this.
	 */
	public void setMaxWidth(Number maxWidth) {
		this.maxWidth = maxWidth;
	}

	/**
	 * @see #setMinHeight(Number)
	 */
	public Number getMinHeight() {
		return minHeight;
	}

	/**
	 * The responsive rule applies if the chart height is greater than this.
	 * <p>
	 * Defaults to: 0
	 */
	public void setMinHeight(Number minHeight) {
		this.minHeight = minHeight;
	}

	/**
	 * @see #setMinWidth(Number)
	 */
	public Number getMinWidth() {
		return minWidth;
	}

	/**
	 * The responsive rule applies if the chart width is greater than this.
	 * <p>
	 * Defaults to: 0
	 */
	public void setMinWidth(Number minWidth) {
		this.minWidth = minWidth;
	}
}
