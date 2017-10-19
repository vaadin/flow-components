package com.vaadin.addon.charts.model;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2012 - 2015 Vaadin Ltd
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vaadin.addon.charts.model.AxisTitle;
import com.vaadin.addon.charts.model.Labels;
import com.vaadin.addon.charts.model.PlotBand;
import com.vaadin.addon.charts.model.PlotLine;
import com.vaadin.addon.charts.model.style.Color;
import com.vaadin.addon.charts.util.Util;

import java.util.Date;

public abstract class Axis extends AbstractConfigurationObject {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected Number min;
    protected Number max;

    private Integer axisIndex;

    @JsonIgnore
    private ChartConfiguration configuration;

    public void setAxisIndex(int i) {
        axisIndex = i;
    }

    protected Integer getAxisIndex() {
        return axisIndex;
    }

    /**
     * @see #setMin(Number)
     * @return the minimum value of the axis or null
     */
    public Number getMin() {
        return min;
    }

    /**
     * The minimum value of the axis. If null the min value is automatically
     * calculated. If the startOnTick option is true, the min value might be
     * rounded down.
     *
     * @param min
     */
    public void setMin(Number min) {
        this.min = min;
    }

    /**
     * The minimum value of the axis as Date.
     *
     * @param min
     * @see #setMin(Number)
     */
    public void setMin(Date min) {
        this.min = Util.toHighchartsTS(min);
    }

    /**
     * @see #setMax(Number)
     * @return Maximum value of axis or null
     */
    public Number getMax() {
        return max;
    }

    /**
     * The maximum value of the axis. If null, the max value is automatically
     * calculated. If the endOnTick option is true, the max value might be
     * rounded up. The actual maximum value is also influenced by
     * chart.alignTicks.
     *
     * @param max
     */
    public void setMax(Number max) {
        this.max = max;
    }

    /**
     * The maximum value of the axis as Date.
     *
     * @param max
     * @see #setMax(Number)
     */
    public void setMax(Date max) {
        this.max = Util.toHighchartsTS(max);
    }


    /**
     * Sets the minimum and maximum of the axes after rendering has finished. If
     * the startOnTick and endOnTick options are true, the minimum and maximum
     * values are rounded off to the nearest tick. To prevent this, these
     * options can be set to false before calling setExtremes.
     * 
     * @param min
     *            The new minimum value
     * @param max
     *            The new maximum value
     */
    public void setExtremes(Number min, Number max) {
        this.setExtremes(min, max, true, true);
    }

    /**
     * The minimun and maximum value of the axis as Date.
     *
     * @see #setExtremes(Number, Number)
     */
    public void setExtremes(Date min, Date max) {
        this.setExtremes(min, max, true, true);
    }

    /**
     * Sets the extremes at runtime.
     * 
     * @param min
     *            Minimum.
     * @param max
     *            Maximum.
     * @param redraw
     *            Whether or not to redraw the chart.
     */
    public void setExtremes(Number min, Number max, boolean redraw) {
        this.setExtremes(min, max, redraw, true);
    }

    /**
     * The minimun and maximum value of the axis as Date.
     *
     * @see #setExtremes(Number, Number, boolean)
     */
    public void setExtremes(Date min, Date max, boolean redraw) {
        this.setExtremes(min, max, redraw, true);
    }

    /**
     * Run-time modification of the axis extremes.
     * 
     * @param minimum
     *            New minimum value.
     * @param maximum
     *            New maximum value.
     * @param redraw
     *            Whether or not to redraw the chart.
     * @param animate
     *            Whether or not to animate the rescaling.
     */
    public void setExtremes(Number minimum, Number maximum, boolean redraw,
            boolean animate) {
        min = minimum;
        max = maximum;
        if (configuration != null) {
            configuration.fireAxesRescaled(this, minimum, maximum, redraw,
                    animate);
        }
    }

    /**
     * The minimun and maximum value of the axis as Date.
     *
     * @see #setExtremes(Number, Number, boolean, boolean)
     */
    public void setExtremes(Date minimum, Date maximum, boolean redraw,
        boolean animate) {
        setMin(minimum);
        setMax(maximum);
        if (configuration != null) {
            configuration.fireAxesRescaled(this, min, max, redraw,
                animate);
        }
    }

