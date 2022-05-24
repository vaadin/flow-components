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
 * The stack labels show the total value for each bar in a stacked column or bar
 * chart. The label will be placed on top of positive columns and below negative
 * columns. In case of an inverted column chart or a bar chart the label is
 * placed to the right of positive bars and to the left of negative bars.
 */
public class StackLabels extends AbstractConfigurationObject {

    private HorizontalAlign align;
    private Boolean enabled;
    private String format;
    private String _fn_formatter;
    private Number rotation;
    private Style style;
    private String textAlign;
    private Boolean useHTML;
    private VerticalAlign verticalAlign;
    private Number x;
    private Number y;

    public StackLabels() {
    }

    /**
     * @see #setAlign(HorizontalAlign)
     */
    public HorizontalAlign getAlign() {
        return align;
    }

    /**
     * Defines the horizontal alignment of the stack total label. Can be one of
     * <code>"left"</code>, <code>"center"</code> or <code>"right"</code>. The
     * default value is calculated at runtime and depends on orientation and
     * whether the stack is positive or negative.
     */
    public void setAlign(HorizontalAlign align) {
        this.align = align;
    }

    public StackLabels(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Enable or disable the stack total labels.
     * <p>
     * Defaults to: false
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setFormat(String)
     */
    public String getFormat() {
        return format;
    }

    /**
     * A <a href="http://docs.highcharts.com/#formatting">format string</a> for
     * the data label. Available variables are the same as for
     * <code>formatter</code>.
     * <p>
     * Defaults to: {total}
     */
    public void setFormat(String format) {
        this.format = format;
    }

    public String getFormatter() {
        return _fn_formatter;
    }

    public void setFormatter(String _fn_formatter) {
        this._fn_formatter = _fn_formatter;
    }

    /**
     * @see #setRotation(Number)
     */
    public Number getRotation() {
        return rotation;
    }

    /**
     * Rotation of the labels in degrees.
     * <p>
     * Defaults to: 0
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
     * CSS styles for the label.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the styles are set in the
     * <code>.highcharts-stack-label</code> class.
     * </p>
     * <p>
     * Defaults to: { "color": "#000000", "fontSize": "11px", "fontWeight":
     * "bold", "textShadow": "1px 1px contrast, -1px -1px contrast, -1px 1px
     * contrast, 1px -1px contrast" }
     */
    public void setStyle(Style style) {
        this.style = style;
    }

    /**
     * @see #setTextAlign(String)
     */
    public String getTextAlign() {
        return textAlign;
    }

    /**
     * The text alignment for the label. While <code>align</code> determines
     * where the texts anchor point is placed with regards to the stack,
     * <code>textAlign</code> determines how the text is aligned against its
     * anchor point. Possible values are <code>"left"</code>,
     * <code>"center"</code> and <code>"right"</code>. The default value is
     * calculated at runtime and depends on orientation and whether the stack is
     * positive or negative.
     */
    public void setTextAlign(String textAlign) {
        this.textAlign = textAlign;
    }

    /**
     * @see #setUseHTML(Boolean)
     */
    public Boolean getUseHTML() {
        return useHTML;
    }

    /**
     * Whether to <a href="http://docs.highcharts.com/#formatting$html">use
     * HTML</a> to render the labels.
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
     * Defines the vertical alignment of the stack total label. Can be one of
     * <code>"top"</code>, <code>"middle"</code> or <code>"bottom"</code>. The
     * default value is calculated at runtime and depends on orientation and
     * whether the stack is positive or negative.
     */
    public void setVerticalAlign(VerticalAlign verticalAlign) {
        this.verticalAlign = verticalAlign;
    }

    /**
     * @see #setX(Number)
     */
    public Number getX() {
        return x;
    }

    /**
     * The x position offset of the label relative to the left of the stacked
     * bar. The default value is calculated at runtime and depends on
     * orientation and whether the stack is positive or negative.
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
     * The y position offset of the label relative to the tick position on the
     * axis. The default value is calculated at runtime and depends on
     * orientation and whether the stack is positive or negative.
     */
    public void setY(Number y) {
        this.y = y;
    }
}
