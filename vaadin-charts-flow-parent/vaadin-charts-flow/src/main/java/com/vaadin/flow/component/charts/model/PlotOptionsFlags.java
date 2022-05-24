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

import java.util.ArrayList;
import java.util.Arrays;

import com.vaadin.flow.component.charts.model.style.Color;
import com.vaadin.flow.component.charts.model.style.Style;

/**
 *
 */
public class PlotOptionsFlags extends AbstractPlotOptions {

    private Boolean allowPointSelect;
    private Number animationLimit;
    private String className;
    private Boolean clip;
    private Color color;
    private Number colorIndex;
    private String colorKey;
    private ArrayList<Color> colors;
    private Number compareBase;
    private Boolean crisp;
    private Number cropThreshold;
    private Cursor cursor;
    private String description;
    private Boolean enableMouseTracking;
    private Boolean exposeElementToA11y;
    private Dimension findNearestPointBy;
    private String gapUnit;
    private Boolean getExtremesFromAll;
    private ArrayList<String> keys;
    private Number legendIndex;
    private Color lineColor;
    private Number lineWidth;
    private String linkedTo;
    private Number maxPointWidth;
    private PlotOptionsSeries navigatorOptions;
    private Color negativeColor;
    private String onKey;
    private String onSeries;
    private Number opacity;
    private String _fn_pointDescriptionFormatter;
    private IntervalUnit pointIntervalUnit;
    private Boolean selected;
    private Boolean shadow;
    private FlagShape shape;
    private Boolean showCheckbox;
    private Boolean showInLegend;
    private Boolean showInNavigator;
    private Boolean skipKeyboardNavigation;
    private Boolean softThreshold;
    private Number stackDistance;
    private States states;
    private Boolean stickyTracking;
    private Style style;
    private String textAlign;
    private Number threshold;
    private SeriesTooltip tooltip;
    private Boolean useHTML;
    private Boolean visible;
    private Number y;
    private ZoneAxis zoneAxis;
    private ArrayList<Zones> zones;

    public PlotOptionsFlags() {
    }

    @Override
    public ChartType getChartType() {
        return ChartType.FLAGS;
    }

    /**
     * @see #setAllowPointSelect(Boolean)
     */
    public Boolean getAllowPointSelect() {
        return allowPointSelect;
    }

    /**
     * Allow this series' points to be selected by clicking on the markers or
     * bars.
     * <p>
     * Defaults to: false
     */
    public void setAllowPointSelect(Boolean allowPointSelect) {
        this.allowPointSelect = allowPointSelect;
    }

    /**
     * @see #setAnimationLimit(Number)
     */
    public Number getAnimationLimit() {
        return animationLimit;
    }

    /**
     * For some series, there is a limit that shuts down initial animation by
     * default when the total number of points in the chart is too high. For
     * example, for a column chart and its derivatives, animation doesn't run if
     * there is more than 250 points totally. To disable this cap, set
     * <code>animationLimit</code> to <code>Infinity</code>.
     */
    public void setAnimationLimit(Number animationLimit) {
        this.animationLimit = animationLimit;
    }

    /**
     * @see #setClassName(String)
     */
    public String getClassName() {
        return className;
    }

    /**
     * A class name to apply to the series' graphical elements.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @see #setClip(Boolean)
     */
    public Boolean getClip() {
        return clip;
    }

    /**
     * Disable this option to allow series rendering in the whole plotting area.
     * Note that clipping should be always enabled when chart.zoomType is set
     * <p>
     * Defaults to <code>true</code>.
     */
    public void setClip(Boolean clip) {
        this.clip = clip;
    }

    /**
     * @see #setColor(Color)
     */
    public Color getColor() {
        return color;
    }