    /**
     * Returns the configuration this axis is bound to.
     * 
     * @return The configuration.
     */
    public ChartConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Sets the configuration this axis is bound to. This method is
     * automatically called by configuration, when the axis is added to it.
     * 
     * @param configuration
     *            Configuration this object is linked to.
     */
    public void setConfiguration(ChartConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * @see #setAllowDecimals(Boolean)
     */
    abstract public Boolean getAllowDecimals();

    /**
     * Whether to allow decimals in this axis' ticks. When counting integers,
     * like persons or hits on a web page, decimals must be avoided in the axis
     * tick labels.
     */
    abstract public void setAllowDecimals(Boolean allowDecimals);

    /**
     * @see #setAlternateGridColor(Color)
     */
    abstract public Color getAlternateGridColor();

    /**
     * When using an alternate grid color, a band is painted across the plot
     * area between every other grid line.
     */
    abstract public void setAlternateGridColor(Color alternateGridColor);

    /**
     * @see #setCategories(String...)
     */
    abstract public String[] getCategories();

    /**
     * <p>
     * If categories are present for the axis, names are used instead of
     * numbers for that axis. Since Highcharts 3.0, categories can also be
     * extracted by giving each point a <code>name</code> and
     * setting axis type to <code>category</code>.
     * However, if you have multiple series, best practice remains defining the
     * <code>categories</code> array.
     * </p>
     *
     * <p>
     * Example:
     *
     * <pre>
     * categories: ['Apples', 'Bananas', 'Oranges']
     * </pre>
     * </p>
     */
    abstract public void setCategories(String... categories);

    /**
     * Adds category to the categories array
     *
     * @param category
     *            to add
     * @see #setCategories(String...)
     */
    abstract public void addCategory(String category);

    /**
     * Removes first occurrence of category in categories array
     *
     * @param category
     *            to remove
     * @see #setCategories(String...)
     */
    abstract public void removeCategory(String category);

    /**
     * @see #setCeiling(Number)
     */
    abstract public Number getCeiling();

    /**
     * The highest allowed value for automatically computed axis extremes.
     */
    abstract public void setCeiling(Number ceiling);

    /**
     * @see #setDateTimeLabelFormats(DateTimeLabelFormats)
     */
    abstract public DateTimeLabelFormats getDateTimeLabelFormats();

    /**
     * For a datetime axis, the scale will automatically adjust to the
     * appropriate unit. This member gives the default string representations
     * used for each unit. For an overview of the replacement codes, see
     * dateFormat.
     */
    abstract public void setDateTimeLabelFormats(
        DateTimeLabelFormats dateTimeLabelFormats);

    /**
     * @see #setEndOnTick(Boolean)
     */
    abstract public Boolean getEndOnTick();

    /**
     * Whether to force the axis to end on a tick. Use this option with the
     * <code>maxPadding</code> option to control the axis end.
     */
    protected abstract void setEndOnTick(Boolean endOnTick);

    /**
     * @see #setFloor(Number)
     */
    abstract public Number getFloor();

    /**
     * The lowest allowed value for automatically computed axis extremes.
     */
    abstract public void setFloor(Number floor);

    /**
     * @see #setGridLineColor(Color)
     */
    abstract public Color getGridLineColor();

    /**
     * Color of the grid lines extending the ticks across the plot area.
     */
    abstract public void setGridLineColor(Color gridLineColor);

    /**
     * @see #setGridLineDashStyle(DashStyle)
     */
    abstract public DashStyle getGridLineDashStyle();

    /**
     * The dash or dot style of the grid lines.
     */
    abstract public void setGridLineDashStyle(DashStyle gridLineDashStyle);

    /**
     * @see #setGridLineWidth(Number)
     */
    abstract public Number getGridLineWidth();

    /**
     * The width of the grid lines extending the ticks across the plot area.
     */
    abstract public void setGridLineWidth(Number gridLineWidth);

    /**
     * @see #setId(String)
     */
    abstract public String getId();

    /**
     * An id for the axis. This can be used after render time to get a pointer
     * to the axis object through <code>chart.get()</code>.
     */
    abstract public void setId(String id);

    /**
     * @see #setLabels(Labels)
     */
    abstract public Labels getLabels();

    /**
     * The axis labels show the number or category for each tick.
     */
    abstract public void setLabels(Labels labels);

    /**
     * @see #setLinkedTo(Number)
     */
    abstract public Number getLinkedTo();

    /**
     * Index of another axis that this axis is linked to. When an axis is linked
     * to a master axis, it will take the same extremes as the master, but as
     * assigned by min or max or by setExtremes. It can be used to show
     * additional info, or to ease reading the chart by duplicating the scales.
     */
    abstract public void setLinkedTo(Number linkedTo);

    /**
     * @see #setMaxPadding(Number)
     */
    abstract public Number getMaxPadding();

    /**
     * Padding of the max value relative to the length of the axis. A padding of
     * 0.05 will make a 100px axis 5px longer. This is useful when you don't
     * want the highest data value to appear on the edge of the plot area. When
     * the axis' <code>max</code> option is set or a max extreme is set using
     * <code>axis.setExtremes()</code>, the maxPadding will be ignored.
     */
    abstract public void setMaxPadding(Number maxPadding);

    /**
     * @see #setMinPadding(Number)
     */
    abstract public Number getMinPadding();

    /**
     * Padding of the min value relative to the length of the axis. A padding of
     * 0.05 will make a 100px axis 5px longer. This is useful when you don't
     * want the lowest data value to appear on the edge of the plot area. When
     * the axis' <code>min</code> option is set or a min extreme is set using
     * <code>axis.setExtremes()</code>, the minPadding will be ignored.
     */
    abstract public void setMinPadding(Number minPadding);

    /**
     * @see #setMinTickInterval(Number)
     */
    abstract public Number getMinTickInterval();

    /**
     * The minimum tick interval allowed in axis values. For example on zooming
     * in on an axis with daily data, this can be used to prevent the axis from
     * showing hours.
     */
    abstract public void setMinTickInterval(Number minTickInterval);

    /**
     * @see #setMinorGridLineColor(Color)
     */
    abstract public Color getMinorGridLineColor();

    /**
     * Color of the minor, secondary grid lines.
     */
    abstract public void setMinorGridLineColor(Color minorGridLineColor);

    /**
     * @see #setMinorGridLineDashStyle(DashStyle)
     */
    abstract public DashStyle getMinorGridLineDashStyle();

    /**
     * The dash or dot style of the minor grid lines.
     */
    abstract public void setMinorGridLineDashStyle(DashStyle minorGridLineDashStyle);

    /**
     * @see #setMinorGridLineWidth(Number)
     */
    abstract public Number getMinorGridLineWidth();

    /**
     * Width of the minor, secondary grid lines.
     */
    abstract public void setMinorGridLineWidth(Number minorGridLineWidth);

    /**
     * @see #setMinorTickColor(Color)
     */
    abstract public Color getMinorTickColor();

    /**
     * Color for the minor tick marks.
     */
    abstract public void setMinorTickColor(Color minorTickColor);

    /**
     * @see #setMinorTickInterval(String)
     */
    abstract public String getMinorTickInterval();

    /**
     * <p>
     * Tick interval in scale units for the minor ticks. On a linear axis, if
     * <code>"auto"</code>, the minor tick interval is calculated as a fifth of
     * the tickInterval. If <code>null</code>, minor ticks are not shown.
     * </p>
     * <p>
     * On logarithmic axes, the unit is the power of the value. For example,
     * setting the minorTickInterval to 1 puts one tick on each of 0.1, 1, 10,
     * 100 etc. Setting the minorTickInterval to 0.1 produces 9 ticks between 1
     * and 10, 10 and 100 etc. A minorTickInterval of "auto" on a log axis
     * results in a best guess, attempting to enter approximately 5 minor ticks
     * between each major tick.
     * </p>
     *
     * <p>
     * If user settings dictate minor ticks to become too dense, they don't make
     * sense, and will be ignored to prevent performance problems.</a>
     *
     * <p>
     * On axes using <code>categories</code>, minor ticks are
     * not supported.
     * </p>
     */
    abstract public void setMinorTickInterval(String minorTickInterval);

    /**
     * @see #setMinorTickLength(Number)
     */
    abstract public Number getMinorTickLength();

    /**
     * The pixel length of the minor tick marks.
     */
    abstract public void setMinorTickLength(Number minorTickLength);

    /**
     * @see #setMinorTickPosition(TickPosition)
     */
    abstract public TickPosition getMinorTickPosition();

    /**
     * The position of the minor tick marks relative to the axis line. Can be
     * one of <code>inside</code> and <code>outside</code>.
     */
    abstract public void setMinorTickPosition(TickPosition minorTickPosition);

    /**
     * @see #setMinorTickWidth(Number)
     */
    abstract public Number getMinorTickWidth();

    /**
     * The pixel width of the minor tick mark.
     */
    abstract public void setMinorTickWidth(Number minorTickWidth);

    /**
     * @see #setOffset(Number)
     */
    abstract public Number getOffset();

    /**
     * The distance in pixels from the plot area to the axis line. A positive
     * offset moves the axis with it's line, labels and ticks away from the plot
     * area. This is typically used when two or more axes are displayed on the
     * same side of the plot.
     */
    abstract public void setOffset(Number offset);

    /**
     * @see #setOpposite(Boolean)
     */
    abstract public Boolean getOpposite();

    /**
     * Whether to display the axis on the opposite side of the normal. The
     * normal is on the left side for vertical axes and bottom for horizontal,
     * so the opposite sides will be right and top respectively. This is
     * typically used with dual or multiple axes.
     */
    abstract public void setOpposite(Boolean opposite);

    /**
     * @see #setPlotBands(PlotBand...)
     */
    abstract public PlotBand[] getPlotBands();

    /**
     * <p>
     * An array of colored bands stretching across the plot area marking an
     * interval on the axis.
     * </p>
     */
    abstract public void setPlotBands(PlotBand... plotBands);

    /**
     * Adds plotBand to the plotBands array
     *
     * @param plotBand
     *            to add
     * @see #setPlotBands(PlotBand...)
     */
    abstract public void addPlotBand(PlotBand plotBand);

    /**
     * Removes first occurrence of plotBand in plotBands array
     *
     * @param plotBand
     *            to remove
     * @see #setPlotBands(PlotBand...)
     */
    abstract public void removePlotBand(PlotBand plotBand);

    /**
     * @see #setPlotLines(PlotLine...)
     */
    abstract public PlotLine[] getPlotLines();

    /**
     * An array of lines stretching across the plot area, marking a specific
     * value on one of the axes.
     */
    abstract public void setPlotLines(PlotLine... plotLines);

    /**
     * Adds plotLine to the plotLines array
     *
     * @param plotLine
     *            to add
     * @see #setPlotLines(PlotLine...)
     */
    abstract public void addPlotLine(PlotLine plotLine);

    /**
     * Removes first occurrence of plotLine in plotLines array
     *
     * @param plotLine
     *            to remove
     * @see #setPlotLines(PlotLine...)
     */
    abstract public void removePlotLine(PlotLine plotLine);

    /**
     * @see #setReversed(Boolean)
     */
    abstract public Boolean getReversed();

    /**
     * Whether to reverse the axis so that the highest number is closest to the
     * origin.
     */
    abstract public void setReversed(Boolean reversed);

    /**
     * @see #setShowEmpty(Boolean)
     */
    abstract public Boolean getShowEmpty();

    /**
     * Whether to show the axis line and title when the axis has no data.
     */
    abstract public void setShowEmpty(Boolean showEmpty);

    /**
     * @see #setShowFirstLabel(Boolean)
     */
    abstract public Boolean getShowFirstLabel();

    /**
     * Whether to show the first tick label.
     */
    abstract public void setShowFirstLabel(Boolean showFirstLabel);

    /**
     * @see #setShowLastLabel(Boolean)
     */
    abstract public Boolean getShowLastLabel();

    /**
     * Whether to show the last tick label.
     */
    abstract public void setShowLastLabel(Boolean showLastLabel);

    /**
     * @see #setStartOfWeek(Number)
     */
    abstract public Number getStartOfWeek();

    /**
     * For datetime axes, this decides where to put the tick between weeks. 0 =
     * Sunday, 1 = Monday.
     */
    abstract public void setStartOfWeek(Number startOfWeek);

    /**
     * @see #setStartOnTick(Boolean)
     */
    abstract public Boolean getStartOnTick();

    /**
     * Whether to force the axis to start on a tick. Use this option with the
     * <code>minPadding</code> option to control the axis start.
     */
    abstract public void setStartOnTick(Boolean startOnTick);

    /**
     * @see #setTickAmount(Number)
     */
    abstract public Number getTickAmount();

    /**
     * <p>
     * The amount of ticks to draw on the axis. This opens up for aligning the
     * ticks of multiple charts or panes within a chart. This option overrides
     * the <code>tickPixelInterval</code> option.
     * </p>
     * <p>
     * This option only has an effect on linear axes. Datetime, logarithmic or
     * category axes are not affected.
     * </p>
     */
    abstract public void setTickAmount(Number tickAmount);

    /**
     * @see #setTickColor(Color)
     */
    abstract public Color getTickColor();

    /**
     * Color for the main tick marks.
     */
    abstract public void setTickColor(Color tickColor);

    /**
     * @see #setTickInterval(Number)
     */
    abstract public Number getTickInterval();

    /**
     * <p>
     * The interval of the tick marks in axis units. When <code>null</code>, the
     * tick interval is computed to approximately follow the
     * <code>tickPixelInterval</code> on linear and
     * datetime axes. On categorized axes, a <code>null</code> tickInterval will
     * default to 1, one category. Note that datetime axes are based on
     * milliseconds, so for example an interval of one day is expressed as
     * <code>24 * 3600 * 1000</code>.
     * </p>
     * <p>
     * On logarithmic axes, the tickInterval is based on powers, so a
     * tickInterval of 1 means one tick on each of 0.1, 1, 10, 100 etc. A
     * tickInterval of 2 means a tick of 0.1, 10, 1000 etc. A tickInterval of
     * 0.2 puts a tick on 0.1, 0.2, 0.4, 0.6, 0.8, 1, 2, 4, 6, 8, 10, 20, 40
     * etc.
     * </p>
     * <p>
     * If the tickInterval is too dense for labels to be drawn, Highcharts may
     * remove ticks.
     * </p>
     */
    abstract public void setTickInterval(Number tickInterval);

    /**
     * @see #setTickLength(Number)
     */
    abstract public Number getTickLength();

    /**
     * The pixel length of the main tick marks.
     */
    abstract public void setTickLength(Number tickLength);

    /**
     * @see #setTickPixelInterval(Number)
     */
    abstract public Number getTickPixelInterval();

    /**
     * If tickInterval is <code>null</code> this option sets the approximate
     * pixel interval of the tick marks. Not applicable to categorized axis.
     */
    abstract public void setTickPixelInterval(Number tickPixelInterval);

    /**
     * @see #setTickPosition(TickPosition)
     */
    abstract public TickPosition getTickPosition();

    /**
     * The position of the major tick marks relative to the axis line. Can be
     * one of <code>inside</code> and <code>outside</code>.
     */
    abstract public void setTickPosition(TickPosition tickPosition);

    /**
     * @see #setTickPositions(Number[])
     */
    abstract public Number[] getTickPositions();

    /**
     * An array defining where the ticks are laid out on the axis. This
     * overrides the default behaviour of <code>tickPixelInterval</code> and
     * <code>tickInterval</code>.
     */
    abstract public void setTickPositions(Number[] tickPositions);

    /**
     * @see #setTickWidth(Number)
     */
    abstract public Number getTickWidth();

    /**
     * The pixel width of the major tick marks.
     */
    abstract public void setTickWidth(Number tickWidth);

    /**
     * @see #setTickmarkPlacement(TickmarkPlacement)
     */
    abstract public TickmarkPlacement getTickmarkPlacement();

    /**
     * For categorized axes only. If <code>on</code> the tick mark is placed in
     * the center of the category, if <code>between</code> the tick mark is
     * placed between categories. The default is <code>between</code> if the
     * <code>tickInterval</code> is 1, else <code>on</code>.
     */
    abstract public void setTickmarkPlacement(TickmarkPlacement tickmarkPlacement);

    /**
     * @see #setTitle(AxisTitle)
     */
    abstract public AxisTitle getTitle();

    /**
     * The axis title, showing next to the axis line.
     */
    abstract public void setTitle(AxisTitle title);

    /**
     * @see #setType(AxisType)
     */
    abstract public AxisType getType();

    /**
     * The type of axis. Can be one of <code>"linear"</code>,
     * <code>"logarithmic"</code>, <code>"datetime"</code> or
     * <code>"category"</code>. In a datetime axis, the numbers are given in
     * milliseconds, and tick marks are placed on appropriate values like full
     * hours or days. In a category axis, the point
     * names of the chart's series are used for categories, if not a
     * categories array is defined.
     */
    abstract public void setType(AxisType type);

    /**
     * @see #setUnits(TimeUnitMultiples...)
     */
    abstract public TimeUnitMultiples[] getUnits();

    /**
     * Datetime axis only. An array determining what time intervals the ticks
     * are allowed to fall on. Each array item is an array where the first value
     * is the time unit and the second value another array of allowed multiples.
     */
    abstract public void setUnits(TimeUnitMultiples... units);

    /**
     * Adds unit to the units array
     *
     * @param unit
     *            to add
     * @see #setUnits(TimeUnitMultiples...)
     */
    abstract public void addUnit(TimeUnitMultiples unit);

    /**
     * Removes first occurrence of unit in units array
     *
     * @param unit
     *            to remove
     * @see #setUnits(TimeUnitMultiples...)
     */
    abstract public void removeUnit(TimeUnitMultiples unit);

    /**
     * @see #setVisible(Boolean)
     */
    abstract public Boolean getVisible();

    /**
     * Whether axis, including axis title, line, ticks and labels, should be
     * visible.
     */
    abstract public void setVisible(Boolean visible);

    abstract public void setTitle(String title);

}
