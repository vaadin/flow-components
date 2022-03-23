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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.vaadin.flow.component.charts.model.style.Color;
import com.vaadin.flow.component.charts.util.Util;

/**
 * A bubble series is a three dimensional series type where each point renders
 * an X, Y and Z value. Each points is drawn as a bubble where the position
 * along the X and Y axes mark the X and Y values, and the size of the bubble
 * relates to the Z value. Requires <code>highcharts-more.js</code>.
 */
public class PlotOptionsBubble extends AbstractPlotOptions {

    private Boolean allowPointSelect;
    private Boolean animation;
    private Number animationLimit;
    private String className;
    private Boolean clip;
    private Color color;
    private Number colorIndex;
    private String colorKey;
    private Boolean crisp;
    private Number cropThreshold;
    private Cursor cursor;
    private DashStyle dashStyle;
    private DataLabels dataLabels;
    private String description;
    private Boolean displayNegative;
    private Boolean enableMouseTracking;
    private Boolean exposeElementToA11y;
    private Dimension findNearestPointBy;
    private Boolean getExtremesFromAll;
    private ArrayList<String> keys;
    private Number lineWidth;
    private String linkedTo;
    private Marker marker;
    private String maxSize;
    private String minSize;
    private Color negativeColor;
    private Number opacity;
    private String _fn_pointDescriptionFormatter;
    private Number pointInterval;
    private IntervalUnit pointIntervalUnit;
    private Number pointStart;
    private Boolean selected;
    private Boolean shadow;
    private Boolean showCheckbox;
    private Boolean showInLegend;
    private String sizeBy;
    private Boolean sizeByAbsoluteValue;
    private Boolean skipKeyboardNavigation;
    private Boolean softThreshold;
    private States states;
    private Boolean stickyTracking;
    private Number threshold;
    private SeriesTooltip tooltip;
    private Boolean visible;
    private Number zMax;
    private Number zMin;
    private Number zThreshold;
    private ZoneAxis zoneAxis;
    private ArrayList<Zones> zones;

    public PlotOptionsBubble() {
    }

    @Override
    public ChartType getChartType() {
        return ChartType.BUBBLE;
    }

    /**
     * @see #setAllowPointSelect(Boolean)
     */
    public Boolean getAllowPointSelect() {
        return allowPointSelect;
    }

    /**
     * Allow this series' points to be selected by clicking on the markers, bars
     * or pie slices.
     * <p>
     * Defaults to: false
     */
    public void setAllowPointSelect(Boolean allowPointSelect) {
        this.allowPointSelect = allowPointSelect;
    }

    /**
     * @see #setAnimation(Boolean)
     */
    public Boolean getAnimation() {
        return animation;
    }

