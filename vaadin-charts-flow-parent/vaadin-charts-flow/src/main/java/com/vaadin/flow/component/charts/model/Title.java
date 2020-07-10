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
 * The chart's main title.
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class Title extends AbstractConfigurationObject {

	private HorizontalAlign align;
	private Boolean floating;
	private Number margin;
	private Style style;
	private String text;
	private Boolean useHTML;
	private VerticalAlign verticalAlign;
	private Number widthAdjust;
	private Number x;
	private Number y;

	public Title() {
	}

	/**
	 * @see #setAlign(HorizontalAlign)
	 */
	public HorizontalAlign getAlign() {
		return align;
	}

	/**
	 * The horizontal alignment of the title. Can be one of "left", "center" and
	 * "right".
	 * <p>
	 * Defaults to: center
	 */
	public void setAlign(HorizontalAlign align) {
		this.align = align;
	}

	/**
	 * @see #setFloating(Boolean)
	 */
	public Boolean getFloating() {
		return floating;
	}

	/**
	 * When the title is floating, the plot area will not move to make space for
	 * it.
	 * <p>
	 * Defaults to: false
	 */
	public void setFloating(Boolean floating) {
		this.floating = floating;
	}

	/**
	 * @see #setMargin(Number)
	 */
	public Number getMargin() {
		return margin;
	}

	/**
	 * The margin between the title and the plot area, or if a subtitle is
	 * present, the margin between the subtitle and the plot area.
	 * <p>
	 * Defaults to: 15
	 */
	public void setMargin(Number margin) {
		this.margin = margin;
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
	 * <p>
	 * CSS styles for the title. Use this for font styling, but use
	 * <code>align</code>, <code>x</code> and <code>y</code> for text alignment.
	 * </p>
	 * 
	 * <p>
	 * In <a href=
	 * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
	 * >styled mode</a>, the title style is given in the
	 * <code>.highcharts-title</code> class.
	 * </p>
	 * <p>
	 * Defaults to: { "color": "#333333", "fontSize": "18px" }
	 */
	public void setStyle(Style style) {
		this.style = style;
	}

	public Title(String text) {
		this.text = text;
	}

	/**
	 * @see #setText(String)
	 */
	public String getText() {
		return text;
	}

	/**
	 * The title of the chart. To disable the title, set the <code>text</code>
	 * to <code>null</code>.
	 * <p>
	 * Defaults to: Chart title
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @see #setUseHTML(Boolean)
	 */
	public Boolean getUseHTML() {
		return useHTML;
	}

	/**
	 * Whether to <a href=
	 * "http://www.highcharts.com/docs/chart-concepts/labels-and-string-formatting#html"
	 * >use HTML</a> to render the text.
	 * <p>
	 * Defaults to: false
	 */
	public void setUseHTML(Boolean useHTML) {
		this.useHTML = useHTML;
	}

	/**
	 * @see #setVerticalAlign(VerticalAlign)
	 */
	public VerticalAlign getVerticalAlign() {
		return verticalAlign;
	}

	/**
	 * The vertical alignment of the title. Can be one of <code>"top"</code>,
	 * <code>"middle"</code> and <code>"bottom"</code>. When a value is given,
	 * the title behaves as if <a href="#title.floating">floating</a> were
	 * <code>true</code>.
	 */
	public void setVerticalAlign(VerticalAlign verticalAlign) {
		this.verticalAlign = verticalAlign;
	}

	/**
	 * @see #setWidthAdjust(Number)
	 */
	public Number getWidthAdjust() {
		return widthAdjust;
	}

	/**
	 * Adjustment made to the title width, normally to reserve space for the
	 * exporting burger menu.
	 * <p>
	 * Defaults to: -44
	 */
	public void setWidthAdjust(Number widthAdjust) {
		this.widthAdjust = widthAdjust;
	}

	/**
	 * @see #setX(Number)
	 */
	public Number getX() {
		return x;
	}

	/**
	 * The x position of the title relative to the alignment within
	 * chart.spacingLeft and chart.spacingRight.
	 * <p>
	 * Defaults to: 0
	 */
	public void setX(Number x) {
		this.x = x;
	}

	/**
	 * @see #setY(Number)
	 */
	public Number getY() {
		return y;
	}

	/**
	 * The y position of the title relative to the alignment within <a
	 * href="#chart.spacingTop">chart.spacingTop</a> and <a
	 * href="#chart.spacingBottom">chart.spacingBottom</a>. By default it
	 * depends on the font size.
	 */
	public void setY(Number y) {
		this.y = y;
	}
}
