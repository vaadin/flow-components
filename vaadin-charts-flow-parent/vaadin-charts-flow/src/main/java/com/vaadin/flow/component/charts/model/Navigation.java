package com.vaadin.flow.component.charts.model;

import javax.annotation.Generated;
/**
 * A collection of options for buttons and menus appearing in the exporting
 * module.
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class Navigation extends AbstractConfigurationObject {

	private ButtonOptions buttonOptions;

	public Navigation() {
	}

	/**
	 * @see #setButtonOptions(ButtonOptions)
	 */
	public ButtonOptions getButtonOptions() {
		if (buttonOptions == null) {
			buttonOptions = new ButtonOptions();
		}
		return buttonOptions;
	}

	/**
	 * <p>
	 * A collection of options for buttons appearing in the exporting module.
	 * </p>
	 * 
	 * <p>
	 * In <a href=
	 * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
	 * >styled mode</a>, the buttons are styled with the
	 * <code>.highcharts-contextbutton</code> and
	 * <code>.highcharts-button-symbol</code> class.
	 * </p>
	 */
	public void setButtonOptions(ButtonOptions buttonOptions) {
		this.buttonOptions = buttonOptions;
	}
}