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
 * <p>
 * Options for the series data labels, appearing next to each data point.
 * </p>
 *
 * <p>
 * In
 * <a href="http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
 * >styled mode</a>, the data labels can be styled wtih the
 * <code>.highcharts-data-label-box</code> and
 * <code>.highcharts-data-label</code> class names (<a href=
 * "http://jsfiddle.net/gh/get/library/pure/highcharts/highcharts/tree/master/samples/highcharts/css/series-datalabels"
 * >see example</a>).
 * </p>
 */
public class DataLabels extends AbstractDataLabels {

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
    private Number x;
    private Number y;
    private Number zIndex;
    private Color connectorColor;
    private Number connectorPadding;
    private Number connectorWidth;
    private Number distance;
    private Boolean softConnector;

    public DataLabels() {
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

    public DataLabels(Boolean enabled) {
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
     * @see #setX(Number)
     */
    public Number getX() {
        return x;
    }

    /**
     * The x position offset of the label relative to the point.
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
     * The y position offset of the label relative to the point.
     * <p>
     * Defaults to: -6
     */
    public void setY(Number y) {
        this.y = y;
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

    /**
     * @see #setConnectorColor(Color)
     */
    public Color getConnectorColor() {
        return connectorColor;
    }

    /**
     * <p>
     * The color of the line connecting the data label to the pie slice. The
     * default color is the same as the point's color.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the connector stroke is given in the
     * <code>.highcharts-data-label-connector</code> class.
     * </p>
     * <p>
     * Defaults to: {point.color}
     */
    public void setConnectorColor(Color connectorColor) {
        this.connectorColor = connectorColor;
    }

    /**
     * @see #setConnectorPadding(Number)
     */
    public Number getConnectorPadding() {
        return connectorPadding;
    }

    /**
     * The distance from the data label to the connector.
     * <p>
     * Defaults to: 5
     */
    public void setConnectorPadding(Number connectorPadding) {
        this.connectorPadding = connectorPadding;
    }

    /**
     * @see #setConnectorWidth(Number)
     */
    public Number getConnectorWidth() {
        return connectorWidth;
    }

    /**
     * <p>
     * The width of the line connecting the data label to the pie slice.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the connector stroke width is given in the
     * <code>.highcharts-data-label-connector</code> class.
     * </p>
     * <p>
     * Defaults to: 1
     */
    public void setConnectorWidth(Number connectorWidth) {
        this.connectorWidth = connectorWidth;
    }

    /**
     * @see #setDistance(Number)
     */
    public Number getDistance() {
        return distance;
    }

    /**
     * The distance of the data label from the pie's edge. Negative numbers put
     * the data label on top of the pie slices. Connectors are only shown for
     * data labels outside the pie.
     * <p>
     * Defaults to: 30
     */
    public void setDistance(Number distance) {
        this.distance = distance;
    }

    /**
     * @see #setSoftConnector(Boolean)
     */
    public Boolean getSoftConnector() {
        return softConnector;
    }

    /**
     * Whether to render the connector as a soft arc or a line with sharp break.
     * <p>
     * Defaults to: true
     */
    public void setSoftConnector(Boolean softConnector) {
        this.softConnector = softConnector;
    }
}
