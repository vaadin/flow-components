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
import java.util.Date;

import com.vaadin.flow.component.charts.model.style.Color;

public abstract class PointOptions extends AbstractPlotOptions {

    @Override
    public abstract ChartType getChartType();

    /**
     * @see #setAllowPointSelect(Boolean)
     */
    public abstract Boolean getAllowPointSelect();

    /**
     * Allow this series' points to be selected by clicking on the markers, bars
     * or pie slices.
     */
    public abstract void setAllowPointSelect(Boolean allowPointSelect);

    /**
     * @see #setAnimation(Boolean)
     */
    public abstract Boolean getAnimation();

    /**
     * <p>
     * Enable or disable the initial animation when a series is displayed. The
     * animation can also be set as a configuration object. Please note that
     * this option only applies to the initial animation of the series itself.
     * <p>
     * Due to poor performance, animation is disabled in old IE browsers for
     * column charts and polar charts.
     * </p>
     */
    public abstract void setAnimation(Boolean animation);

    /**
     * @see #setColor(Color)
     */
    public abstract Color getColor();

    /**
     * The main color or the series. In line type series it applies to the line
     * and the point markers unless otherwise specified. In bar type series it
     * applies to the bars unless a color is specified per point. The default
     * value is pulled from the <code>options.colors</code> array.
     */
    public abstract void setColor(Color color);

    /**
     * @see #setAnimationLimit(Number)
     */
    public abstract Number getAnimationLimit();

    /**
     * For some series, there is a limit that shuts down initial animation by
     * default when the total number of points in the chart is too high. For
     * example, for a column chart and its derivatives, animation doesn't run if
     * there is more than 250 points totally. To disable this cap, set
     * <code>animationLimit</code> to <code>Infinity</code>.
     */
    public abstract void setAnimationLimit(Number animationLimit);

    /**
     * @see #setClassName(String)
     */
    public abstract String getClassName();

    /**
     * A class name to apply to the series' graphical elements.
     */
    public abstract void setClassName(String className);

    /**
     * @see #setClip(Boolean)
     */
    public abstract Boolean getClip();

    /**
     * Disable this option to allow series rendering in the whole plotting area.
     * Note that clipping should be always enabled when chart.zoomType is set
     */
    public abstract void setClip(Boolean clip);

    /**
     * @see #setColorIndex(Number)
     */
    public abstract Number getColorIndex();

    /**
     * A specific color index to use for the series, so its graphic
     * representations are given the class name
     * <code>highcharts-color-{n}</code>.
     */
    public abstract void setColorIndex(Number colorIndex);

    public abstract String getColorKey();

    /**
     * Determines what data value should be used to calculate point color if
     * <code>colorAxis</code> is used. Requires to set <code>min</code> and
     * <code>max</code> if some custom point property is used or if
     * approximation for data grouping is set to <code>'sum'</code>.
     */
    public abstract void setColorKey(String colorKey);

    /**
     * @see #setCrisp(Boolean)
     */
    public abstract Boolean getCrisp();

    /**
     * When true, each point or column edge is rounded to its nearest pixel in
     * order to render sharp on screen. In some cases, when there are a lot of
     * densely packed columns, this leads to visible difference in column widths
     * or distance between columns. In these cases, setting <code>crisp</code>
     * to <code>false</code> may look better, even though each column is
     * rendered blurry.
     */
    public abstract void setCrisp(Boolean crisp);

    /**
     * @see #setCropThreshold(Number)
     */
    public abstract Number getCropThreshold();

    /**
     * When the series contains less points than the crop threshold, all points
     * are drawn, even if the points fall outside the visible plot area at the
     * current zoom. The advantage of drawing all points (including markers and
     * columns), is that animation is performed on updates. On the other hand,
     * when the series contains more points than the crop threshold, the series
     * data is cropped to only contain points that fall within the plot area.
     * The advantage of cropping away invisible points is to increase
     * performance on large series.
     */
    public abstract void setCropThreshold(Number cropThreshold);

    /**
     * @see #setCursor(Cursor)
     */
    public abstract Cursor getCursor();

    /**
     * You can set the cursor to "pointer" if you have click events attached to
     * the series, to signal to the user that the points and lines can be
     * clicked.
     */
    public abstract void setCursor(Cursor cursor);

    /**
     * @see #setDescription(String)
     */
    public abstract String getDescription();

    /**
     * A description of the series to add to the screen reader information about
     * the series.
     */
    public abstract void setDescription(String description);

    /**
     * @see #setDashStyle(DashStyle)
     */
    public abstract DashStyle getDashStyle();

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
     */
    public abstract void setDashStyle(DashStyle dashStyle);

