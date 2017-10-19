package com.vaadin.addon.charts.model;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2012 - 2016 Vaadin Ltd
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

import com.vaadin.addon.charts.model.style.Style;
/**
 * A HTML label that can be positioned anywhere in the chart area.
 */
public class Items extends AbstractConfigurationObject {

	private static final long serialVersionUID = 1L;
	private String html;
	private Style style;

	public Items() {
	}

	/**
	 * @see #setHtml(String)
	 */
	public String getHtml() {
		return html;
	}

	/**
	 * Inner HTML or text for the label.
	 */
	public void setHtml(String html) {
		this.html = html;
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
	 * CSS styles for each label. To position the label, use left and top like
	 * this:
	 *
	 * <pre>
	 * style: {
	 * 		left: '100px',
	 * 		top: '100px'
	 * 	}
	 * </pre>
	 */
	public void setStyle(Style style) {
		this.style = style;
	}
}