    /**
     * Enable or disable the initial animation when a series is displayed.
     * Please note that this option only applies to the initial animation of the
     * series itself. For other animations, see
     * {@link ChartModel#setAnimation(Boolean)}
     */
    public void setAnimation(Boolean animation) {
        this.animation = animation;
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
     * Note that clipping should be always enabled when chart.zoomType is set.
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
     * The main color or the series. In line type series it applies to the line
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
     * Defaults to <code>z</code>.
     */
    public void setColorKey(String colorKey) {
        this.colorKey = colorKey;
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
     * are drawn, even if the points fall outside the visible plot area at the
     * current zoom. The advantage of drawing all points (including markers and
     * columns), is that animation is performed on updates. On the other hand,
     * when the series contains more points than the crop threshold, the series
     * data is cropped to only contain points that fall within the plot area.
     * The advantage of cropping away invisible points is to increase
     * performance on large series.
     * <p>
     * Defaults to: 300
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
     * @see #setDashStyle(DashStyle)
     */
    public DashStyle getDashStyle() {
        return dashStyle;
    }

    /**
     * A name for the dash style to use for the graph. Applies only to series
     * type having a graph, like <code>line</code>, <code>spline</code>,
     * <code>area</code> and <code>scatter</code> in case it has a
     * <code>lineWidth</code>. The value for the <code>dashStyle</code> include:
     * <ul>
     * <li>Solid</li>
     * <li>ShortDash</li>
     * <li>ShortDot</li>
     * <li>ShortDashDot</li>
     * <li>ShortDashDotDot</li>
     * <li>Dot</li>
     * <li>Dash</li>
     * <li>LongDash</li>
     * <li>DashDot</li>
     * <li>LongDashDot</li>
     * <li>LongDashDotDot</li>
     * </ul>
     * <p>
     * Defaults to: Solid
     */
    public void setDashStyle(DashStyle dashStyle) {
        this.dashStyle = dashStyle;
    }

    /**
     * @see #setDataLabels(DataLabels)
     */
    public DataLabels getDataLabels() {
        if (dataLabels == null) {
            dataLabels = new DataLabels();
        }
        return dataLabels;
    }

    /**
     * <p>
     * Options for the series data labels, appearing next to each data point.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the data labels can be styled wtih the
     * <code>.highcharts-data-label-box</code> and
     * <code>.highcharts-data-label</code> class names (<a href=
     * "http://jsfiddle.net/gh/get/library/pure/highcharts/highcharts/tree/master/samples/highcharts/css/series-datalabels"
     * >see example</a>).
     * </p>
     */
    public void setDataLabels(DataLabels dataLabels) {
        this.dataLabels = dataLabels;
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
     * @see #setDisplayNegative(Boolean)
     */
    public Boolean getDisplayNegative() {
        return displayNegative;
    }

    /**
     * Whether to display negative sized bubbles. The threshold is given by the
     * <a href="#plotOptions.bubble.zThreshold">zThreshold</a> option, and
     * negative bubbles can be visualized by setting
     * <a href="#plotOptions.bubble.negativeColor">negativeColor</a>.
     * <p>
     * Defaults to: true
     */
    public void setDisplayNegative(Boolean displayNegative) {
        this.displayNegative = displayNegative;
    }

    /**
     * @see #setEnableMouseTracking(Boolean)
     */
    public Boolean getEnableMouseTracking() {
        return enableMouseTracking;
    }

    /**
     * Enable or disable the mouse tracking for a specific series. This includes
     * point tooltips and click events on graphs and points. For large datasets
     * it improves performance.
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
     * @see #setLineWidth(Number)
     */
    public Number getLineWidth() {
        return lineWidth;
    }

    /**
     * The width of the line connecting the data points.
     * <p>
     * Defaults to: 0
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
     * @see #setMarker(Marker)
     */
    public Marker getMarker() {
        if (marker == null) {
            marker = new Marker();
        }
        return marker;
    }

    /**
     * <p>
     * Options for the point markers of line-like series. Properties like
     * <code>fillColor</code>, <code>lineColor</code> and <code>lineWidth</code>
     * define the visual appearance of the markers. Other series types, like
     * column series, don't have markers, but have visual options on the series
     * level instead.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the markers can be styled with the
     * <code>.highcharts-point</code>, <code>.highcharts-point-hover</code> and
     * <code>.highcharts-point-select</code> class names.
     * </p>
     */
    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    /**
     * @see #setMaxSize(String)
     */
    public String getMaxSize() {
        return maxSize;
    }

    /**
     * Maximum bubble size. Bubbles will automatically size between the
     * <code>minSize</code> and <code>maxSize</code> to reflect the
     * <code>z</code> value of each bubble. Can be either pixels (when no unit
     * is given), or a percentage of the smallest one of the plot width and
     * height.
     * <p>
     * Defaults to: 20%
     */
    public void setMaxSize(String maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * @see #setMinSize(String)
     */
    public String getMinSize() {
        return minSize;
    }

    /**
     * Minimum bubble size. Bubbles will automatically size between the
     * <code>minSize</code> and <code>maxSize</code> to reflect the
     * <code>z</code> value of each bubble. Can be either pixels (when no unit
     * is given), or a percentage of the smallest one of the plot width and
     * height.
     * <p>
     * Defaults to: 8
     */
    public void setMinSize(String minSize) {
        this.minSize = minSize;
    }

    /**
     * @see #setNegativeColor(Color)
     */
    public Color getNegativeColor() {
        return negativeColor;
    }

    /**
     * When a point's Z value is below the
     * <a href="#plotOptions.bubble.zThreshold">zThreshold</a> setting, this
     * color is used.
     * <p>
     * Defaults to: null
     */
    public void setNegativeColor(Color negativeColor) {
        this.negativeColor = negativeColor;
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
     * @see #setPointInterval(Number)
     */
    public Number getPointInterval() {
        return pointInterval;
    }

    /**
     * <p>
     * If no x values are given for the points in a series, pointInterval
     * defines the interval of the x values. For example, if a series contains
     * one value every decade starting from year 0, set pointInterval to 10.
     * </p>
     * <p>
     * Since Highcharts 4.1, it can be combined with
     * <code>pointIntervalUnit</code> to draw irregular intervals.
     * </p>
     * <p>
     * Defaults to: 1
     */
    public void setPointInterval(Number pointInterval) {
        this.pointInterval = pointInterval;
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
     * @see #setPointStart(Number)
     */
    public Number getPointStart() {
        return pointStart;
    }

    /**
     * If no x values are given for the points in a series, pointStart defines
     * on what value to start. For example, if a series contains one yearly
     * value starting from 1945, set pointStart to 1945.
     * <p>
     * Defaults to: 0
     */
    public void setPointStart(Number pointStart) {
        this.pointStart = pointStart;
    }

    /**
     * @see #setSelected(Boolean)
     */
    public Boolean getSelected() {
        return selected;
    }

    /**
     * Whether to select the series initially. If <code>showCheckbox</code> is
     * true, the checkbox next to the series name will be checked for a selected
     * series.
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
     * Whether to apply a drop shadow to the graph line. Since 2.3 the shadow
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
     * @see #setSizeBy(String)
     */
    public String getSizeBy() {
        return sizeBy;
    }

    /**
     * Whether the bubble's value should be represented by the area or the width
     * of the bubble. The default, <code>area</code>, corresponds best to the
     * human perception of the size of each bubble.
     * <p>
     * Defaults to: area
     */
    public void setSizeBy(String sizeBy) {
        this.sizeBy = sizeBy;
    }

    /**
     * @see #setSizeByAbsoluteValue(Boolean)
     */
    public Boolean getSizeByAbsoluteValue() {
        return sizeByAbsoluteValue;
    }

    /**
     * When this is true, the absolute value of z determines the size of the
     * bubble. This means that with the default <code>zThreshold</code> of 0, a
     * bubble of value -1 will have the same size as a bubble of value 1, while
     * a bubble of value 0 will have a smaller size according to
     * <code>minSize</code>.
     * <p>
     * Defaults to: false
     */
    public void setSizeByAbsoluteValue(Boolean sizeByAbsoluteValue) {
        this.sizeByAbsoluteValue = sizeByAbsoluteValue;
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
     * series' graph or markers. This also implies the tooltip. When
     * <code>stickyTracking</code> is false and <code>tooltip.shared</code> is
     * false, the tooltip will be hidden when moving the mouse between series.
     * <p>
     * Defaults to: false
     */
    public void setStickyTracking(Boolean stickyTracking) {
        this.stickyTracking = stickyTracking;
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
     * A configuration object for the tooltip rendering of each single series.
     * Properties are inherited from <a href="#tooltip">tooltip</a>, but only
     * the following properties can be defined on a series level.
     */
    public void setTooltip(SeriesTooltip tooltip) {
        this.tooltip = tooltip;
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
     * @see #setZMax(Number)
     */
    public Number getZMax() {
        return zMax;
    }

    /**
     * The minimum for the Z value range. Defaults to the highest Z value in the
     * data.
     * <p>
     * Defaults to: null
     */
    public void setZMax(Number zMax) {
        this.zMax = zMax;
    }

    /**
     * @see #setZMin(Number)
     */
    public Number getZMin() {
        return zMin;
    }

    /**
     * The minimum for the Z value range. Defaults to the lowest Z value in the
     * data.
     * <p>
     * Defaults to: null
     */
    public void setZMin(Number zMin) {
        this.zMin = zMin;
    }

    /**
     * @see #setZThreshold(Number)
     */
    public Number getZThreshold() {
        return zThreshold;
    }

    /**
     * When <a href="#plotOptions.bubble.displayNegative">displayNegative</a> is
     * <code>false</code>, bubbles with lower Z values are skipped. When
     * <code>displayNegative</code> is <code>true</code> and a
     * <a href="#plotOptions.bubble.negativeColor">negativeColor</a> is given,
     * points with lower Z is colored.
     * <p>
     * Defaults to: 0
     */
    public void setZThreshold(Number zThreshold) {
        this.zThreshold = zThreshold;
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

    /**
     * @deprecated as of 4.0. Use {@link #setPointStart(Instant)}
     */
    @Deprecated
    public void setPointStart(Date date) {
        this.pointStart = Util.toHighchartsTS(date);
    }

    /**
     * @see #setPointStart(Number)
     */
    public void setPointStart(Instant instant) {
        this.pointStart = Util.toHighchartsTS(instant);
    }
}