    /**
     * @see #setDataLabels(DataLabels)
     */
    public abstract DataLabels getDataLabels();

    /**
     * Specific data labels configuration for a series type
     *
     * @param dataLabels
     */
    public abstract void setDataLabels(DataLabels dataLabels);

    /**
     * @see #setEnableMouseTracking(Boolean)
     */
    public abstract Boolean getEnableMouseTracking();

    /**
     * Enable or disable the mouse tracking for a specific series. This includes
     * point tooltips and click events on graphs and points. For large datasets
     * it improves performance.
     */
    public abstract void setEnableMouseTracking(Boolean enableMouseTracking);

    /**
     * @see #setExposeElementToA11y(Boolean)
     */
    public abstract Boolean getExposeElementToA11y();

    /**
     * By default, series are exposed to screen readers as regions. By enabling
     * this option, the series element itself will be exposed in the same way as
     * the data points. This is useful if the series is not used as a grouping
     * entity in the chart, but you still want to attach a description to the
     * series.
     */
    public abstract void setExposeElementToA11y(Boolean exposeElementToA11y);

    /**
     * @see #setFindNearestPointBy(Dimension)
     */
    public abstract Dimension getFindNearestPointBy();

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
    public abstract void setFindNearestPointBy(Dimension findNearestPointBy);

    /**
     * @see #setGetExtremesFromAll(Boolean)
     */
    public abstract Boolean getGetExtremesFromAll();

    /**
     * Whether to use the Y extremes of the total chart width or only the zoomed
     * area when zooming in on parts of the X axis. By default, the Y axis
     * adjusts to the min and max of the visible data. Cartesian series only.
     */
    public abstract void setGetExtremesFromAll(Boolean getExtremesFromAll);

    /**
     * @see #setKeys(String...)
     */
    public abstract String[] getKeys();

    /**
     * An array specifying which option maps to which key in the data point
     * array. This makes it convenient to work with unstructured data arrays
     * from different sources.
     */
    public abstract void setKeys(String... keys);

    /**
     * Adds key to the keys array
     *
     * @param key
     *            to add
     * @see #setKeys(String...)
     */
    public abstract void addKey(String key);

    /**
     * Removes first occurrence of key in keys array
     *
     * @param key
     *            to remove
     * @see #setKeys(String...)
     */
    public abstract void removeKey(String key);

    /**
     * @see #setLineWidth(Number)
     */
    public abstract Number getLineWidth();

    /**
     * The width of the line connecting the data points.
     */
    public abstract void setLineWidth(Number lineWidth);

    /**
     * @see #setLinkedTo(String)
     */
    public abstract String getLinkedTo();

    /**
     * The ID of another series to link to. Additionally, the value can be
     * ":previous" to link to the previous series. When two series are linked,
     * only the first one appears in the legend. Toggling the visibility of this
     * also toggles the linked series.
     */
    public abstract void setLinkedTo(String linkedTo);

    /**
     * @see #setMarker(Marker)
     */
    public abstract Marker getMarker();

    /**
     * Options for the point markers of line-like series.
     */
    public abstract void setMarker(Marker marker);

    /**
     * @see #setNegativeColor(Color)
     */
    public abstract Color getNegativeColor();

    /**
     * The color for the parts of the graph or points that are below the
     * threshold.
     */
    public abstract void setNegativeColor(Color negativeColor);

    /**
     * @see #setOpacity(Number)
     */
    public abstract Number getOpacity();

    /**
     * Opacity of a series parts: line, fill (e.g. area) and dataLabels.
     */
    public abstract void setOpacity(Number opacity);

    public abstract String getPointDescriptionFormatter();

    public abstract void setPointDescriptionFormatter(
            String _fn_pointDescriptionFormatter);

    /**
     * @see #setPointInterval(Number)
     */
    public abstract Number getPointInterval();

    /**
     * <p>
     * If no x values are given for the points in a series, pointInterval
     * defines the interval of the x values. For example, if a series contains
     * one value every decade starting from year 0, set pointInterval to 10.
     * </p>
     * <p>
     * It can be combined with <code>pointIntervalUnit</code> to draw irregular
     * intervals.
     * </p>
     */
    public abstract void setPointInterval(Number pointInterval);

    /**
     * @see #setPointIntervalUnit(IntervalUnit)
     */
    public abstract IntervalUnit getPointIntervalUnit();

