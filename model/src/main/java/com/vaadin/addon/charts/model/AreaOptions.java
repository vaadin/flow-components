package com.vaadin.addon.charts.model;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2012 - 2016 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.addon.charts.model.DataGrouping;
import com.vaadin.addon.charts.model.SeriesTooltip;
import com.vaadin.addon.charts.model.States;
import com.vaadin.addon.charts.model.Zones;
import com.vaadin.addon.charts.model.style.Color;

import java.util.Date;

public abstract class AreaOptions extends AbstractPlotOptions {

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
     * @see #setConnectNulls(Boolean)
     */
    public abstract Boolean getConnectNulls();

    /**
     * Whether to connect a graph line across null points.
     */
    public abstract void setConnectNulls(Boolean connectNulls);

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
     * @see #setFillColor(Color)
     */
    public abstract Color getFillColor();

    /**
     * Fill color or gradient for the area. When <code>null</code>, the series'
     * <code>color</code> is used with the series' <code>fillOpacity</code>.
     */
    public abstract void setFillColor(Color fillColor);

    /**
     * @see #setFillOpacity(Number)
     */
    public abstract Number getFillOpacity();

    /**
     * Fill opacity for the area. Note that when you set an explicit
     * <code>fillColor</code>, the <code>fillOpacity</code> is not applied.
     * Instead, you should define the opacity in the <code>fillColor</code> with
     * an rgba color definition.
     */
    public abstract void setFillOpacity(Number fillOpacity);

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
     * @see #setLineColor(Color)
     */
    public abstract Color getLineColor();

    /**
     * A separate color for the graph line. By default the line takes the
     * <code>color</code> of the series, but the lineColor setting allows
     * setting a separate color for the line without altering the
     * <code>fillColor</code>.
     */
    public abstract void setLineColor(Color lineColor);

    /**
     * @see #setLineWidth(Number)
     */
    public abstract Number getLineWidth();

    /**
     * Pixel with of the graph line.
     */
    public abstract void setLineWidth(Number lineWidth);

    /**
     * @see #setLinecap(String)
     */
    public abstract String getLinecap();

    /**
     * The line cap used for line ends and line joins on the graph.
     */
    public abstract void setLinecap(String linecap);

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
     * @see #setNegativeColor(Color)
     */
    public abstract Color getNegativeColor();

    /**
     * The color for the parts of the graph or points that are below the
     * threshold.
     */
    public abstract void setNegativeColor(Color negativeColor);

    /**
     * @see #setNegativeFillColor(Color)
     */
    public abstract Color getNegativeFillColor();

    /**
     * A separate color for the negative part of the area.
     */
    public abstract void setNegativeFillColor(Color negativeFillColor);

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
     * On datetime series, this allows for setting the <a
     * href="plotOptions.series.pointInterval">pointInterval</a> to irregular
     * time units, <code>day</code>, <code>month</code> and <code>year</code>. A
     * day is usually the same as 24 hours, but pointIntervalUnit also takes the
     * DST crossover into consideration when dealing with local time. Combine
     * this option with <code>pointInterval</code> to draw weeks, quarters, 6
     * months, 10 years etc.
     */
    public abstract void setPointIntervalUnit(IntervalUnit pointIntervalUnit);

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
     * @see #setStacking(Stacking)
     */
    public abstract Stacking getStacking();

    /**
     * Whether to stack the values of each series on top of each other. Possible
     * values are null to disable, "normal" to stack by value or "percent".
     */
    public abstract void setStacking(Stacking stacking);

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
     * @see #setTooltip(SeriesTooltip)
     */
    public abstract SeriesTooltip getTooltip();

    /**
     * A configuration object for the tooltip rendering of each single series.
     */
    public abstract void setTooltip(SeriesTooltip tooltip);

    /**
     * @see #setTrackByArea(Boolean)
     */
    public abstract Boolean getTrackByArea();

    /**
     * Whether the whole area or just the line should respond to mouseover
     * tooltips and other mouse or touch events.
     */
    public abstract void setTrackByArea(Boolean trackByArea);

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
     * @see #setZoneAxis(String)
     */
    public abstract String getZoneAxis();

    /**
     * Defines the Axis on which the zones are applied.
     */
    public abstract void setZoneAxis(String zoneAxis);

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
     * @see #setDataGrouping(DataGrouping)
     */
    public abstract DataGrouping getDataGrouping();

    public abstract void setDataGrouping(DataGrouping dataGrouping);

    /**
     * @see #setGapSize(Number)
     */
    public abstract Number getGapSize();

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
     */
    public abstract void setGapSize(Number gapSize);

    /**
     * @see #setLegendIndex(Number)
     */
    public abstract Number getLegendIndex();

    /**
     * The sequential index of the series within the legend.
     */
    public abstract void setLegendIndex(Number legendIndex);

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
     * @see #setPointStart(Number)
     */
    public abstract void setPointStart(Date date);
}