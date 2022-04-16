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
 * Options regarding the chart area and plot area as well as general chart
 * options.
 */
public class ChartModel extends AbstractConfigurationObject {

    private Boolean alignTicks;
    private Boolean animation;
    private Color backgroundColor;
    private Color borderColor;
    private Number borderRadius;
    private Number borderWidth;
    private String className;
    private Number colorCount;
    private String description;
    private String height;
    private Boolean ignoreHiddenSeries;
    private Boolean inverted;
    private Number[] margin;
    private Number marginBottom;
    private Number marginLeft;
    private Number marginRight;
    private Number marginTop;
    private Options3d options3d;
    private PanKey panKey;
    private Boolean panning;
    private Dimension pinchType;
    private Color plotBackgroundColor;
    private String plotBackgroundImage;
    private Color plotBorderColor;
    private Number plotBorderWidth;
    private Boolean plotShadow;
    private Boolean polar;
    private Boolean reflow;
    private ResetZoomButton resetZoomButton;
    private Color selectionMarkerFill;
    private Boolean shadow;
    private Boolean showAxes;
    private Number[] spacing;
    private Number spacingBottom;
    private Number spacingLeft;
    private Number spacingRight;
    private Number spacingTop;
    private Style style;
    private Boolean styledMode = Boolean.FALSE;
    private ChartType type;
    private String typeDescription;
    private Number width;
    private Dimension zoomType;

    public ChartModel() {
    }

    /**
     * @see #setAlignTicks(Boolean)
     */
    public Boolean getAlignTicks() {
        return alignTicks;
    }

    /**
     * <p>
     * When using multiple axis, the ticks of two or more opposite axes will
     * automatically be aligned by adding ticks to the axis or axes with the
     * least ticks, as if <code>tickAmount</code> were specified.
     * </p>
     *
     * <p>
     * This can be prevented by setting <code>alignTicks</code> to false. If the
     * grid lines look messy, it's a good idea to hide them for the secondary
     * axis by setting <code>gridLineWidth</code> to 0.
     * </p>
     * <p>
     * Defaults to: true
     */
    public void setAlignTicks(Boolean alignTicks) {
        this.alignTicks = alignTicks;
    }

    /**
     * @see #setAnimation(Boolean)
     */
    public Boolean getAnimation() {
        return animation;
    }

    /**
     * <p>
     * Set the overall animation for all chart updating. Animation can be
     * disabled throughout the chart by setting it to false here. It can be
     * overridden for each individual API method as a function parameter. The
     * only animation not affected by this option is the initial series
     * animation, see <a class="internal" href=
     * "#plotOptions.series.animation">plotOptions.series.animation</a>.
     * </p>
     *
     * <p>
     * The animation can either be set as a boolean or a configuration object.
     * If <code>true</code>, it will use the 'swing' jQuery easing and a
     * duration of 500 ms. If used as a configuration object, the following
     * properties are supported:
     * </p>
     * <dl>
     * <dt>duration</dt>
     * <dd>The duration of the animation in milliseconds.</dd>
     *
     * <dt>easing</dt>
     * <dd>A string reference to an easing function set on the <code>Math</code>
     * object. See <a href=
     * "http://jsfiddle.net/gh/get/library/pure/highcharts/highcharts/tree/master/samples/highcharts/plotoptions/series-animation-easing/"
     * >the easing demo</a>.</dd>
     * </dl>
     * <p>
     * Defaults to: true
     */
    public void setAnimation(Boolean animation) {
        this.animation = animation;
    }

    /**
     * @see #setBackgroundColor(Color)
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * The background color or gradient for the outer chart area.
     * <p>
     * Defaults to: #FFFFFF
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
     * The color of the outer chart border.
     * <p>
     * Defaults to: #335cad
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
     * The corner radius of the outer chart border.
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
     * The pixel width of the outer chart border.
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
     * A CSS class name to apply to the charts container <code>div</code>,
     * allowing unique CSS styling for each chart.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @see #setColorCount(Number)
     */
    public Number getColorCount() {
        return colorCount;
    }

    /**
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, this sets how many colors the class names should rotate
     * between. With ten colors, series (or points) are given class names like
     * <code>highcharts-color-0</code>, <code>highcharts-color-0</code> [...]
     * <code>highcharts-color-9</code>. The equivalent in non-styled mode is to
     * set colors using the <a href="#colors">colors</a> setting.
     * <p>
     * Defaults to: 10
     */
    public void setColorCount(Number colorCount) {
        this.colorCount = colorCount;
    }

