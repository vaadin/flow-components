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
import com.vaadin.flow.component.charts.model.style.Style;

/**
 * Extended data labels for range series types. Range series data labels have no
 * <code>x</code> and <code>y</code> options. Instead, they have
 * <code>xLow</code>, <code>xHigh</code>, <code>yLow</code> and
 * <code>yHigh</code> options to allow the higher and lower data label sets
 * individually.
 */
public class DataLabelsRange extends AbstractDataLabels {

    private HorizontalAlign align;
    private Boolean allowOverlap;
    private Color backgroundColor;
    private Color borderColor;
    private Number borderRadius;
    private Number borderWidth;
    private String className;
    private Color color;
    private Boolean crop;
    private Boolean defer;
    private Boolean enabled;
    private String format;
    private String _fn_formatter;
    private Boolean inside;
    private String overflow;
    private Number padding;
    private Number rotation;
    private Boolean shadow;
    private Shape shape;
    private Style style;
    private Boolean useHTML;
    private VerticalAlign verticalAlign;
    private Number xHigh;
    private Number xLow;
    private Number yHigh;
    private Number yLow;
    private Number zIndex;

    public DataLabelsRange() {
    }

    /**
     * @see #setAlign(HorizontalAlign)
     */
    public HorizontalAlign getAlign() {
        return align;
    }

    /**
     * The alignment of the data label compared to the point. If
     * <code>right</code>, the right side of the label should be touching the
     * point. For points with an extent, like columns, the alignments also
     * dictates how to align it inside the box, as given with the
     * <a href="#plotOptions.column.dataLabels.inside">inside</a> option. Can be
     * one of "left", "center" or "right".
     * <p>
     * Defaults to: center
     */
    public void setAlign(HorizontalAlign align) {
        this.align = align;
    }

    /**
     * @see #setAllowOverlap(Boolean)
     */
    public Boolean getAllowOverlap() {
        return allowOverlap;
    }

    /**
     * Whether to allow data labels to overlap. To make the labels less
     * sensitive for overlapping, the
     * <a href="#plotOptions.series.dataLabels.padding">dataLabels.padding</a>
     * can be set to 0.
     * <p>
     * Defaults to: false
     */
    public void setAllowOverlap(Boolean allowOverlap) {
        this.allowOverlap = allowOverlap;
    }

    /**
     * @see #setBackgroundColor(Color)
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * The background color or gradient for the data label. Defaults to
     * <code>undefined</code>.
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
     * The border color for the data label. Defaults to <code>undefined</code>.
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
     * The border radius in pixels for the data label.
     * <p>
     * Defaults to: 0
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
     * The border width in pixels for the data label.
     * <p>
     * Defaults to: 0
     */
    public void setBorderWidth(Number borderWidth) {
        this.borderWidth = borderWidth;
    }

    /**
     * @see #setClassName(String)
     */
    public String getClassName() {
        return className;
    }

    /**
     * A class name for the data label. Particularly in <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, this can be used to give each series' or point's data
     * label unique styling. In addition to this option, a default color class
     * name is added so that we can give the labels a <a href=
     * "http://jsfiddle.net/gh/get/library/pure/highcharts/highcharts/tree/master/samples/highcharts/css/data-label-contrast/"
     * >contrast text shadow</a>.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @see #setColor(Color)
     */
    public Color getColor() {
        return color;
    }

    /**
     * The text color for the data labels. Defaults to <code>null</code>.
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * @see #setCrop(Boolean)
     */
    public Boolean getCrop() {
        return crop;
    }

    /**
     * Whether to hide data labels that are outside the plot area. By default,
     * the data label is moved inside the plot area according to the
     * <a href="#plotOptions.series.dataLabels.overflow">overflow</a> option.
     * <p>
     * Defaults to: true
     */
    public void setCrop(Boolean crop) {
        this.crop = crop;
    }

    /**
     * @see #setDefer(Boolean)
     */
    public Boolean getDefer() {
        return defer;
    }

    /**
     * Whether to defer displaying the data labels until the initial series
     * animation has finished.
     * <p>
     * Defaults to: true
     */
    public void setDefer(Boolean defer) {
        this.defer = defer;
    }