    /**
     * <p>
     * The main color of the series. In line type series it applies to the line
     * and the point markers unless otherwise specified. In bar type series it
     * applies to the bars unless a color is specified per point. The default
     * value is pulled from the <code>options.colors</code> array.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the color can be defined by the
     * <a href="#plotOptions.series.colorIndex">colorIndex</a> option. Also, the
     * series color can be set with the <code>.highcharts-series</code>,
     * <code>.highcharts-color-{n}</code>,
     * <code>.highcharts-{type}-series</code> or
     * <code>.highcharts-series-{n}</code> class, or individual classes given by
     * the <code>className</code> option.
     * </p>
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * @see #setColorIndex(Number)
     */
    public Number getColorIndex() {
        return colorIndex;
    }

    /**
     * <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >Styled mode</a> only. A specific color index to use for the series, so
     * its graphic representations are given the class name
     * <code>highcharts-color-{n}</code>.
     */
    public void setColorIndex(Number colorIndex) {
        this.colorIndex = colorIndex;
    }

    /**
     * @see #setColorKey(String)
     */
    public String getColorKey() {
        return colorKey;
    }

    /**
     * Determines what data value should be used to calculate point color if
     * <code>colorAxis</code> is used. Requires to set <code>min</code> and
     * <code>max</code> if some custom point property is used or if
     * approximation for data grouping is set to <code>'sum'</code>.
     * <p>
     * Defaults to <code>y</code>.
     */
    public void setColorKey(String colorKey) {
        this.colorKey = colorKey;
    }

    /**
     * @see #setColors(Color...)
     */
    public Color[] getColors() {
        if (colors == null) {
            return new Color[] {};
        }
        Color[] arr = new Color[colors.size()];
        colors.toArray(arr);
        return arr;
    }

    /**
     * A series specific or series type specific color set to apply instead of
     * the global <a href="#colors">colors</a> when
     * <a href="#plotOptions.column.colorByPoint">colorByPoint</a> is true.
     */
    public void setColors(Color... colors) {
        this.colors = new ArrayList<Color>(Arrays.asList(colors));
    }

    /**
     * Adds color to the colors array
     *
     * @param color
     *            to add
     * @see #setColors(Color...)
     */
    public void addColor(Color color) {
        if (this.colors == null) {
            this.colors = new ArrayList<Color>();
        }
        this.colors.add(color);
    }

    /**
     * Removes first occurrence of color in colors array
     *
     * @param color
     *            to remove
     * @see #setColors(Color...)
     */
    public void removeColor(Color color) {
        this.colors.remove(color);
    }

    /**
     * @see #setCompareBase(Number)
     */
    public Number getCompareBase() {
        return compareBase;
    }

    /**
     * When <a href="#plotOptions.series.compare">compare</a> is
     * <code>percent</code>, this option dictates whether to use 0 or 100 as the
     * base of comparison.
     * <p>
     * Defaults to: 0
     */
    public void setCompareBase(Number compareBase) {
        this.compareBase = compareBase;
    }

    /**
     * @see #setCrisp(Boolean)
     */
    public Boolean getCrisp() {
        return crisp;
    }

    /**
     * When true, each point or column edge is rounded to its nearest pixel in
     * order to render sharp on screen. In some cases, when there are a lot of
     * densely packed columns, this leads to visible difference in column widths
     * or distance between columns. In these cases, setting <code>crisp</code>
     * to <code>false</code> may look better, even though each column is
     * rendered blurry.
     * <p>
     * Defaults to <code>true</code>.
     */
    public void setCrisp(Boolean crisp) {
        this.crisp = crisp;
    }

    /**
     * @see #setCropThreshold(Number)
     */
    public Number getCropThreshold() {
        return cropThreshold;
    }

    /**
     * When the series contains less points than the crop threshold, all points
     * are drawn, event if the points fall outside the visible plot area at the
     * current zoom. The advantage of drawing all points (including markers and
     * columns), is that animation is performed on updates. On the other hand,
     * when the series contains more points than the crop threshold, the series
     * data is cropped to only contain points that fall within the plot area.
     * The advantage of cropping away invisible points is to increase
     * performance on large series. .
     * <p>
     * Defaults to: 50
     */
    public void setCropThreshold(Number cropThreshold) {
        this.cropThreshold = cropThreshold;
    }

