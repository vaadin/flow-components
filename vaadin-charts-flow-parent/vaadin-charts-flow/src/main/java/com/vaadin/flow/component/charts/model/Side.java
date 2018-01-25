package com.vaadin.flow.component.charts.model;

import javax.annotation.Generated;
import com.vaadin.flow.component.charts.model.style.Color;
/**
 * <p>
 * Note: As of v5.0.12, <code>frame.left</code> or <code>frame.right</code>
 * should be used instead.
 * </p>
 * 
 * <p>
 * The side for the frame around a 3D chart.
 * </p>
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class Side extends AbstractConfigurationObject {

	private Color color;
	private Number size;

	public Side() {
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
	 * The thickness of the panel.
	 * <p>
	 * Defaults to: 1
	 */
	public void setSize(Number size) {
		this.size = size;
	}
}