    /**
     * @see #setDescription(String)
     */
    public String getDescription() {
        return description;
    }

    /**
     * <p>
     * A text description of the chart.
     * </p>
     *
     * <p>
     * If the Accessibility module is loaded, this is included by default as a
     * long description of the chart and its contents in the hidden screen
     * reader information region.
     * </p>
     * <p>
     * Defaults to: undefined
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see #setHeight(String)
     */
    public String getHeight() {
        return height;
    }

    /**
     * <p>
     * An explicit height for the chart. If a <em>number</em>, the height is
     * given in pixels. If given a <em>percentage string</em> (for example
     * <code>'56%'</code>), the height is given as the percentage of the actual
     * chart width. This allows for preserving the aspect ratio across
     * responsive sizes.
     * </p>
     *
     * <p>
     * By default (when <code>null</code>) the height is calculated from the
     * offset height of the containing element, or 400 pixels if the containing
     * element's height is 0.
     * </p>
     * <p>
     * Defaults to: null
     */
    public void setHeight(String height) {
        this.height = height;
    }

    /**
     * @see #setIgnoreHiddenSeries(Boolean)
     */
    public Boolean getIgnoreHiddenSeries() {
        return ignoreHiddenSeries;
    }

    /**
     * If true, the axes will scale to the remaining visible series once one
     * series is hidden. If false, hiding and showing a series will not affect
     * the axes or the other series. For stacks, once one series within the
     * stack is hidden, the rest of the stack will close in around it even if
     * the axis is not affected.
     * <p>
     * Defaults to: true
     */
    public void setIgnoreHiddenSeries(Boolean ignoreHiddenSeries) {
        this.ignoreHiddenSeries = ignoreHiddenSeries;
    }

    /**
     * @see #setInverted(Boolean)
     */
    public Boolean getInverted() {
        return inverted;
    }

    /**
     * <p>
     * Whether to invert the axes so that the x axis is vertical and y axis is
     * horizontal. When true, the x axis is
     * <a href="#xAxis.reversed">reversed</a> by default. If a bar series is
     * present in the chart, it will be inverted automatically.
     * </p>
     *
     * <p>
     * Inverting the chart doesn't have an effect if there are no cartesian
     * series in the chart, or if the chart is <a href="#chart.polar">polar</a>.
     * </p>
     * <p>
     * Defaults to: false
     */
    public void setInverted(Boolean inverted) {
        this.inverted = inverted;
    }

    /**
     * @see #setMargin(Number)
     * @see #setMargin(Number, Number)
     * @see #setMargin(Number, Number, Number, Number)
     */
    public Number[] getMargin() {
        return margin;
    }

    /**
     * @see #setMarginBottom(Number)
     */
    public Number getMarginBottom() {
        return marginBottom;
    }

    /**
     * The margin between the bottom outer edge of the chart and the plot area.
     * Use this to set a fixed pixel value for the margin as opposed to the
     * default dynamic margin. See also <code>spacingBottom</code>.
     */
    public void setMarginBottom(Number marginBottom) {
        this.marginBottom = marginBottom;
    }

    /**
     * @see #setMarginLeft(Number)
     */
    public Number getMarginLeft() {
        return marginLeft;
    }

    /**
     * The margin between the left outer edge of the chart and the plot area.
     * Use this to set a fixed pixel value for the margin as opposed to the
     * default dynamic margin. See also <code>spacingLeft</code>.
     */
    public void setMarginLeft(Number marginLeft) {
        this.marginLeft = marginLeft;
    }

    /**
     * @see #setMarginRight(Number)
     */
    public Number getMarginRight() {
        return marginRight;
    }

    /**
     * The margin between the right outer edge of the chart and the plot area.
     * Use this to set a fixed pixel value for the margin as opposed to the
     * default dynamic margin. See also <code>spacingRight</code>.
     */
    public void setMarginRight(Number marginRight) {
        this.marginRight = marginRight;
    }

    /**
     * @see #setMarginTop(Number)
     */
    public Number getMarginTop() {
        return marginTop;
    }

    /**
     * The margin between the top outer edge of the chart and the plot area. Use
     * this to set a fixed pixel value for the margin as opposed to the default
     * dynamic margin. See also <code>spacingTop</code>.
     */
    public void setMarginTop(Number marginTop) {
        this.marginTop = marginTop;
    }