    /**
     * @see #setCursor(Cursor)
     */
    public Cursor getCursor() {
        return cursor;
    }

    /**
     * You can set the cursor to "pointer" if you have click events attached to
     * the series, to signal to the user that the points and lines can be
     * clicked.
     */
    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    /**
     * @see #setDescription(String)
     */
    public String getDescription() {
        return description;
    }

    /**
     * <p>
     * <i>Requires Accessibility module</i>
     * </p>
     * <p>
     * A description of the series to add to the screen reader information about
     * the series.
     * </p>
     * <p>
     * Defaults to: undefined
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see #setEnableMouseTracking(Boolean)
     */
    public Boolean getEnableMouseTracking() {
        return enableMouseTracking;
    }

    /**
     * Enable or disable the mouse tracking for a specific series. This includes
     * point tooltips and click events on graphs and points. When using shared
     * tooltips (default in stock charts), mouse tracking is not required. For
     * large datasets it improves performance.
     * <p>
     * Defaults to: true
     */
    public void setEnableMouseTracking(Boolean enableMouseTracking) {
        this.enableMouseTracking = enableMouseTracking;
    }

    /**
     * @see #setExposeElementToA11y(Boolean)
     */
    public Boolean getExposeElementToA11y() {
        return exposeElementToA11y;
    }

    /**
     * <p>
     * By default, series are exposed to screen readers as regions. By enabling
     * this option, the series element itself will be exposed in the same way as
     * the data points. This is useful if the series is not used as a grouping
     * entity in the chart, but you still want to attach a description to the
     * series.
     * </p>
     * <p>
     * Requires the Accessibility module.
     * </p>
     * <p>
     * Defaults to: undefined
     */
    public void setExposeElementToA11y(Boolean exposeElementToA11y) {
        this.exposeElementToA11y = exposeElementToA11y;
    }

    /**
     * @see #setFindNearestPointBy(Dimension)
     */
    public Dimension getFindNearestPointBy() {
        return findNearestPointBy;
    }

    /**
     * <p>
     * Determines whether the series should look for the nearest point in both
     * dimensions or just the x-dimension when hovering the series. Defaults to
     * <code>'xy'</code> for scatter series and <code>'x'</code> for most other
     * series. If the data has duplicate x-values, it is recommended to set this
     * to <code>'xy'</code> to allow hovering over all points.
     * </p>
     * <p>
     * Applies only to series types using nearest neighbor search (not direct
     * hover) for tooltip.
     * </p>
     */
    public void setFindNearestPointBy(Dimension findNearestPointBy) {
        this.findNearestPointBy = findNearestPointBy;
    }

    /**
     * @see #setGapUnit(String)
     */
    public String getGapUnit() {
        return gapUnit;
    }

    /**
     * Together with <code>gapSize</code>, this option defines where to draw
     * gaps in the graph.
     * <p>
     * Defaults to: relative
     */
    public void setGapUnit(String gapUnit) {
        this.gapUnit = gapUnit;
    }

    /**
     * @see #setGetExtremesFromAll(Boolean)
     */
    public Boolean getGetExtremesFromAll() {
        return getExtremesFromAll;
    }

    /**
     * Whether to use the Y extremes of the total chart width or only the zoomed
     * area when zooming in on parts of the X axis. By default, the Y axis
     * adjusts to the min and max of the visible data. Cartesian series only.
     * <p>
     * Defaults to: false
     */
    public void setGetExtremesFromAll(Boolean getExtremesFromAll) {
        this.getExtremesFromAll = getExtremesFromAll;
    }

    /**
     * @see #setKeys(String...)
     */
    public String[] getKeys() {
        if (keys == null) {
            return new String[] {};
        }
        String[] arr = new String[keys.size()];
        keys.toArray(arr);
        return arr;
    }