    /**
     * On datetime series, this allows for setting the
     * <a href="plotOptions.series.pointInterval">pointInterval</a> to irregular
     * time units, <code>day</code>, <code>month</code> and <code>year</code>. A
     * day is usually the same as 24 hours, but pointIntervalUnit also takes the
     * DST crossover into consideration when dealing with local time. Combine
     * this option with <code>pointInterval</code> to draw weeks, quarters, 6
     * months, 10 years etc.
     */
    public abstract void setPointIntervalUnit(IntervalUnit pointIntervalUnit);

    /**
     * @see #setPointStart(Number)
     */
    public abstract Number getPointStart();

    /**
     * If no x values are given for the points in a series, pointStart defines
     * on what value to start. For example, if a series contains one yearly
     * value starting from 1945, set pointStart to 1945.
     */
    public abstract void setPointStart(Number pointStart);

    /**
     * @see #setSelected(Boolean)
     */
    public abstract Boolean getSelected();

    /**
     * Whether to select the series initially. If <code>showCheckbox</code> is
     * true, the checkbox next to the series name will be checked for a selected
     * series.
     */
    public abstract void setSelected(Boolean selected);

    /**
     * @see #setShadow(Boolean)
     */
    public abstract Boolean getShadow();

    /**
     * Whether to apply a drop shadow to the graph line.
     */
    public abstract void setShadow(Boolean shadow);

    /**
     * @see #setShowCheckbox(Boolean)
     */
    public abstract Boolean getShowCheckbox();

    /**
     * If true, a checkbox is displayed next to the legend item to allow
     * selecting the series. The state of the checkbox is determined by the
     * <code>selected</code> option.
     */
    public abstract void setShowCheckbox(Boolean showCheckbox);

    /**
     * @see #setShowInLegend(Boolean)
     */
    public abstract Boolean getShowInLegend();

    /**
     * Whether to display this particular series or series type in the legend.
     * The default value is <code>true</code> for standalone series,
     * <code>false</code> for linked series.
     */
    public abstract void setShowInLegend(Boolean showInLegend);

    /**
     * @see #setSkipKeyboardNavigation(Boolean)
     */
    public abstract Boolean getSkipKeyboardNavigation();

    /**
     * Whether or not to skip past the points in this series for keyboard
     * navigation.
     */
    public abstract void setSkipKeyboardNavigation(
            Boolean skipKeyboardNavigation);

    /**
     * @see #setSoftThreshold(Boolean)
     */
    public abstract Boolean getSoftThreshold();

    /**
     * <p>
     * When this is true, the series will not cause the Y axis to cross the zero
     * plane unless the data actually crosses the plane.
     * </p>
     *
     * <p>
     * For example, if <code>softThreshold</code> is <code>false</code>, a
     * series of 0, 1, 2, 3 will make the Y axis show negative values according
     * to the <code>minPadding</code> option. If <code>softThreshold</code> is
     * <code>true</code>, the Y axis starts at 0.
     * </p>
     */
    public abstract void setSoftThreshold(Boolean softThreshold);

    /**
     * @see #setStates(States)
     */
    public abstract States getStates();

    /**
     * A wrapper object for all the series options in specific states.
     */
    public abstract void setStates(States states);

    /**
     * @see #setStickyTracking(Boolean)
     */
    public abstract Boolean getStickyTracking();

    /**
     * Sticky tracking of mouse events. When true, the <code>mouseOut</code>
     * event on a series isn't triggered until the mouse moves over another
     * series, or out of the plot area. When false, the <code>mouseOut</code>
     * event on a series is triggered when the mouse leaves the area around the
     * series' graph or markers. This also implies the tooltip. When
     * <code>stickyTracking</code> is false and <code>tooltip.shared</code> is
     * false, the tooltip will be hidden when moving the mouse between series.
     */
    public abstract void setStickyTracking(Boolean stickyTracking);

    /**
     * @see #setThreshold(Number)
     */
    public abstract Number getThreshold();

    /**
     * The threshold, also called zero level or base level. For line type series
     * this is only used in conjunction with negativeColor.
     */
    public abstract void setThreshold(Number threshold);

    /**
     * @see #setTooltip(SeriesTooltip)
     */
    public abstract SeriesTooltip getTooltip();

    /**
     * A configuration object for the tooltip rendering of each single series.
     */
    public abstract void setTooltip(SeriesTooltip tooltip);

    /**
     * @see #setTurboThreshold(Number)
     */
    public abstract Number getTurboThreshold();

    /**
     * When a series contains a data array that is longer than this, only one
     * dimensional arrays of numbers, or two dimensional arrays with x and y
     * values are allowed. Also, only the first point is tested, and the rest
     * are assumed to be the same format. This saves expensive data checking and
     * indexing in long series. Set it to <code>0</code> disable.
     */
    public abstract void setTurboThreshold(Number turboThreshold);

