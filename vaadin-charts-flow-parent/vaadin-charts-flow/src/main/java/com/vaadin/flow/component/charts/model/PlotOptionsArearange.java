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
 * The area range is a cartesian series type with higher and lower Y values
 * along an X axis. Requires <code>highcharts-more.js</code>.
 */
public class PlotOptionsArearange extends AreaOptions {

    private Boolean allowPointSelect;
    private Boolean animation;
    private Number animationLimit;
    private String className;
    private Boolean clip;
    private Color color;
    private Number colorIndex;
    private String colorKey;
    private Boolean connectNulls;
    private Boolean crisp;
    private Number cropThreshold;
    private Cursor cursor;
    private DashStyle dashStyle;
    private DataLabelsRange dataLabels;
    private String description;
    private Boolean enableMouseTracking;
    private Boolean exposeElementToA11y;
    private Color fillColor;
    private Number fillOpacity;
    private Dimension findNearestPointBy;
    private Boolean getExtremesFromAll;
    private ArrayList<String> keys;
    private Color lineColor;
    private Number lineWidth;
    private String linecap;
    private String linkedTo;
    private Color negativeColor;
    private Color negativeFillColor;
    private Number opacity;
    private String _fn_pointDescriptionFormatter;
    private Number pointInterval;
    private IntervalUnit pointIntervalUnit;
    private PointPlacement pointPlacement;
    private Number pointStart;
    private Boolean selected;
    private Boolean shadow;
    private Boolean showCheckbox;
    private Boolean showInLegend;
    private Boolean skipKeyboardNavigation;
    private States states;
    private StepType step;
    private Boolean stickyTracking;
    private SeriesTooltip tooltip;
    private Boolean trackByArea;
    private Number turboThreshold;
    private Boolean visible;
    private ZoneAxis zoneAxis;
    private ArrayList<Zones> zones;
    private Compare compare;
    private Number compareBase;
    private DataGrouping dataGrouping;
    private Number gapSize;
    private String gapUnit;
    private Number legendIndex;
    private PlotOptionsSeries navigatorOptions;
    private Number pointRange;
    private Boolean showInNavigator;
    private Stacking stacking;
    private Number threshold;

    public PlotOptionsArearange() {
    }