    /**
     * An array specifying which option maps to which key in the data point
     * array. This makes it convenient to work with unstructured data arrays
     * from different sources.
     */
    public void setKeys(String... keys) {
        this.keys = new ArrayList<String>(Arrays.asList(keys));
    }

    /**
     * Adds key to the keys array
     *
     * @param key
     *            to add
     * @see #setKeys(String...)
     */
    public void addKey(String key) {
        if (this.keys == null) {
            this.keys = new ArrayList<String>();
        }
        this.keys.add(key);
    }

    /**
     * Removes first occurrence of key in keys array
     *
     * @param key
     *            to remove
     * @see #setKeys(String...)
     */
    public void removeKey(String key) {
        this.keys.remove(key);
    }

    /**
     * @see #setLegendIndex(Number)
     */
    public Number getLegendIndex() {
        return legendIndex;
    }

    /**
     * The sequential index of the series within the legend.
     * <p>
     * Defaults to: 0
     */
    public void setLegendIndex(Number legendIndex) {
        this.legendIndex = legendIndex;
    }

    /**
     * @see #setLineColor(Color)
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * <p>
     * The color of the line/border of the flag.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the stroke is set in the
     * <code>.highcharts-flag-series .highcharts-point</code> rule.
     * </p>
     * <p>
     * Defaults to: #000000
     */
    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    /**
     * @see #setLineWidth(Number)
     */
    public Number getLineWidth() {
        return lineWidth;
    }

    /**
     * The pixel width of the candlestick line/border. Defaults to
     * <code>1</code>.
     * <p>
     * Defaults to: 1
     */
    public void setLineWidth(Number lineWidth) {
        this.lineWidth = lineWidth;
    }

    /**
     * @see #setLinkedTo(String)
     */
    public String getLinkedTo() {
        return linkedTo;
    }

    /**
     * The <a href="#series.id">id</a> of another series to link to.
     * Additionally, the value can be ":previous" to link to the previous
     * series. When two series are linked, only the first one appears in the
     * legend. Toggling the visibility of this also toggles the linked series.
     */
    public void setLinkedTo(String linkedTo) {
        this.linkedTo = linkedTo;
    }

    /**
     * @see #setMaxPointWidth(Number)
     */
    public Number getMaxPointWidth() {
        return maxPointWidth;
    }

    /**
     * The maximum allowed pixel width for a column, translated to the height of
     * a bar in a bar chart. This prevents the columns from becoming too wide
     * when there is a small number of points in the chart.
     * <p>
     * Defaults to: null
     */
    public void setMaxPointWidth(Number maxPointWidth) {
        this.maxPointWidth = maxPointWidth;
    }

    /**
     * @see #setNavigatorOptions(PlotOptionsSeries)
     */
    public PlotOptionsSeries getNavigatorOptions() {
        return navigatorOptions;
    }

    /**
     * <p>
     * Options for the corresponding navigator series if
     * <code>showInNavigator</code> is <code>true</code> for this series.
     * Available options are the same as any series, documented at
     * <a class="internal" href="#plotOptions.series">plotOptions</a> and
     * <a class="internal" href="#series">series</a>.
     * </p>
     *
     * <p>
     * These options are merged with options in
     * <a href="#navigator.series">navigator.series</a>, and will take
     * precedence if the same option is defined both places.
     * </p>
     * <p>
     * Defaults to: undefined
     */
    public void setNavigatorOptions(PlotOptionsSeries navigatorOptions) {
        this.navigatorOptions = navigatorOptions;
    }

    /**
     * @see #setNegativeColor(Color)
     */
    public Color getNegativeColor() {
        return negativeColor;
    }

    /**
     * The color for the parts of the graph or points that are below the
     * <a href="#plotOptions.series.threshold">threshold</a>.
     * <p>
     * Defaults to: null
     */
    public void setNegativeColor(Color negativeColor) {
        this.negativeColor = negativeColor;
    }

    /**
     * @see #setOnKey(String)
     */
    public String getOnKey() {
        return onKey;
    }

