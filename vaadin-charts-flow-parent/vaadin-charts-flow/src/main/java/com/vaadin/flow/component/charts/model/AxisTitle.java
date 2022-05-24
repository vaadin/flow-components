package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.vaadin.flow.component.charts.model.style.Style;

/**
 * The axis title, showing next to the axis line.
 */
public class AxisTitle extends AbstractConfigurationObject {

    private VerticalAlign align;
    private Number margin;
    private Number offset;
    private Boolean reserveSpace;
    private Number rotation;
    private Style style;
    private String text;
    private Number x;
    private Number y;

    public AxisTitle() {
    }

    /**
     * @see #setAlign(VerticalAlign)
     */
    public VerticalAlign getAlign() {
        return align;
    }

    /**
     * Alignment of the title relative to the axis values. Possible values are
     * "low", "middle" or "high".
     * <p>
     * Defaults to: middle
     */
    public void setAlign(VerticalAlign align) {
        this.align = align;
    }

    /**
     * @see #setMargin(Number)
     */
    public Number getMargin() {
        return margin;
    }

    /**
     * The pixel distance between the axis labels and the title. Positive values
     * are outside the axis line, negative are inside.
     * <p>
     * Defaults to: 40
     */
    public void setMargin(Number margin) {
        this.margin = margin;
    }

    /**
     * @see #setOffset(Number)
     */
    public Number getOffset() {
        return offset;
    }

    /**
     * The distance of the axis title from the axis line. By default, this
     * distance is computed from the offset width of the labels, the labels'
     * distance from the axis and the title's margin. However when the offset
     * option is set, it overrides all this.
     */
    public void setOffset(Number offset) {
        this.offset = offset;
    }

    /**
     * @see #setReserveSpace(Boolean)
     */
    public Boolean getReserveSpace() {
        return reserveSpace;
    }

    /**
     * Whether to reserve space for the title when laying out the axis.
     * <p>
     * Defaults to: true
     */
    public void setReserveSpace(Boolean reserveSpace) {
        this.reserveSpace = reserveSpace;
    }

    /**
     * @see #setRotation(Number)
     */
    public Number getRotation() {
        return rotation;
    }

    /**
     * The rotation of the text in degrees. 0 is horizontal, 270 is vertical
     * reading from bottom to top.
     * <p>
     * Defaults to: 270
     */
    public void setRotation(Number rotation) {
        this.rotation = rotation;
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
     * CSS styles for the title. When titles are rotated they are rendered using
     * vector graphic techniques and not all styles are applicable.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the stroke width is given in the
     * <code>.highcharts-axis-title</code> class.
     * </p>
     * <p>
     * Defaults to: { "color": "#666666" }
     */
    public void setStyle(Style style) {
        this.style = style;
    }

    public AxisTitle(String text) {
        this.text = text;
    }

    /**
     * @see #setText(String)
     */
    public String getText() {
        return text;
    }

    /**
     * The actual text of the axis title. Horizontal texts can contain HTML, but
     * rotated texts are painted using vector techniques and must be clean text.
     * The Y axis title is disabled by setting the <code>text</code> option to
     * <code>null</code>.
     * <p>
     * Defaults to: Values
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @see #setX(Number)
     */
    public Number getX() {
        return x;
    }

    /**
     * Horizontal pixel offset of the title position.
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
     * Vertical pixel offset of the title position.
     */
    public void setY(Number y) {
        this.y = y;
    }
}
