package com.vaadin.addon.charts.model;
/**
 * A title to be added on top of the legend.
 */
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