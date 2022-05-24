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

import com.vaadin.flow.component.charts.model.style.Color;

/**
 * <p>
 * A label on the axis next to the crosshair.
 * </p>
 *
 * <p>
 * In
 * <a href="http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
 * >styled mode</a>, the label is styled with the
 * <code>.highcharts-crosshair-label</code> class.
 * </p>
 */
public class CrosshairLabel extends AbstractConfigurationObject {

    private HorizontalAlign align;
    private Color backgroundColor;
    private Color borderColor;
    private Number borderRadius;
    private Number borderWidth;
    private String format;
    private String _fn_formatter;
    private Number padding;
    private Shape shape;
    private Boolean enabled;

    public CrosshairLabel() {
    }

    /**
     * @see #setAlign(HorizontalAlign)
     */
    public HorizontalAlign getAlign() {
        return align;
    }

    /**
     * Alignment of the label compared to the axis. Defaults to
     * <code>left</code> for right-side axes, <code>right</code> for left-side
     * axes and <code>center</code> for horizontal axes.
     */
    public void setAlign(HorizontalAlign align) {
        this.align = align;
    }

    /**
     * @see #setBackgroundColor(Color)
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * The background color for the label. Defaults to the related series color,
     * or <code>#666666</code> if that is not available.
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * @see #setBorderColor(Color)
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * The border color for the crosshair label
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * @see #setBorderRadius(Number)
     */
    public Number getBorderRadius() {
        return borderRadius;
    }

    /**
     * The border corner radius of the crosshair label.
     * <p>
     * Defaults to: 3
     */
    public void setBorderRadius(Number borderRadius) {
        this.borderRadius = borderRadius;
    }

    /**
     * @see #setBorderWidth(Number)
     */
    public Number getBorderWidth() {
        return borderWidth;
    }

    /**
     * The border width for the crosshair label.
     * <p>
     * Defaults to: 0
     */
    public void setBorderWidth(Number borderWidth) {
        this.borderWidth = borderWidth;
    }

    /**
     * @see #setFormat(String)
     */
    public String getFormat() {
        return format;
    }

    /**
     * A format string for the crosshair label. Defaults to <code>{value}</code>
     * for numeric axes and <code>{value:%b %d, %Y}</code> for datetime axes.
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
     * @see #setPadding(Number)
     */
    public Number getPadding() {
        return padding;
    }

    /**
     * Padding inside the crosshair label.
     * <p>
     * Defaults to: 8
     */
    public void setPadding(Number padding) {
        this.padding = padding;
    }

    /**
     * @see #setShape(Shape)
     */
    public Shape getShape() {
        return shape;
    }

    /**
     * The shape to use for the label box.
     * <p>
     * Defaults to: callout
     */
    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public CrosshairLabel(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