    /**
     * @see #setOptions3d(Options3d)
     */
    public Options3d getOptions3d() {
        if (options3d == null) {
            options3d = new Options3d();
        }
        return options3d;
    }

    /**
     * Options to render charts in 3 dimensions. This feature requires
     * <code>highcharts-3d.js</code>, found in the download package or online at
     * <a href=
     * "http://code.highcharts.com/highcharts-3d.js">code.highcharts.com/
     * highcharts-3d.js</a>.
     */
    public void setOptions3d(Options3d options3d) {
        this.options3d = options3d;
    }

    /**
     * @see #setPanKey(PanKey)
     */
    public PanKey getPanKey() {
        return panKey;
    }

    /**
     * Allows setting a key to switch between zooming and panning. Can be one of
     * <code>alt</code>, <code>ctrl</code>, <code>meta</code> (the command key
     * on Mac and Windows key on Windows) or <code>shift</code>. The keys are
     * mapped directly to the key properties of the click event argument (
     * <code>event.altKey</code>, <code>event.ctrlKey</code>,
     * <code>event.metaKey</code> and <code>event.shiftKey</code>).
     */
    public void setPanKey(PanKey panKey) {
        this.panKey = panKey;
    }

    /**
     * @see #setPanning(Boolean)
     */
    public Boolean getPanning() {
        return panning;
    }

    /**
     * <p>
     * Allow panning in a chart. Best used with
     * <a href="#chart.panKey">panKey</a> to combine zooming and panning.
     * </p>
     *
     * <p>
     * On touch devices, when the
     * <a href="#tooltip.followTouchMove">tooltip.followTouchMove</a> option is
     * <code>true</code> (default), panning requires two fingers. To allow
     * panning with one finger, set <code>followTouchMove</code> to
     * <code>false</code>.
     * </p>
     * <p>
     * Defaults to: false
     */
    public void setPanning(Boolean panning) {
        this.panning = panning;
    }

    /**
     * @see #setPinchType(Dimension)
     */
    public Dimension getPinchType() {
        return pinchType;
    }

    /**
     * Equivalent to <a href="#chart.zoomType">zoomType</a>, but for multitouch
     * gestures only. By default, the <code>pinchType</code> is the same as the
     * <code>zoomType</code> setting. However, pinching can be enabled
     * separately in some cases, for example in stock charts where a mouse drag
     * pans the chart, while pinching is enabled. When
     * <a href="#tooltip.followTouchMove">tooltip.followTouchMove</a> is true,
     * pinchType only applies to two-finger touches.
     * <p>
     * Defaults to: null
     */
    public void setPinchType(Dimension pinchType) {
        this.pinchType = pinchType;
    }

    /**
     * @see #setPlotBackgroundColor(Color)
     */
    public Color getPlotBackgroundColor() {
        return plotBackgroundColor;
    }

    /**
     * The background color or gradient for the plot area.
     */
    public void setPlotBackgroundColor(Color plotBackgroundColor) {
        this.plotBackgroundColor = plotBackgroundColor;
    }

    /**
     * @see #setPlotBackgroundImage(String)
     */
    public String getPlotBackgroundImage() {
        return plotBackgroundImage;
    }

    /**
     * The URL for an image to use as the plot background. To set an image as
     * the background for the entire chart, set a CSS background image to the
     * container element. Note that for the image to be applied to exported
     * charts, its URL needs to be accessible by the export server.
     */
    public void setPlotBackgroundImage(String plotBackgroundImage) {
        this.plotBackgroundImage = plotBackgroundImage;
    }

    /**
     * @see #setPlotBorderColor(Color)
     */
    public Color getPlotBorderColor() {
        return plotBorderColor;
    }

    /**
     * The color of the inner chart or plot area border.
     * <p>
     * Defaults to: #cccccc
     */
    public void setPlotBorderColor(Color plotBorderColor) {
        this.plotBorderColor = plotBorderColor;
    }

    /**
     * @see #setPlotBorderWidth(Number)
     */
    public Number getPlotBorderWidth() {
        return plotBorderWidth;
    }

    /**
     * The pixel width of the plot area border.
     * <p>
     * Defaults to: 0
     */
    public void setPlotBorderWidth(Number plotBorderWidth) {
        this.plotBorderWidth = plotBorderWidth;
    }