    /**
     * @see #setVisible(Boolean)
     */
    public abstract Boolean getVisible();

    /**
     * Set the initial visibility of the series.
     */
    public abstract void setVisible(Boolean visible);

    /**
     * @see #setZoneAxis(ZoneAxis)
     */
    public abstract ZoneAxis getZoneAxis();

    /**
     * Defines the Axis on which the zones are applied.
     */
    public abstract void setZoneAxis(ZoneAxis zoneAxis);

    /**
     * @see #setZones(Zones...)
     */
    public abstract Zones[] getZones();

    /**
     * An array defining zones within a series. Zones can be applied to the X
     * axis, Y axis or Z axis for bubbles, according to the
     * <code>zoneAxis</code> option.
     */
    public abstract void setZones(Zones... zones);

    /**
     * Adds zone to the zones array
     *
     * @param zone
     *            to add
     * @see #setZones(Zones...)
     */
    public abstract void addZone(Zones zone);

    /**
     * Removes first occurrence of zone in zones array
     *
     * @param zone
     *            to remove
     * @see #setZones(Zones...)
     */
    public abstract void removeZone(Zones zone);

    /**
     * @see #setCompare(Compare)
     */
    public abstract Compare getCompare();

    /**
     * Compare the values of the series against the first non-null, non-zero
     * value in the visible range. The y axis will show percentage or absolute
     * change depending on whether <code>compare</code> is set to
     * <code>"percent"</code> or <code>"value"</code>. When this is applied to
     * multiple series, it allows comparing the development of the series
     * against each other.
     */
    public abstract void setCompare(Compare compare);

    /**
     * @see #setCompareBase(Number)
     */
    public abstract Number getCompareBase();

    /**
     * This option dictates whether to use 0 or 100 as the base of comparison.
     */
    public abstract void setCompareBase(Number compareBase);

    /**
     * @see #setDataGrouping(DataGrouping)
     */
    public abstract DataGrouping getDataGrouping();

    /**
     * Data grouping is the concept of sampling the data values into larger
     * blocks in order to ease readability and increase performance of the
     * charts.
     */
    public abstract void setDataGrouping(DataGrouping dataGrouping);

    /**
     * @see #setGapUnit(String)
     */
    public abstract String getGapUnit();

    /**
     * Together with <code>gapSize</code>, this option defines where to draw
     * gaps in the graph.
     */
    public abstract void setGapUnit(String gapUnit);

    /**
     * @see #setLegendIndex(Number)
     */
    public abstract Number getLegendIndex();

    /**
     * The sequential index of the series within the legend.
     */
    public abstract void setLegendIndex(Number legendIndex);

    /**
     * @see #setNavigatorOptions(PlotOptionsSeries)
     */
    public abstract PlotOptionsSeries getNavigatorOptions();

    /**
     * Options for the corresponding navigator series if
     * <code>showInNavigator</code> is <code>true</code> for this series.
     */
    public abstract void setNavigatorOptions(
            PlotOptionsSeries navigatorOptions);

    /**
     * @see #setPointPlacement(PointPlacement)
     */
    public abstract PointPlacement getPointPlacement();

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
     * Note that pointPlacement needs a pointRange to work. For column series
     * this is computed, but for line-type series it needs to be set.
     * </p>
     */
    public abstract void setPointPlacement(PointPlacement pointPlacement);

    /**
     * @see #setPointRange(Number)
     */
    public abstract Number getPointRange();

    /**
     * The width of each point on the x axis. For example in a column chart with
     * one value each day, the pointRange would be 1 day (= 24 * 3600 * 1000
     * milliseconds). This is normally computed automatically, but this option
     * can be used to override the automatic value.
     */
    public abstract void setPointRange(Number pointRange);

    /**
     * @see #setShowInNavigator(Boolean)
     */
    public abstract Boolean getShowInNavigator();

    /**
     * Whether or not to show the series in the navigator.
     */
    public abstract void setShowInNavigator(Boolean showInNavigator);

    /**
     * @see #setStacking(Stacking)
     */
    public abstract Stacking getStacking();

    /**
     * Whether to stack the values of each series on top of each other. Possible
     * values are null to disable, "normal" to stack by value or "percent".
     */
    public abstract void setStacking(Stacking stacking);

    /**
     * @see #setPointStart(Number)
     */
    @Deprecated
    public abstract void setPointStart(Date date);

    /**
     * @see #setPointStart(Number)
     */
    public abstract void setPointStart(Instant instant);
}
