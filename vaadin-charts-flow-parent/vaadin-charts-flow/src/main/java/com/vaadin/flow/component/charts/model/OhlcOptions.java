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

public abstract class OhlcOptions extends AbstractPlotOptions {

    @Override
    public abstract ChartType getChartType();

    /**
     * @see #setAllowPointSelect(Boolean)
     */
    public abstract Boolean getAllowPointSelect();

    /**
     * Allow this series' points to be selected by clicking on the markers or
     * bars.
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
     */
    public abstract void setClip(Boolean clip);

    /**
     * @see #setColorByPoint(Boolean)
     */
    public abstract Boolean getColorByPoint();

    /**
     * When using automatic point colors pulled from the global colors or
     * series-specific plotOptions.column.colors collections, this option
     * determines whether the chart should receive one color per series or one
     * color per point.
     * <p>
     * In styled mode, the <code>colors</code> or <code>series.colors</code>
     * arrays are not supported, and instead this option gives the points
     * individual color class names on the form
     * <code>highcharts-color-{n}</code>.
     */
    public abstract void setColorByPoint(Boolean colorByPoint);

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

    /**
     * @see #setColorKey(String)
     */
    public abstract String getColorKey();

    /**
     * Determines what data value should be used to calculate point color if
     * <code>colorAxis</code> is used. Requires to set <code>min</code> and
     * <code>max</code> if some custom point property is used or if
     * approximation for data grouping is set to <code>'sum'</code>.
     */
    public abstract void setColorKey(String colorKey);

    /**
     * @see #setColors(Color...)
     */
    public abstract Color[] getColors();

    /**
     * A series specific or series type specific color set to apply instead of
     * the theme colors.
     */
    public abstract void setColors(Color... colors);

    /**
     * Adds color to the colors array
     *
     * @param color
     *            to add
     * @see #setColors(Color...)
     */
    public abstract void addColor(Color color);

    /**
     * Removes first occurrence of color in colors array
     *
     * @param color
     *            to remove
     * @see #setColors(Color...)
     */
    public abstract void removeColor(Color color);

    /**
     * @see #setCompareBase(Number)
     */
    public abstract Number getCompareBase();

    /**
     * This option dictates whether to use 0 or 100 as the base of comparison.
     */
    public abstract void setCompareBase(Number compareBase);

    /**
     * @see #setCropThreshold(Number)
     */
    public abstract Number getCropThreshold();

    /**
     * When the series contains less points than the crop threshold, all points
     * are drawn, event if the points fall outside the visible plot area at the
     * current zoom. The advantage of drawing all points (including markers and
     * columns), is that animation is performed on updates. On the other hand,
     * when the series contains more points than the crop threshold, the series
     * data is cropped to only contain points that fall within the plot area.
     * The advantage of cropping away invisible points is to increase
     * performance on large series. .
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
     * @see #setDataGrouping(DataGrouping)
     */
    public abstract DataGrouping getDataGrouping();

    /**
     * Data grouping is the concept of sampling the data values into larger
     * blocks in order to ease readability and increase performance of the
     * JavaScript charts. By default data grouping is applied when the points
     * become closer than a certain pixel value, determined by the
     * groupPixelWidth option.
     *
     * @param dataGrouping
     */
    public abstract void setDataGrouping(DataGrouping dataGrouping);

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
     * @see #setEnableMouseTracking(Boolean)
     */
    public abstract Boolean getEnableMouseTracking();

    /**
     * Enable or disable the mouse tracking for a specific series. This includes
     * point tooltips and click events on graphs and points. When using shared
     * tooltips (default in stock charts), mouse tracking is not required. For
     * large datasets it improves performance.
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
     * @see #setGapUnit(String)
     */
    public abstract String getGapUnit();

    /**
     * Together with <code>gapSize</code>, this option defines where to draw
     * gaps in the graph.
     */
    public abstract void setGapUnit(String gapUnit);

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
     * @see #setGroupPadding(Number)
     */
    public abstract Number getGroupPadding();

