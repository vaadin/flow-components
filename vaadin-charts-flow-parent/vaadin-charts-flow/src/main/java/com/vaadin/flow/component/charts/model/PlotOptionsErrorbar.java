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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.time.Instant;
import com.vaadin.flow.component.charts.util.Util;

/**
 * Error bars are a graphical representation of the variability of data and are
 * used on graphs to indicate the error, or uncertainty in a reported
 * measurement.
 */
public class PlotOptionsErrorbar extends AbstractPlotOptions {

    private Boolean allowPointSelect;
    private Number animationLimit;
    private String className;
    private Boolean clip;
    private Color color;
    private Boolean colorByPoint;
    private Number colorIndex;
    private String colorKey;
    private ArrayList<Color> colors;
    private Boolean crisp;
    private Cursor cursor;
    private Number depth;
    private String description;
    private Color edgeColor;
    private Number edgeWidth;
    private Boolean enableMouseTracking;
    private Boolean exposeElementToA11y;
    private Dimension findNearestPointBy;
    private Boolean getExtremesFromAll;
    private Number groupZPadding;
    private ArrayList<String> keys;
    private Number lineWidth;
    private String linkedTo;
    private Number maxPointWidth;
    private Color negativeColor;
    private Number opacity;
    private String _fn_pointDescriptionFormatter;
    private Number pointInterval;
    private IntervalUnit pointIntervalUnit;
    private Number pointPadding;
    private PointPlacement pointPlacement;
    private Number pointRange;
    private Number pointStart;
    private Number pointWidth;
    private Boolean selected;
    private Boolean skipKeyboardNavigation;
    private States states;
    private Color stemColor;
    private DashStyle stemDashStyle;
    private Number stemWidth;
    private Boolean stickyTracking;
    private SeriesTooltip tooltip;
    private Number turboThreshold;
    private Boolean visible;
    private Color whiskerColor;
    private String whiskerLength;
    private Number whiskerWidth;
    private ZoneAxis zoneAxis;
    private ArrayList<Zones> zones;

    public PlotOptionsErrorbar() {
    }

