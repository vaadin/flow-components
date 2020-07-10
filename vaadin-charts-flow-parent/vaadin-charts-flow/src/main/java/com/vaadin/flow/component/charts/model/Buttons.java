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
