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
import com.vaadin.flow.component.charts.model.style.Style;

/**
 * Highchart by default puts a credits label in the lower right corner of the
 * chart. This can be changed using these options.
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class Credits extends AbstractConfigurationObject {

	private Boolean enabled;
	private String href;
	private Position position;
	private Style style;
	private String text;

	public Credits() {
	}

	public Credits(Boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @see #setEnabled(Boolean)
	 */
	public Boolean getEnabled() {
		return enabled;
	}

	/**
	 * Whether to show the credits text.
	 * <p>
	 * Defaults to: true
	 */
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @see #setHref(String)
	 */
	public String getHref() {
		return href;
	}

	/**
	 * The URL for the credits label.
	 * <p>
	 * Defaults to: http://www.highcharts.com
	 */
	public void setHref(String href) {
		this.href = href;
	}

	/**
	 * @see #setPosition(Position)
	 */
	public Position getPosition() {
		if (position == null) {
			position = new Position();
		}
		return position;
	}

	/**
	 * Position configuration for the credits label.
	 */
	public void setPosition(Position position) {
		this.position = position;
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
	 * CSS styles for the credits label.
	 * <p>
	 * Defaults to: { "cursor": "pointer", "color": "#999999", "fontSize":
	 * "10px" }
	 */
	public void setStyle(Style style) {
		this.style = style;
	}

	public Credits(String text) {
		this.text = text;
	}

	/**
	 * @see #setText(String)
	 */
	public String getText() {
		return text;
	}

	/**
	 * The text for the credits label.
	 * <p>
	 * Defaults to: Highcharts.com
	 */
	public void setText(String text) {
		this.text = text;
	}
}
