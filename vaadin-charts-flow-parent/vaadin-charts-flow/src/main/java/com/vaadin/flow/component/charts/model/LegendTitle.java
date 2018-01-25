package com.vaadin.flow.component.charts.model;

import javax.annotation.Generated;
/**
 * A title to be added on top of the legend.
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class LegendTitle extends AbstractConfigurationObject {

	private String text;

	public LegendTitle() {
	}

	public LegendTitle(String text) {
		this.text = text;
	}

	/**
	 * @see #setText(String)
	 */
	public String getText() {
		return text;
	}

	/**
	 * A text or HTML string for the title.
	 * <p>
	 * Defaults to: null
	 */
	public void setText(String text) {
		this.text = text;
	}
}