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
 * The chart's subtitle
 */
public class Subtitle extends AbstractConfigurationObject {

    private HorizontalAlign align;
    private Boolean floating;
    private Style style;
    private String text;
    private Boolean useHTML;
    private VerticalAlign verticalAlign;
    private Number widthAdjust;
    private Number x;
    private Number y;

    public Subtitle() {
    }

    /**
     * @see #setAlign(HorizontalAlign)
     */
    public HorizontalAlign getAlign() {
        return align;
    }

    /**
     * The horizontal alignment of the subtitle. Can be one of "left", "center"
     * and "right".
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
     * When the subtitle is floating, the plot area will not move to make space
     * for it.
     * <p>
     * Defaults to: false
     */
    public void setFloating(Boolean floating) {
        this.floating = floating;
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
     * CSS styles for the title.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the subtitle style is given in the
     * <code>.highcharts-subtitle</code> class.
     * </p>
     * <p>
     * Defaults to: { "color": "#666666" }
     */
    public void setStyle(Style style) {
        this.style = style;
    }

    public Subtitle(String text) {
        this.text = text;
    }

    /**
     * @see #setText(String)
     */
    public String getText() {
        return text;
    }

    /**
     * The subtitle of the chart.
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
     * The vertical alignment of the title. Can be one of "top", "middle" and
     * "bottom". When a value is given, the title behaves as floating.
     * <p>
     * Defaults to:
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
     * Adjustment made to the subtitle width, normally to reserve space for the
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
     * The x position of the subtitle relative to the alignment within
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
     * The y position of the subtitle relative to the alignment within
     * chart.spacingTop and chart.spacingBottom. By default the subtitle is laid
     * out below the title unless the title is floating.
     * <p>
     * Defaults to: null
     */
    public void setY(Number y) {
        this.y = y;
    }
}
