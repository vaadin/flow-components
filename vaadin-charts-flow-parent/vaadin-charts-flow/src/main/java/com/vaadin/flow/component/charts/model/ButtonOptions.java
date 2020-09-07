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
 * <p>
 * A collection of options for buttons appearing in the exporting module.
 * </p>
 * 
 * <p>
 * In <a
 * href="http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
 * >styled mode</a>, the buttons are styled with the
 * <code>.highcharts-contextbutton</code> and
 * <code>.highcharts-button-symbol</code> class.
 * </p>
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class ButtonOptions extends AbstractConfigurationObject {

	private HorizontalAlign align;
	private Boolean enabled;
	private Number height;
	private Number symbolSize;
	private Number symbolStrokeWidth;
	private Number symbolX;
	private Number symbolY;
	private String text;
	private VerticalAlign verticalAlign;
	private Number width;
	private Number y;

	public ButtonOptions() {
	}

	/**
	 * @see #setAlign(HorizontalAlign)
	 */
	public HorizontalAlign getAlign() {
		return align;
	}

	/**
	 * Alignment for the buttons.
	 * <p>
	 * Defaults to: right
	 */
	public void setAlign(HorizontalAlign align) {
		this.align = align;
	}

	public ButtonOptions(Boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @see #setEnabled(Boolean)
	 */
	public Boolean getEnabled() {
		return enabled;
	}

	/**
	 * Whether to enable buttons.
	 * <p>
	 * Defaults to: true
	 */
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @see #setHeight(Number)
	 */
	public Number getHeight() {
		return height;
	}

	/**
	 * Pixel height of the buttons.
	 * <p>
	 * Defaults to: 20
	 */
	public void setHeight(Number height) {
		this.height = height;
	}

	/**
	 * @see #setSymbolSize(Number)
	 */
	public Number getSymbolSize() {
		return symbolSize;
	}

	/**
	 * The pixel size of the symbol on the button.
	 * <p>
	 * Defaults to: 14
	 */
	public void setSymbolSize(Number symbolSize) {
		this.symbolSize = symbolSize;
	}

	/**
	 * @see #setSymbolStrokeWidth(Number)
	 */
	public Number getSymbolStrokeWidth() {
		return symbolStrokeWidth;
	}

	/**
	 * The pixel stroke width of the symbol on the button.
	 * <p>
	 * Defaults to: 1
	 */
	public void setSymbolStrokeWidth(Number symbolStrokeWidth) {
		this.symbolStrokeWidth = symbolStrokeWidth;
	}

	/**
	 * @see #setSymbolX(Number)
	 */
	public Number getSymbolX() {
		return symbolX;
	}

	/**
	 * The x position of the center of the symbol inside the button.
	 * <p>
	 * Defaults to: 12.5
	 */
	public void setSymbolX(Number symbolX) {
		this.symbolX = symbolX;
	}

	/**
	 * @see #setSymbolY(Number)
	 */
	public Number getSymbolY() {
		return symbolY;
	}

	/**
	 * The y position of the center of the symbol inside the button.
	 * <p>
	 * Defaults to: 10.5
	 */
	public void setSymbolY(Number symbolY) {
		this.symbolY = symbolY;
	}

	public ButtonOptions(String text) {
		this.text = text;
	}

	/**
	 * @see #setText(String)
	 */
	public String getText() {
		return text;
	}

	/**
	 * A text string to add to the individual button.
	 * <p>
	 * Defaults to: null
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @see #setVerticalAlign(VerticalAlign)
	 */
	public VerticalAlign getVerticalAlign() {
		return verticalAlign;
	}

	/**
	 * The vertical alignment of the buttons. Can be one of "top", "middle" or
	 * "bottom".
	 * <p>
	 * Defaults to: top
	 */
	public void setVerticalAlign(VerticalAlign verticalAlign) {
		this.verticalAlign = verticalAlign;
	}

	/**
	 * @see #setWidth(Number)
	 */
	public Number getWidth() {
		return width;
	}

	/**
	 * The pixel width of the button.
	 * <p>
	 * Defaults to: 24
	 */
	public void setWidth(Number width) {
		this.width = width;
	}

	/**
	 * @see #setY(Number)
	 */
	public Number getY() {
		return y;
	}

	/**
	 * The vertical offset of the button's position relative to its
	 * <code>verticalAlign</code>. .
	 * <p>
	 * Defaults to: 0
	 */
	public void setY(Number y) {
		this.y = y;
	}
}
