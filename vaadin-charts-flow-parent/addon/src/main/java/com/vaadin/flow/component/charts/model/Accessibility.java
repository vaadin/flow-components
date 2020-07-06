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
 * Options for configuring accessibility for the chart. Requires the <a
 * href="//code.highcharts.com/modules/accessibility.js">accessibility
 * module</a> to be loaded. For a description of the module and information on
 * its features, see <a
 * href="http://www.highcharts.com/docs/chart-concepts/accessibility">Highcharts
 * Accessibility</a>.
 * </p>
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class Accessibility extends AbstractConfigurationObject {

	private Boolean describeSingleSeries;
	private Boolean enabled;
	private KeyboardNavigation keyboardNavigation;
	private String _fn_onTableAnchorClick;
	private String pointDateFormat;
	private String _fn_pointDateFormatter;
	private String _fn_pointDescriptionFormatter;
	private String _fn_screenReaderSectionFormatter;
	private String _fn_seriesDescriptionFormatter;

	public Accessibility() {
	}

	/**
	 * @see #setDescribeSingleSeries(Boolean)
	 */
	public Boolean getDescribeSingleSeries() {
		return describeSingleSeries;
	}

	/**
	 * Whether or not to add series descriptions to charts with a single series.
	 * <p>
	 * Defaults to: false
	 */
	public void setDescribeSingleSeries(Boolean describeSingleSeries) {
		this.describeSingleSeries = describeSingleSeries;
	}

	public Accessibility(Boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @see #setEnabled(Boolean)
	 */
	public Boolean getEnabled() {
		return enabled;
	}

	/**
	 * Enable accessibility features for the chart.
	 * <p>
	 * Defaults to: true
	 */
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @see #setKeyboardNavigation(KeyboardNavigation)
	 */
	public KeyboardNavigation getKeyboardNavigation() {
		if (keyboardNavigation == null) {
			keyboardNavigation = new KeyboardNavigation();
		}
		return keyboardNavigation;
	}

	/**
	 * Options for keyboard navigation.
	 */
	public void setKeyboardNavigation(KeyboardNavigation keyboardNavigation) {
		this.keyboardNavigation = keyboardNavigation;
	}

	public String getOnTableAnchorClick() {
		return _fn_onTableAnchorClick;
	}

	public void setOnTableAnchorClick(String _fn_onTableAnchorClick) {
		this._fn_onTableAnchorClick = _fn_onTableAnchorClick;
	}

	/**
	 * @see #setPointDateFormat(String)
	 */
	public String getPointDateFormat() {
		return pointDateFormat;
	}

	/**
	 * <p>
	 * Date format to use for points on datetime axes when describing them to
	 * screen reader users.
	 * </p>
	 * <p>
	 * Defaults to the same format as in tooltip.
	 * </p>
	 * <p>
	 * For an overview of the replacement codes, see <a
	 * href="#Highcharts.dateFormat">dateFormat</a>.
	 * </p>
	 */
	public void setPointDateFormat(String pointDateFormat) {
		this.pointDateFormat = pointDateFormat;
	}

	public String getPointDateFormatter() {
		return _fn_pointDateFormatter;
	}

	public void setPointDateFormatter(String _fn_pointDateFormatter) {
		this._fn_pointDateFormatter = _fn_pointDateFormatter;
	}

	public String getPointDescriptionFormatter() {
		return _fn_pointDescriptionFormatter;
	}

	public void setPointDescriptionFormatter(
			String _fn_pointDescriptionFormatter) {
		this._fn_pointDescriptionFormatter = _fn_pointDescriptionFormatter;
	}

	public String getScreenReaderSectionFormatter() {
		return _fn_screenReaderSectionFormatter;
	}

	public void setScreenReaderSectionFormatter(
			String _fn_screenReaderSectionFormatter) {
		this._fn_screenReaderSectionFormatter = _fn_screenReaderSectionFormatter;
	}

	public String getSeriesDescriptionFormatter() {
		return _fn_seriesDescriptionFormatter;
	}

	public void setSeriesDescriptionFormatter(
			String _fn_seriesDescriptionFormatter) {
		this._fn_seriesDescriptionFormatter = _fn_seriesDescriptionFormatter;
	}
}