    /**
     * @see #setPlotShadow(Boolean)
     */
    public Boolean getPlotShadow() {
        return plotShadow;
    }

    /**
     * Whether to apply a drop shadow to the plot area. Requires that
     * plotBackgroundColor be set. The shadow can be an object configuration
     * containing <code>color</code>, <code>offsetX</code>, <code>offsetY</code>
     * , <code>opacity</code> and <code>width</code>.
     * <p>
     * Defaults to: false
     */
    public void setPlotShadow(Boolean plotShadow) {
        this.plotShadow = plotShadow;
    }

    /**
     * @see #setPolar(Boolean)
     */
    public Boolean getPolar() {
        return polar;
    }

    /**
     * When true, cartesian charts like line, spline, area and column are
     * transformed into the polar coordinate system. Requires
     * <code>highcharts-more.js</code>.
     * <p>
     * Defaults to: false
     */
    public void setPolar(Boolean polar) {
        this.polar = polar;
    }

    /**
     * @see #setReflow(Boolean)
     */
    public Boolean getReflow() {
        return reflow;
    }

    /**
     * Whether to reflow the chart to fit the width of the container div on
     * resizing the window.
     * <p>
     * Defaults to: true
     */
    public void setReflow(Boolean reflow) {
        this.reflow = reflow;
    }

    /**
     * @see #setResetZoomButton(ResetZoomButton)
     */
    public ResetZoomButton getResetZoomButton() {
        if (resetZoomButton == null) {
            resetZoomButton = new ResetZoomButton();
        }
        return resetZoomButton;
    }

    /**
     * The button that appears after a selection zoom, allowing the user to
     * reset zoom.
     */
    public void setResetZoomButton(ResetZoomButton resetZoomButton) {
        this.resetZoomButton = resetZoomButton;
    }

    /**
     * @see #setSelectionMarkerFill(Color)
     */
    public Color getSelectionMarkerFill() {
        return selectionMarkerFill;
    }

    /**
     * The background color of the marker square when selecting (zooming in on)
     * an area of the chart.
     * <p>
     * Defaults to: rgba(51,92,173,0.25)
     */
    public void setSelectionMarkerFill(Color selectionMarkerFill) {
        this.selectionMarkerFill = selectionMarkerFill;
    }

    /**
     * @see #setShadow(Boolean)
     */
    public Boolean getShadow() {
        return shadow;
    }

    /**
     * Whether to apply a drop shadow to the outer chart area. Requires that
     * backgroundColor be set. The shadow can be an object configuration
     * containing <code>color</code>, <code>offsetX</code>, <code>offsetY</code>
     * , <code>opacity</code> and <code>width</code>.
     * <p>
     * Defaults to: false
     */
    public void setShadow(Boolean shadow) {
        this.shadow = shadow;
    }

    /**
     * @see #setShowAxes(Boolean)
     */
    public Boolean getShowAxes() {
        return showAxes;
    }

    /**
     * Whether to show the axes initially. This only applies to empty charts
     * where series are added dynamically, as axes are automatically added to
     * cartesian series.
     * <p>
     * Defaults to: false
     */
    public void setShowAxes(Boolean showAxes) {
        this.showAxes = showAxes;
    }

    /**
     * @see #setSpacing(Number[])
     */
    public Number[] getSpacing() {
        return spacing;
    }

    /**
     * The distance between the outer edge of the chart and the content, like
     * title or legend, or axis title or labels if present. The numbers in the
     * array designate top, right, bottom and left respectively. Use the options
     * spacingTop, spacingRight, spacingBottom and spacingLeft options for
     * shorthand setting of one option.
     * <p>
     * Defaults to: [10, 10, 15, 10]
     */
    public void setSpacing(Number[] spacing) {
        this.spacing = spacing;
    }

    /**
     * @see #setSpacingBottom(Number)
     */
    public Number getSpacingBottom() {
        return spacingBottom;
    }

    /**
     * <p>
     * The space between the bottom edge of the chart and the content (plot
     * area, axis title and labels, title, subtitle or legend in top position).
     * </p>
     * <p>
     * Defaults to: 15
     */
    public void setSpacingBottom(Number spacingBottom) {
        this.spacingBottom = spacingBottom;
    }

    /**
     * @see #setSpacingLeft(Number)
     */
    public Number getSpacingLeft() {
        return spacingLeft;
    }

