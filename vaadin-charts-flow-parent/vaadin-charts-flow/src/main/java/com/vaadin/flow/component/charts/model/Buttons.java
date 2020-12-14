package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2020 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 * 
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */


import javax.annotation.Generated;

/**
 * Options for the export related buttons, print and export. In addition to the
 * default buttons listed here, custom buttons can be added. See <a
 * href="#navigation.buttonOptions">navigation.buttonOptions</a> for general
 * options.
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class Buttons extends AbstractConfigurationObject {

	private ContextButton contextButton;

	public Buttons() {
	}

	/**
	 * @see #setContextButton(ContextButton)
	 */
	public ContextButton getContextButton() {
		if (contextButton == null) {
			contextButton = new ContextButton();
		}
		return contextButton;
	}

	/**
	 * <p>
	 * Options for the export button.
	 * </p>
	 * 
	 * <p>
	 * In <a href=
	 * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
	 * >styled mode</a>, export button styles can be applied with the
	 * <code>.highcharts-contextbutton</code> class.
	 * </p>
	 */
	public void setContextButton(ContextButton contextButton) {
		this.contextButton = contextButton;
	}
}