    /**
     * Padding between each value groups, in x axis units.
     */
    public abstract void setGroupPadding(Number groupPadding);

    /**
     * @see #setGrouping(Boolean)
     */
    public abstract Boolean getGrouping();

    /**
     * Whether to group non-stacked columns or to let them render independent of
     * each other. Non-grouped columns will be laid out individually and overlap
     * each other.
     */
    public abstract void setGrouping(Boolean grouping);

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
     * @see #setLegendIndex(Number)
     */
    public abstract Number getLegendIndex();

    /**
     * The sequential index of the series within the legend.
     */
    public abstract void setLegendIndex(Number legendIndex);

    /**
     * @see #setLineWidth(Number)
     */
    public abstract Number getLineWidth();

    /**
     * The pixel width of the candlestick line/border.
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
     * @see #setMaxPointWidth(Number)
     */
    public abstract Number getMaxPointWidth();

    /**
     * The maximum allowed pixel width for a column, translated to the height of
     * a bar in a bar chart. This prevents the columns from becoming too wide
     * when there is a small number of points in the chart.
     */
    public abstract void setMaxPointWidth(Number maxPointWidth);

    /**
     * @see #setMinPointLength(Number)
     */
    public abstract Number getMinPointLength();

    /**
     * The minimal height for a column or width for a bar. By default, 0 values
     * are not shown. To visualize a 0 (or close to zero) point, set the minimal
     * point length to a pixel value like 3. In stacked column charts,
     * minPointLength might not be respected for tightly packed values.
     */
    public abstract void setMinPointLength(Number minPointLength);

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
     * defines the interval of the x values in milliseconds. For example, if a
     * series contains one value each day, set pointInterval to
     * <code>24 * 3600 * 1000</code>.
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
     * @see #setPointPadding(Number)
     */
    public abstract Number getPointPadding();

    /**
     * Padding between each column or bar, in x axis units.
     */
    public abstract void setPointPadding(Number pointPadding);

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
     * can be used to override the automatic value. In a series on a categorized
     * axis the pointRange is 1 by default.
     */
    public abstract void setPointRange(Number pointRange);

    /**
     * @see #setPointStart(Number)
     */
    public abstract Number getPointStart();

    /**
     * If no x values are given for the points in a series, pointStart defines
     * on what value to start. On a datetime X axis, the number will be given as
     * milliseconds since 1970-01-01, for example
     * <code>Date.UTC(2011, 0, 1)</code>.
     */
    public abstract void setPointStart(Number pointStart);

    /**
     * @see #setPointWidth(Number)
     */
    public abstract Number getPointWidth();

    /**
     * A pixel value specifying a fixed width for each column or bar. When
     * <code>null</code>, the width is calculated from the
     * <code>pointPadding</code> and <code>groupPadding</code>.
     */
    public abstract void setPointWidth(Number pointWidth);

    /**
     * @see #setSelected(Boolean)
     */
    public abstract Boolean getSelected();

    /**
     * Whether to select the series initially. If <code>showCheckbox</code> is
     * true, the checkbox next to the series name in the legend will be checked
     * for a selected series.
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
     * @see #setShowInNavigator(Boolean)
     */
    public abstract Boolean getShowInNavigator();

    /**
     * Whether or not to show the series in the navigator.
     */
    public abstract void setShowInNavigator(Boolean showInNavigator);

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
     * series' graph or markers. This also implies the tooltip when not shared.
     * When <code>stickyTracking</code> is false, the tooltip will be hidden
     * when moving the mouse between series.
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
     * @see #setUpColor(Color)
     */
    public abstract Color getUpColor();

    /**
     * Line color for up points.
     */
    public abstract void setUpColor(Color upColor);

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
     * @deprecated as of 4.0. Use {@link #setPointStart(Instant)}
     */
    @Deprecated
    public abstract void setPointStart(Date date);

    /**
     * @see #setPointStart(Number)
     */
    public abstract void setPointStart(Instant instant);
}