    @Override
    public ChartType getChartType() {
        return ChartType.AREARANGE;
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
     * Defaults to <code>low</code>.
     */
    public void setColorKey(String colorKey) {
        this.colorKey = colorKey;
    }

    /**
     * @see #setConnectNulls(Boolean)
     */
    public Boolean getConnectNulls() {
        return connectNulls;
    }

    /**
     * Whether to connect a graph line across null points.
     * <p>
     * Defaults to: false
     */
    public void setConnectNulls(Boolean connectNulls) {
        this.connectNulls = connectNulls;
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
     * @see #setDataLabels(DataLabelsRange)
     */
    public DataLabelsRange getDataLabels() {
        if (dataLabels == null) {
            dataLabels = new DataLabelsRange();
        }
        return dataLabels;
    }

    /**
     * Extended data labels for range series types. Range series data labels
     * have no <code>x</code> and <code>y</code> options. Instead, they have
     * <code>xLow</code>, <code>xHigh</code>, <code>yLow</code> and
     * <code>yHigh</code> options to allow the higher and lower data label sets
     * individually.
     */
    public void setDataLabels(DataLabelsRange dataLabels) {
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
     * @see #setFillColor(Color)
     */
    public Color getFillColor() {
        return fillColor;
    }

    /**
     * Fill color or gradient for the area. When <code>null</code>, the series'
     * <code>color</code> is used with the series' <code>fillOpacity</code>.
     */
    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    /**
     * @see #setFillOpacity(Number)
     */
    public Number getFillOpacity() {
        return fillOpacity;
    }

    /**
     * Fill opacity for the area. When you set an explicit
     * <code>fillColor</code>, the <code>fillOpacity</code> is not applied.
     * Instead, you should define the opacity in the <code>fillColor</code> with
     * an rgba color definition. The <code>fillOpacity</code> setting, also the
     * default setting, overrides the alpha component of the <code>color</code>
     * setting.
     * <p>
     * Defaults to: 0.75
     */
    public void setFillOpacity(Number fillOpacity) {
        this.fillOpacity = fillOpacity;
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
     * @see #setLineColor(Color)
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * A separate color for the graph line. By default the line takes the
     * <code>color</code> of the series, but the lineColor setting allows
     * setting a separate color for the line without altering the
     * <code>fillColor</code>.
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
     * Pixel width of the arearange graph line.
     * <p>
     * Defaults to: 1
     */
    public void setLineWidth(Number lineWidth) {
        this.lineWidth = lineWidth;
    }

    /**
     * @see #setLinecap(String)
     */
    public String getLinecap() {
        return linecap;
    }

    /**
     * The line cap used for line ends and line joins on the graph.
     * <p>
     * Defaults to: round
     */
    public void setLinecap(String linecap) {
        this.linecap = linecap;
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
     * @see #setNegativeFillColor(Color)
     */
    public Color getNegativeFillColor() {
        return negativeFillColor;
    }

    /**
     * A separate color for the negative part of the area.
     */
    public void setNegativeFillColor(Color negativeFillColor) {
        this.negativeFillColor = negativeFillColor;
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
     * @see #setPointPlacement(PointPlacement)
     */
    public PointPlacement getPointPlacement() {
        return pointPlacement;
    }

    /**
     * <p>
     * Possible values: <code>null</code>, <code>"on"</code>,
     * <code>"between"</code>.
     * </p>
     * <p>
     * In a column chart, when pointPlacement is <code>"on"</code>, the point
     * will not create any padding of the X axis. In a polar column chart this
     * means that the first column points directly north. If the pointPlacement
     * is <code>"between"</code>, the columns will be laid out between ticks.
     * This is useful for example for visualising an amount between two points
     * in time or in a certain sector of a polar chart.
     * </p>
     * <p>
     * Since Highcharts 3.0.2, the point placement can also be numeric, where 0
     * is on the axis value, -0.5 is between this value and the previous, and
     * 0.5 is between this value and the next. Unlike the textual options,
     * numeric point placement options won't affect axis padding.
     * </p>
     * <p>
     * Note that pointPlacement needs a
     * <a href="#plotOptions.series.pointRange">pointRange</a> to work. For
     * column series this is computed, but for line-type series it needs to be
     * set.
     * </p>
     * <p>
     * Defaults to <code>null</code> in cartesian charts, <code>"between"</code>
     * in polar charts.
     */
    public void setPointPlacement(PointPlacement pointPlacement) {
        this.pointPlacement = pointPlacement;
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
     * @see #setStep(StepType)
     */
    public StepType getStep() {
        return step;
    }

    /**
     * Whether to apply steps to the line. Possible values are <code>left</code>
     * , <code>center</code> and <code>right</code>. Prior to 2.3.5, only
     * <code>left</code> was supported.
     * <p>
     * Defaults to: false
     */
    public void setStep(StepType step) {
        this.step = step;
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
     * Defaults to true for line and area type series, but to false for columns,
     * pies etc.
     * <p>
     * Defaults to: true
     */
    public void setStickyTracking(Boolean stickyTracking) {
        this.stickyTracking = stickyTracking;
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
     * @see #setTrackByArea(Boolean)
     */
    public Boolean getTrackByArea() {
        return trackByArea;
    }

    /**
     * Whether the whole area or just the line should respond to mouseover
     * tooltips and other mouse or touch events.
     * <p>
     * Defaults to: true
     */
    public void setTrackByArea(Boolean trackByArea) {
        this.trackByArea = trackByArea;
    }

    /**
     * @see #setTurboThreshold(Number)
     */
    public Number getTurboThreshold() {
        return turboThreshold;
    }

    /**
     * When a series contains a data array that is longer than this, only one
     * dimensional arrays of numbers, or two dimensional arrays with x and y
     * values are allowed. Also, only the first point is tested, and the rest
     * are assumed to be the same format. This saves expensive data checking and
     * indexing in long series. Set it to <code>0</code> disable.
     * <p>
     * Defaults to: 1000
     */
    public void setTurboThreshold(Number turboThreshold) {
        this.turboThreshold = turboThreshold;
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
     * @see #setCompare(Compare)
     */
    public Compare getCompare() {
        return compare;
    }

    /**
     * Compare the values of the series against the first non-null, non-zero
     * value in the visible range. The y axis will show percentage or absolute
     * change depending on whether <code>compare</code> is set to
     * <code>"percent"</code> or <code>"value"</code>. When this is applied to
     * multiple series, it allows comparing the development of the series
     * against each other.
     * <p>
     * Defaults to: undefined
     */
    public void setCompare(Compare compare) {
        this.compare = compare;
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
     * @see #setDataGrouping(DataGrouping)
     */
    public DataGrouping getDataGrouping() {
        if (dataGrouping == null) {
            dataGrouping = new DataGrouping();
        }
        return dataGrouping;
    }

    /**
     * <p>
     * Data grouping is the concept of sampling the data values into larger
     * blocks in order to ease readability and increase performance of the
     * JavaScript charts. Highstock by default applies data grouping when the
     * points become closer than a certain pixel value, determined by the
     * <code>groupPixelWidth</code> option.
     * </p>
     *
     * <p>
     * If data grouping is applied, the grouping information of grouped points
     * can be read from the <a href="#Point.dataGroup">Point.dataGroup</a>.
     * </p>
     */
    public void setDataGrouping(DataGrouping dataGrouping) {
        this.dataGrouping = dataGrouping;
    }

    /**
     * @see #setGapSize(Number)
     */
    public Number getGapSize() {
        return gapSize;
    }

    /**
     * <p>
     * Defines when to display a gap in the graph. A gap size of 5 means that if
     * the distance between two points is greater than five times that of the
     * two closest points, the graph will be broken.
     * </p>
     *
     * <p>
     * In practice, this option is most often used to visualize gaps in time
     * series. In a stock chart, intraday data is available for daytime hours,
     * while gaps will appear in nights and weekends.
     * </p>
     * <p>
     * Defaults to: 0
     */
    public void setGapSize(Number gapSize) {
        this.gapSize = gapSize;
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
     * @see #setPointRange(Number)
     */
    public Number getPointRange() {
        return pointRange;
    }

    /**
     * The width of each point on the x axis. For example in a column chart with
     * one value each day, the pointRange would be 1 day (= 24 * 3600 * 1000
     * milliseconds). This is normally computed automatically, but this option
     * can be used to override the automatic value.
     * <p>
     * Defaults to: 0
     */
    public void setPointRange(Number pointRange) {
        this.pointRange = pointRange;
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
     * @see #setStacking(Stacking)
     */
    public Stacking getStacking() {
        return stacking;
    }

    /**
     * Whether to stack the values of each series on top of each other. Possible
     * values are null to disable, "normal" to stack by value or "percent". When
     * stacking is enabled, data must be sorted in ascending X order.
     * <p>
     * Defaults to: null
     */
    public void setStacking(Stacking stacking) {
        this.stacking = stacking;
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

    /**
     * @see #setThreshold(Number)
     */
    public Number getThreshold() {
        return threshold;
    }

    public void setThreshold(Number threshold) {
        this.threshold = threshold;
    }
}