    /**
     * In case the flag is placed on a series, on what point key to place it.
     * Line and columns have one key, <code>y</code>. In range or OHLC-type
     * series, however, the flag can optionally be placed on the
     * <code>open</code>, <code>high</code>, <code>low</code> or
     * <code>close</code> key.
     * <p>
     * Defaults to: y
     */
    public void setOnKey(String onKey) {
        this.onKey = onKey;
    }

    /**
     * @see #setOnSeries(String)
     */
    public String getOnSeries() {
        return onSeries;
    }

    /**
     * The id of the series that the flags should be drawn on. If no id is
     * given, the flags are drawn on the x axis.
     * <p>
     * Defaults to: undefined
     */
    public void setOnSeries(String onSeries) {
        this.onSeries = onSeries;
    }

    /**
     * @see #setOpacity(Number)
     */
    public Number getOpacity() {
        return opacity;
    }

    /**
     * Opacity of a series parts: line, fill (e.g. area) and dataLabels.
     * <p>
     * Defaults to <code>1</code>.
     */
    public void setOpacity(Number opacity) {
        this.opacity = opacity;
    }

    public String getPointDescriptionFormatter() {
        return _fn_pointDescriptionFormatter;
    }

    public void setPointDescriptionFormatter(
            String _fn_pointDescriptionFormatter) {
        this._fn_pointDescriptionFormatter = _fn_pointDescriptionFormatter;
    }

    /**
     * @see #setPointIntervalUnit(IntervalUnit)
     */
    public IntervalUnit getPointIntervalUnit() {
        return pointIntervalUnit;
    }

    /**
     * On datetime series, this allows for setting the
     * <a href="#plotOptions.series.pointInterval">pointInterval</a> to
     * irregular time units, <code>day</code>, <code>month</code> and
     * <code>year</code>. A day is usually the same as 24 hours, but
     * pointIntervalUnit also takes the DST crossover into consideration when
     * dealing with local time. Combine this option with
     * <code>pointInterval</code> to draw weeks, quarters, 6 months, 10 years
     * etc.
     */
    public void setPointIntervalUnit(IntervalUnit pointIntervalUnit) {
        this.pointIntervalUnit = pointIntervalUnit;
    }

    /**
     * @see #setSelected(Boolean)
     */
    public Boolean getSelected() {
        return selected;
    }

    /**
     * Whether to select the series initially. If <code>showCheckbox</code> is
     * true, the checkbox next to the series name in the legend will be checked
     * for a selected series.
     * <p>
     * Defaults to: false
     */
    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    /**
     * @see #setShadow(Boolean)
     */
    public Boolean getShadow() {
        return shadow;
    }

    /**
     * Whether to apply a drop shadow to the graph line. Since 1.1.7 the shadow
     * can be an object configuration containing <code>color</code>,
     * <code>offsetX</code>, <code>offsetY</code>, <code>opacity</code> and
     * <code>width</code>.
     * <p>
     * Defaults to: false
     */
    public void setShadow(Boolean shadow) {
        this.shadow = shadow;
    }

    /**
     * @see #setShape(FlagShape)
     */
    public FlagShape getShape() {
        return shape;
    }

    /**
     * The shape of the marker. Can be one of "flag", "circlepin", "squarepin",
     * or an image on the format <code>url(/path-to-image.jpg)</code>.
     * Individual shapes can also be set for each point.
     * <p>
     * Defaults to: flag
     */
    public void setShape(FlagShape shape) {
        this.shape = shape;
    }

    /**
     * @see #setShowCheckbox(Boolean)
     */
    public Boolean getShowCheckbox() {
        return showCheckbox;
    }

    /**
     * If true, a checkbox is displayed next to the legend item to allow
     * selecting the series. The state of the checkbox is determined by the
     * <code>selected</code> option.
     * <p>
     * Defaults to: false
     */
    public void setShowCheckbox(Boolean showCheckbox) {
        this.showCheckbox = showCheckbox;
    }

