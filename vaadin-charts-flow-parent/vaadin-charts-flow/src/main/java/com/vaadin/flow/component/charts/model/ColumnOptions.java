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

import java.util.Date;

public abstract class ColumnOptions extends AbstractPlotOptions {

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
     * @see #setBorderColor(Color)
     */
    public abstract Color getBorderColor();

    /**
     * The color of the border of each waterfall column.
     */
    public abstract void setBorderColor(Color borderColor);

    /**
     * @see #setBorderRadius(Number)
     */
    public abstract Number getBorderRadius();

    /**
     * The corner radius of the border surrounding each column or bar.
     */
    public abstract void setBorderRadius(Number borderRadius);

    /**
     * @see #setBorderWidth(Number)
     */
    public abstract Number getBorderWidth();

    /**
     * The width of the border surrounding each column or bar.
     */
    public abstract void setBorderWidth(Number borderWidth);

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
     * When using automatic point colors pulled from the
     * <code>options.colors</code> collection, this option determines whether
     * the chart should receive one color per series or one color per point.
     */
    public abstract void setColorByPoint(Boolean colorByPoint);

    /**
     * @see #setColorKey(String)
     */
    public abstract String getColorKey();

    /**
     * Determines what data value should be used to calculate point color if
     * colorAxis is used. Requires to set <code>min</code> and <code>max</code>
     * if some custom point property is used or if approximation for data
     * grouping is set to <code>'sum'</code>'.
     */
    public abstract void setColorKey(String colorKey);

    /**
     * @see #setColors(Color...)
     */
    public abstract Color[] getColors();

    /**
     * A series specific or series type specific color set to apply instead of
     * the theme colors when {@link #setColorByPoint(Boolean)} is true.
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
     * @see #setCrisp(Boolean)
     */
    public abstract Boolean getCrisp();

    /**
     * When true, each point or column edge is rounded to its nearest pixel in
     * order to render sharp on screen. In some cases, when there are a lot of
     * densely packed columns, this leads to visible difference in column widths
     * or distance between columns. In these cases, setting crisp to false may
     * look better, even though each column is rendered blurry.
     */
    public abstract void setCrisp(Boolean crisp);

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
     * @see #setDepth(Number)
     */
    public abstract Number getDepth();

    /**
     * Depth of the columns in a 3D column chart.
     */
    public abstract void setDepth(Number depth);

    /**
     * @see #setEdgeColor(Color)
     */
    public abstract Color getEdgeColor();

    /**
     * 3D columns only. The color of the edges.
     */
    public abstract void setEdgeColor(Color edgeColor);

    /**
     * @see #setEdgeWidth(Number)
     */
    public abstract Number getEdgeWidth();

    /**
     * 3D columns only. The width of the colored edges.
     */
    public abstract void setEdgeWidth(Number edgeWidth);

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
     * @see #setGroupZPadding(Number)
     */
    public abstract Number getGroupZPadding();

    /**
     * The spacing between columns on the Z Axis in a 3D chart.
     */
    public abstract void setGroupZPadding(Number groupZPadding);

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
     * @see #setOpacity(Number)
     */
    public abstract Number getOpacity();

    /**
     * Opacity of a series parts: line, fill (e.g. area) and dataLabels.
     */
    public abstract void setOpacity(Number opacity);

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
     * The X axis range that each point is valid for. This determines the width
     * of the column. On a categorized axis, the range will be 1 by default (one
     * category unit). On linear and datetime axes, the range will be computed
     * as the distance between the two closest data points.
     */
    public abstract void setPointRange(Number pointRange);

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
     * @see #setPointStart(Number)
     */
    public abstract void setPointStart(Date date);
}
