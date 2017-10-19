package com.vaadin.addon.charts.model;

import com.vaadin.addon.charts.model.style.Style;
/**
 * A title to be added on top of the legend.
 */
public class LegendTitle extends AbstractConfigurationObject {

	private Style style;
	private String text;

	public LegendTitle() {
	}

	/**
	 * @see #setStyle(Style)
	 */
	public Style getStyle() {
		if (style == null) {
			style = new Style();
		}
		return style;
	}

	/**
	 * Generic CSS styles for the legend title.
	 * <p>
	 * Defaults to: {"fontWeight":"bold"}
	 */
	public void setStyle(Style style) {
		this.style = style;
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