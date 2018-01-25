package com.vaadin.flow.component.charts.model;

import javax.annotation.Generated;
import com.vaadin.flow.component.charts.model.style.Color;
/**
 * Defines the back panel of the frame around 3D charts.
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class Back extends AbstractConfigurationObject {

	private Color color;
	private Number size;

	public Back() {
	}

	/**
	 * @see #setColor(Color)
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * The color of the panel.
	 * <p>
	 * Defaults to: transparent
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * @see #setSize(Number)
	 */
	public Number getSize() {
		return size;
	}

	/**
	 * Thickness of the panel.
	 * <p>
	 * Defaults to: 1
	 */
	public void setSize(Number size) {
		this.size = size;
	}
}