    public DataLabelsRange(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Enable or disable the data labels.
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
     * A <a href=
     * "http://www.highcharts.com/docs/chart-concepts/labels-and-string-formatting"
     * >format string</a> for the data label. Available variables are the same
     * as for <code>formatter</code>.
     * <p>
     * Defaults to: {y}
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
     * @see #setInside(Boolean)
     */
    public Boolean getInside() {
        return inside;
    }

    /**
     * For points with an extent, like columns, whether to align the data label
     * inside the box or to the actual value point. Defaults to
     * <code>false</code> in most cases, <code>true</code> in stacked columns.
     */
    public void setInside(Boolean inside) {
        this.inside = inside;
    }

    /**
     * @see #setOverflow(String)
     */
    public String getOverflow() {
        return overflow;
    }

    /**
     * How to handle data labels that flow outside the plot area. The default is
     * <code>justify</code>, which aligns them inside the plot area. For columns
     * and bars, this means it will be moved inside the bar. To display data
     * labels outside the plot area, set <code>crop</code> to <code>false</code>
     * and <code>overflow</code> to <code>"none"</code>.
     * <p>
     * Defaults to: justify
     */
    public void setOverflow(String overflow) {
        this.overflow = overflow;
    }

    /**
     * @see #setPadding(Number)
     */
    public Number getPadding() {
        return padding;
    }

    /**
     * When either the <code>borderWidth</code> or the
     * <code>backgroundColor</code> is set, this is the padding within the box.
     * <p>
     * Defaults to: 5
     */
    public void setPadding(Number padding) {
        this.padding = padding;
    }

    /**
     * @see #setRotation(Number)
     */
    public Number getRotation() {
        return rotation;
    }

    /**
     * Text rotation in degrees. Note that due to a more complex structure,
     * backgrounds, borders and padding will be lost on a rotated data label.
     * <p>
     * Defaults to: 0
     */
    public void setRotation(Number rotation) {
        this.rotation = rotation;
    }

    /**
     * @see #setShadow(Boolean)
     */
    public Boolean getShadow() {
        return shadow;
    }

    /**
     * The shadow of the box. Works best with <code>borderWidth</code> or
     * <code>backgroundColor</code>. Since 2.3 the shadow can be an object
     * configuration containing <code>color</code>, <code>offsetX</code>,
     * <code>offsetY</code>, <code>opacity</code> and <code>width</code>.
     * <p>
     * Defaults to: false
     */
    public void setShadow(Boolean shadow) {
        this.shadow = shadow;
    }

    /**
     * @see #setShape(Shape)
     */
    public Shape getShape() {
        return shape;
    }

    /**
     * The name of a symbol to use for the border around the label. Symbols are
     * predefined functions on the Renderer object.
     * <p>
     * Defaults to: square
     */
    public void setShape(Shape shape) {
        this.shape = shape;
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
     * Styles for the label. The default <code>color</code> setting is
     * <code>"contrast"</code>, which is a pseudo color that Highcharts picks up
     * and applies the maximum contrast to the underlying point item, for
     * example the bar in a bar chart. The <code>textOutline</code> is a pseudo
     * property that applies an outline of the given width with the given color,
     * which by default is the maximum contrast to the text. So a bright text
     * color will result in a black text outline for maximum readability on a
     * mixed background. In some cases, especially with grayscale text, the text
     * outline doesn't work well, in which cases it can be disabled by setting
     * it to <code>"none"</code>.
     * <p>
     * Defaults to: {"color": "contrast", "fontSize": "11px", "fontWeight":
     * "bold", "textOutline": "1px contrast" }
     */
    public void setStyle(Style style) {
        this.style = style;
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
     * >use HTML</a> to render the labels.
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
     * The vertical alignment of a data label. Can be one of <code>top</code>,
     * <code>middle</code> or <code>bottom</code>. The default value depends on
     * the data, for instance in a column chart, the label is above positive
     * values and below negative values.
     */
    public void setVerticalAlign(VerticalAlign verticalAlign) {
        this.verticalAlign = verticalAlign;
    }

    /**
     * @see #setXHigh(Number)
     */
    public Number getXHigh() {
        return xHigh;
    }

    /**
     * X offset of the higher data labels relative to the point value.
     * <p>
     * Defaults to: 0
     */
    public void setXHigh(Number xHigh) {
        this.xHigh = xHigh;
    }

    /**
     * @see #setXLow(Number)
     */
    public Number getXLow() {
        return xLow;
    }

    /**
     * X offset of the lower data labels relative to the point value.
     * <p>
     * Defaults to: 0
     */
    public void setXLow(Number xLow) {
        this.xLow = xLow;
    }

    /**
     * @see #setYHigh(Number)
     */
    public Number getYHigh() {
        return yHigh;
    }

    /**
     * Y offset of the higher data labels relative to the point value.
     * <p>
     * Defaults to: -6
     */
    public void setYHigh(Number yHigh) {
        this.yHigh = yHigh;
    }

    /**
     * @see #setYLow(Number)
     */
    public Number getYLow() {
        return yLow;
    }

    /**
     * Y offset of the lower data labels relative to the point value.
     * <p>
     * Defaults to: 16
     */
    public void setYLow(Number yLow) {
        this.yLow = yLow;
    }

    /**
     * @see #setZIndex(Number)
     */
    public Number getZIndex() {
        return zIndex;
    }

    /**
     * The Z index of the data labels. The default Z index puts it above the
     * series. Use a Z index of 2 to display it behind the series.
     * <p>
     * Defaults to: 6
     */
    public void setZIndex(Number zIndex) {
        this.zIndex = zIndex;
    }
}