    /**
     * <p>
     * The space between the left edge of the chart and the content (plot area,
     * axis title and labels, title, subtitle or legend in top position).
     * </p>
     * <p>
     * Defaults to: 10
     */
    public void setSpacingLeft(Number spacingLeft) {
        this.spacingLeft = spacingLeft;
    }

    /**
     * @see #setSpacingRight(Number)
     */
    public Number getSpacingRight() {
        return spacingRight;
    }

    /**
     * <p>
     * The space between the right edge of the chart and the content (plot area,
     * axis title and labels, title, subtitle or legend in top position).
     * </p>
     * <p>
     * Defaults to: 10
     */
    public void setSpacingRight(Number spacingRight) {
        this.spacingRight = spacingRight;
    }

    /**
     * @see #setSpacingTop(Number)
     */
    public Number getSpacingTop() {
        return spacingTop;
    }

    /**
     * <p>
     * The space between the top edge of the chart and the content (plot area,
     * axis title and labels, title, subtitle or legend in top position).
     * </p>
     * <p>
     * Defaults to: 10
     */
    public void setSpacingTop(Number spacingTop) {
        this.spacingTop = spacingTop;
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
     * Additional CSS styles to apply inline to the container <code>div</code>.
     * Note that since the default font styles are applied in the renderer, it
     * is ignorant of the individual chart options and must be set globally.
     * <p>
     * Defaults to: {"fontFamily": "\"Lucida Grande\", \"Lucida Sans Unicode\",
     * Verdana, Arial, Helvetica, sans-serif" ,"fontSize":"12px"}
     */
    public void setStyle(Style style) {
        this.style = style;
    }

    /**
     * @see #setType(ChartType)
     */
    public ChartType getType() {
        return type;
    }

    /**
     * The default series type for the chart. Can be any of the chart types
     * listed under <a href="#plotOptions">plotOptions</a>.
     * <p>
     * Defaults to: line
     */
    public void setType(ChartType type) {
        this.type = type;
    }

    /**
     * @see #setTypeDescription(String)
     */
    public String getTypeDescription() {
        return typeDescription;
    }

    /**
     * <p>
     * A text description of the chart type.
     * </p>
     * <p>
     * If the Accessibility module is loaded, this will be included in the
     * description of the chart in the screen reader information region.
     * </p>
     * <p>
     * Highcharts will by default attempt to guess the chart type, but for more
     * complex charts it is recommended to specify this property for clarity.
     * </p>
     * <p>
     * Defaults to: undefined
     */
    public void setTypeDescription(String typeDescription) {
        this.typeDescription = typeDescription;
    }

    /**
     * @see #setWidth(Number)
     */
    public Number getWidth() {
        return width;
    }

    /**
     * An explicit width for the chart. By default (when <code>null</code>) the
     * width is calculated from the offset width of the containing element.
     * <p>
     * Defaults to: null
     */
    public void setWidth(Number width) {
        this.width = width;
    }

    /**
     * @see #setZoomType(Dimension)
     */
    public Dimension getZoomType() {
        return zoomType;
    }

    /**
     * Decides in what dimensions the user can zoom by dragging the mouse. Can
     * be one of <code>x</code>, <code>y</code> or <code>xy</code>.
     */
    public void setZoomType(Dimension zoomType) {
        this.zoomType = zoomType;
    }

    /**
     * Sets all margins to the same value
     */
    public void setMargin(Number margin) {
        setMargin(margin, margin, margin, margin);
    }

    /**
     * Sets the vertical margin for top and bottom and the horizontal margin for
     * the right and left margin
     */
    public void setMargin(Number vertical, Number horizontal) {
        setMargin(vertical, horizontal, vertical, horizontal);
    }

    /**
     * Set all margins in one call
     */
    public void setMargin(Number top, Number right, Number bottom,
            Number left) {
        margin = new Number[] { top, right, bottom, left };
    }

    public Boolean getStyledMode() {
        return styledMode;
    }

    /**
     * Whether to apply styled mode. When in styled mode, no presentational
     * attributes or CSS are applied to the chart SVG. Instead, CSS rules are
     * required to style the chart. The default style sheet is available from
     * https://code.highcharts.com/css/highcharts.css.
     */
    public void setStyledMode(Boolean styledMode) {
        this.styledMode = styledMode;
    }
}
