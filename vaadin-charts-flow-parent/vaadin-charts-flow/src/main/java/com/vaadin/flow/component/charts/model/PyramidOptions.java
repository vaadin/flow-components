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

public abstract class PyramidOptions extends AbstractPlotOptions {

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
     * The center of the series. By default, it is centered in the middle of the
     * plot area, so it fills the plot area height.
     */
    public abstract void setCenter(String[] center);

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
     * A series specific or series type specific color set to use instead of the
     * theme colors.
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
     * or distance between columns. In these cases, setting crisp to
     * <code>falase</code> may look better, even though each column is rendered
     * blurry.
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
     * @see #setDataLabels(DataLabelsFunnel)
     */
    public abstract DataLabelsFunnel getDataLabels();

    /**
     * Specific data labels configuration for a series type
     *
     * @param dataLabels
     */
    public abstract void setDataLabels(DataLabelsFunnel dataLabels);

    /**
     * @see #setDepth(Number)
     */
    public abstract Number getDepth();

    /**
     * The thickness of a 3D pie.
     */
    public abstract void setDepth(Number depth);

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
     * @see #setHeight(String)
     */
    public abstract String getHeight();

    /**
     * Sets the height using String presentation. String presentation is similar
     * to what is used in Cascading Style Sheets. Size can be pixels or
     * percentage, otherwise IllegalArgumentException is thrown. The empty
     * string ("") or null will unset the height and set the units to pixels.
     *
     * @param height
     *            CSS style string representation
     */
    public abstract void setHeight(String height);

    /**
     * @see #setIgnoreHiddenPoint(Boolean)
     */
    public abstract Boolean getIgnoreHiddenPoint();

    /**
     * This option tells whether the series shall be redrawn as if the hidden
     * point were null.
     */
    public abstract void setIgnoreHiddenPoint(Boolean ignoreHiddenPoint);

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
     * @see #setMinSize(Number)
     */
    public abstract Number getMinSize();

    /**
     * The minimum size for a pie in response to auto margins. The pie will try
     * to shrink to make room for data labels in side the plot area, but only to
     * this size.
     */
    public abstract void setMinSize(Number minSize);

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
     * @see #setReversed(Boolean)
     */
    public abstract Boolean getReversed();

    /**
     * The pyramid is reversed by default, as opposed to the funnel, which
     * shares the layout engine, and is not reversed.
     */
    public abstract void setReversed(Boolean reversed);

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
     * @see #setShowInLegend(Boolean)
     */
    public abstract Boolean getShowInLegend();

    /**
     * Whether to display this particular series or series type in the legend.
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
     * @see #setSlicedOffset(Number)
     */
    public abstract Number getSlicedOffset();

    /**
     * If a point is sliced, moved out from the center, how many pixels should
     * it be moved?.
     */
    public abstract void setSlicedOffset(Number slicedOffset);

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
     * @see #setWidth(String)
     */
    public abstract String getWidth();

    /**
     * Sets the width using String presentation. String presentation is similar
     * to what is used in Cascading Style Sheets. Size can be pixels or
     * percentage, otherwise IllegalArgumentException is thrown. The empty
     * string ("") or null will unset the height and set the units to pixels.
     *
     * @param width
     *            CSS style string representation
     */
    public abstract void setWidth(String width);

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

    public abstract void setCenter(String x, String y);

    public abstract String[] getCenter();
}