    @Override
    public ChartType getChartType() {
        return ChartType.ERRORBAR;
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
     * @return
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
     * The main color of the bars. This can be overridden by
     * <a href="#plotOptions.errorbar.stemColor">stemColor</a> and
     * <a href="#plotOptions.errorbar.whiskerColor">whiskerColor</a>
     * individually.
     * <p>
     * Defaults to: #000000
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * @see #setColorByPoint(Boolean)
     */
    public Boolean getColorByPoint() {
        return colorByPoint;
    }

    /**
     * When using automatic point colors pulled from the
     * <code>options.colors</code> collection, this option determines whether
     * the chart should receive one color per series or one color per point.
     * <p>
     * Defaults to: false
     */
    public void setColorByPoint(Boolean colorByPoint) {
        this.colorByPoint = colorByPoint;
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
     * approximation for data grouping is set to <code>`sum'</code>.
     * <p>
     * Defaults to <code>high</code>.
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
     * @see #setCrisp(Boolean)
     */
    public Boolean getCrisp() {
        return crisp;
    }

    /**
     * When true, each column edge is rounded to its nearest pixel in order to
     * render sharp on screen. In some cases, when there are a lot of densely
     * packed columns, this leads to visible difference in column widths or
     * distance between columns. In these cases, setting <code>crisp</code> to
     * <code>false</code> may look better, even though each column is rendered
     * blurry.
     * <p>
     * Defaults to: true
     */
    public void setCrisp(Boolean crisp) {
        this.crisp = crisp;
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
     * @see #setDepth(Number)
     */
    public Number getDepth() {
        return depth;
    }

    /**
     * Depth of the columns in a 3D column chart. Requires
     * <code>highcharts-3d.js</code>.
     * <p>
     * Defaults to: 25
     */
    public void setDepth(Number depth) {
        this.depth = depth;
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
     * @see #setEdgeColor(Color)
     */
    public Color getEdgeColor() {
        return edgeColor;
    }

    /**
     * 3D columns only. The color of the edges. Similar to
     * <code>borderColor</code>, except it defaults to the same color as the
     * column.
     */
    public void setEdgeColor(Color edgeColor) {
        this.edgeColor = edgeColor;
    }

    /**
     * @see #setEdgeWidth(Number)
     */
    public Number getEdgeWidth() {
        return edgeWidth;
    }

    /**
     * 3D columns only. The width of the colored edges.
     * <p>
     * Defaults to: 1
     */
    public void setEdgeWidth(Number edgeWidth) {
        this.edgeWidth = edgeWidth;
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
     * @see #setGroupZPadding(Number)
     */
    public Number getGroupZPadding() {
        return groupZPadding;
    }

    /**
     * The spacing between columns on the Z Axis in a 3D chart. Requires
     * <code>highcharts-3d.js</code>.
     * <p>
     * Defaults to: 1
     */
    public void setGroupZPadding(Number groupZPadding) {
        this.groupZPadding = groupZPadding;
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
     * The width of the line surrounding the box. If any of
     * <a href="#plotOptions.boxplot.stemWidth">stemWidth</a>,
     * <a href="#plotOptions.boxplot.medianWidth">medianWidth</a> or
     * <a href="#plotOptions.boxplot.whiskerWidth">whiskerWidth</a> are
     * <code>null</code>, the lineWidth also applies to these lines.
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
     * The parent series of the error bar. The default value links it to the
     * previous series. Otherwise, use the id of the parent series.
     * <p>
     * Defaults to: :previous
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
     * @see #setOpacity(Number)
     */
    public Number getOpacity() {
        return opacity;
    }

    /**
     * Opacity of a series parts: line, fill (e.g. area) and dataLabels.
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
     * @see #setPointPadding(Number)
     */
    public Number getPointPadding() {
        return pointPadding;
    }

    /**
     * Padding between each column or bar, in x axis units.
     * <p>
     * Defaults to: 0.1
     */
    public void setPointPadding(Number pointPadding) {
        this.pointPadding = pointPadding;
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
     * @see #setPointRange(Number)
     */
    public Number getPointRange() {
        return pointRange;
    }

    /**
     * The X axis range that each point is valid for. This determines the width
     * of the column. On a categorized axis, the range will be 1 by default (one
     * category unit). On linear and datetime axes, the range will be computed
     * as the distance between the two closest data points.
     */
    public void setPointRange(Number pointRange) {
        this.pointRange = pointRange;
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
     * @see #setPointWidth(Number)
     */
    public Number getPointWidth() {
        return pointWidth;
    }

    /**
     * A pixel value specifying a fixed width for each column or bar. When
     * <code>null</code>, the width is calculated from the
     * <code>pointPadding</code> and <code>groupPadding</code>.
     */
    public void setPointWidth(Number pointWidth) {
        this.pointWidth = pointWidth;
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
     * @see #setStemColor(Color)
     */
    public Color getStemColor() {
        return stemColor;
    }

    /**
     * The color of the stem, the vertical line extending from the box to the
     * whiskers. If <code>null</code>, the series color is used.
     * <p>
     * Defaults to: null
     */
    public void setStemColor(Color stemColor) {
        this.stemColor = stemColor;
    }

    /**
     * @see #setStemDashStyle(DashStyle)
     */
    public DashStyle getStemDashStyle() {
        return stemDashStyle;
    }

    /**
     * The dash style of the stem, the vertical line extending from the box to
     * the whiskers.
     * <p>
     * Defaults to: Solid
     */
    public void setStemDashStyle(DashStyle stemDashStyle) {
        this.stemDashStyle = stemDashStyle;
    }

    /**
     * @see #setStemWidth(Number)
     */
    public Number getStemWidth() {
        return stemWidth;
    }

    /**
     * The width of the stem, the vertical line extending from the box to the
     * whiskers. If <code>null</code>, the width is inherited from the
     * <a href="#plotOptions.boxplot.lineWidth">lineWidth</a> option.
     * <p>
     * Defaults to: null
     */
    public void setStemWidth(Number stemWidth) {
        this.stemWidth = stemWidth;
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
     * @see #setWhiskerColor(Color)
     */
    public Color getWhiskerColor() {
        return whiskerColor;
    }

    /**
     * The color of the whiskers, the horizontal lines marking low and high
     * values. When <code>null</code>, the general series color is used.
     * <p>
     * Defaults to: null
     */
    public void setWhiskerColor(Color whiskerColor) {
        this.whiskerColor = whiskerColor;
    }

    /**
     * @see #setWhiskerLength(String)
     */
    public String getWhiskerLength() {
        return whiskerLength;
    }

    /**
     * The length of the whiskers, the horizontal lines marking low and high
     * values. It can be a numerical pixel value, or a percentage value of the
     * box width. Set <code>0</code> to disable whiskers.
     * <p>
     * Defaults to: 50%
     */
    public void setWhiskerLength(String whiskerLength) {
        this.whiskerLength = whiskerLength;
    }

    /**
     * @see #setWhiskerWidth(Number)
     */
    public Number getWhiskerWidth() {
        return whiskerWidth;
    }

    /**
     * The line width of the whiskers, the horizontal lines marking low and high
     * values. When <code>null</code>, the general
     * <a href="#plotOptions.errorbar.lineWidth">lineWidth</a> applies.
     * <p>
     * Defaults to: null
     */
    public void setWhiskerWidth(Number whiskerWidth) {
        this.whiskerWidth = whiskerWidth;
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