    /**
     * @see #setShowInLegend(Boolean)
     */
    public Boolean getShowInLegend() {
        return showInLegend;
    }

    /**
     * Whether to display this particular series or series type in the legend.
     * The default value is <code>true</code> for standalone series,
     * <code>false</code> for linked series.
     * <p>
     * Defaults to: true
     */
    public void setShowInLegend(Boolean showInLegend) {
        this.showInLegend = showInLegend;
    }

    /**
     * @see #setShowInNavigator(Boolean)
     */
    public Boolean getShowInNavigator() {
        return showInNavigator;
    }

    /**
     * Whether or not to show the series in the navigator. Takes precedence over
     * <a href="#navigator.baseSeries">navigator.baseSeries</a> if defined.
     * <p>
     * Defaults to: undefined
     */
    public void setShowInNavigator(Boolean showInNavigator) {
        this.showInNavigator = showInNavigator;
    }

    /**
     * @see #setSkipKeyboardNavigation(Boolean)
     */
    public Boolean getSkipKeyboardNavigation() {
        return skipKeyboardNavigation;
    }

    /**
     * If set to <code>True</code>, the accessibility module will skip past the
     * points in this series for keyboard navigation.
     */
    public void setSkipKeyboardNavigation(Boolean skipKeyboardNavigation) {
        this.skipKeyboardNavigation = skipKeyboardNavigation;
    }

    /**
     * @see #setSoftThreshold(Boolean)
     */
    public Boolean getSoftThreshold() {
        return softThreshold;
    }

    /**
     * <p>
     * When this is true, the series will not cause the Y axis to cross the zero
     * plane (or <a href="#plotOptions.series.threshold">threshold</a> option)
     * unless the data actually crosses the plane.
     * </p>
     *
     * <p>
     * For example, if <code>softThreshold</code> is <code>false</code>, a
     * series of 0, 1, 2, 3 will make the Y axis show negative values according
     * to the <code>minPadding</code> option. If <code>softThreshold</code> is
     * <code>true</code>, the Y axis starts at 0.
     * </p>
     * <p>
     * Defaults to: false
     */
    public void setSoftThreshold(Boolean softThreshold) {
        this.softThreshold = softThreshold;
    }

    /**
     * @see #setStackDistance(Number)
     */
    public Number getStackDistance() {
        return stackDistance;
    }

    /**
     * When multiple flags in the same series fall on the same value, this
     * number determines the vertical offset between them.
     * <p>
     * Defaults to: 12
     */
    public void setStackDistance(Number stackDistance) {
        this.stackDistance = stackDistance;
    }

    /**
     * @see #setStates(States)
     */
    public States getStates() {
        if (states == null) {
            states = new States();
        }
        return states;
    }

    /**
     * A wrapper object for all the series options in specific states.
     */
    public void setStates(States states) {
        this.states = states;
    }

    /**
     * @see #setStickyTracking(Boolean)
     */
    public Boolean getStickyTracking() {
        return stickyTracking;
    }

    /**
     * Sticky tracking of mouse events. When true, the <code>mouseOut</code>
     * event on a series isn't triggered until the mouse moves over another
     * series, or out of the plot area. When false, the <code>mouseOut</code>
     * event on a series is triggered when the mouse leaves the area around the
     * series' graph or markers. This also implies the tooltip when not shared.
     * When <code>stickyTracking</code> is false, the tooltip will be hidden
     * when moving the mouse between series. Defaults to true for line and area
     * type series, but to false for columns, candlesticks etc.
     * <p>
     * Defaults to: true
     */
    public void setStickyTracking(Boolean stickyTracking) {
        this.stickyTracking = stickyTracking;
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
     * The text styles of the flag.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the styles are set in the
     * <code>.highcharts-flag-series .highcharts-point</code> rule.
     * </p>
     * <p>
     * Defaults to: { "fontSize": "11px", "fontWeight": "bold" }
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
     * Text alignment for the text inside the flag.
     * <p>
     * Defaults to: center
     */
    public void setTextAlign(String textAlign) {
        this.textAlign = textAlign;
    }

    /**
     * @see #setThreshold(Number)
     */
    public Number getThreshold() {
        return threshold;
    }

    /**
     * The threshold, also called zero level or base level. For line type series
     * this is only used in conjunction with
     * <a href="#plotOptions.series.negativeColor">negativeColor</a>.
     * <p>
     * Defaults to: 0
     */
    public void setThreshold(Number threshold) {
        this.threshold = threshold;
    }

    /**
     * @see #setTooltip(SeriesTooltip)
     */
    public SeriesTooltip getTooltip() {
        if (tooltip == null) {
            tooltip = new SeriesTooltip();
        }
        return tooltip;
    }

    /**
     * Specific tooltip options for flag series. Flag series tooltips are
     * different from most other types in that a flag doesn't have a data value,
     * so the tooltip rather displays the <code>text</code> option for each
     * point.
     */
    public void setTooltip(SeriesTooltip tooltip) {
        this.tooltip = tooltip;
    }

    /**
     * @see #setUseHTML(Boolean)
     */
    public Boolean getUseHTML() {
        return useHTML;
    }

    /**
     * Whether to use HTML to render the flag texts. Using HTML allows for
     * advanced formatting, images and reliable bi-directional text rendering.
     * Note that exported images won't respect the HTML, and that HTML won't
     * respect Z-index settings.
     * <p>
     * Defaults to: false
     */
    public void setUseHTML(Boolean useHTML) {
        this.useHTML = useHTML;
    }

    /**
     * @see #setVisible(Boolean)
     */
    public Boolean getVisible() {
        return visible;
    }

    /**
     * Set the initial visibility of the series.
     * <p>
     * Defaults to: true
     */
    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    /**
     * @see #setY(Number)
     */
    public Number getY() {
        return y;
    }

    /**
     * The y position of the top left corner of the flag relative to either the
     * series (if onSeries is defined), or the x axis. Defaults to
     * <code>-30</code>.
     * <p>
     * Defaults to: -30
     */
    public void setY(Number y) {
        this.y = y;
    }

    /**
     * @see #setZoneAxis(ZoneAxis)
     */
    public ZoneAxis getZoneAxis() {
        return zoneAxis;
    }

    /**
     * Defines the Axis on which the zones are applied.
     * <p>
     * Defaults to: y
     */
    public void setZoneAxis(ZoneAxis zoneAxis) {
        this.zoneAxis = zoneAxis;
    }

    /**
     * @see #setZones(Zones...)
     */
    public Zones[] getZones() {
        if (zones == null) {
            return new Zones[] {};
        }
        Zones[] arr = new Zones[zones.size()];
        zones.toArray(arr);
        return arr;
    }

    /**
     * <p>
     * An array defining zones within a series. Zones can be applied to the X
     * axis, Y axis or Z axis for bubbles, according to the
     * <code>zoneAxis</code> option.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the color zones are styled with the
     * <code>.highcharts-zone-{n}</code> class, or custom classed from the
     * <code>className</code> option (<a href=
     * "http://jsfiddle.net/gh/get/library/pure/highcharts/highcharts/tree/master/samples/highcharts/css/color-zones/"
     * >view live demo</a>).
     * </p>
     */
    public void setZones(Zones... zones) {
        this.zones = new ArrayList<Zones>(Arrays.asList(zones));
    }

    /**
     * Adds zone to the zones array
     *
     * @param zone
     *            to add
     * @see #setZones(Zones...)
     */
    public void addZone(Zones zone) {
        if (this.zones == null) {
            this.zones = new ArrayList<Zones>();
        }
        this.zones.add(zone);
    }

    /**
     * Removes first occurrence of zone in zones array
     *
     * @param zone
     *            to remove
     * @see #setZones(Zones...)
     */
    public void removeZone(Zones zone) {
        this.zones.remove(zone);
    }
}
