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

/**
 * <p>
 * The Z axis or depth axis for 3D plots.
 * </p>
 * <p>
 * See <a class="internal" href="#Axis">the Axis object</a> for programmatic
 * access to the axis.
 * </p>
 */
public class ZAxis extends Axis {

    private Boolean allowDecimals;
    private Color alternateGridColor;
    private ArrayList<String> categories;
    private Number ceiling;
    private String className;
    private Crosshair crosshair;
    private DateTimeLabelFormats dateTimeLabelFormats;
    private String description;
    private Boolean endOnTick;
    private Number floor;
    private Color gridLineColor;
    private DashStyle gridLineDashStyle;
    private Number gridLineWidth;
    private Number gridZIndex;
    private String id;
    private Labels labels;
    private Color lineColor;
    private Number lineWidth;
    private Number linkedTo;
    private Number maxPadding;
    private Number minPadding;
    private Number minRange;
    private Number minTickInterval;
    private Color minorGridLineColor;
    private DashStyle minorGridLineDashStyle;
    private Number minorGridLineWidth;
    private Color minorTickColor;
    private String minorTickInterval;
    private Number minorTickLength;
    private TickPosition minorTickPosition;
    private Number minorTickWidth;
    private Number offset;
    private Boolean opposite;
    private ArrayList<PlotBand> plotBands;
    private ArrayList<PlotLine> plotLines;
    private Boolean reversed;
    private Boolean showEmpty;
    private Boolean showFirstLabel;
    private Boolean showLastLabel;
    private Number softMax;
    private Number softMin;
    private Number startOfWeek;
    private Boolean startOnTick;
    private Number tickAmount;
    private Color tickColor;
    private Number tickInterval;
    private Number tickLength;
    private Number tickPixelInterval;
    private TickPosition tickPosition;
    private Number[] tickPositions;
    private Number tickWidth;
    private TickmarkPlacement tickmarkPlacement;
    private AxisTitle title;
    private AxisType type;
    private Boolean uniqueNames;
    private ArrayList<TimeUnitMultiples> units;
    private Boolean visible;

    public ZAxis() {
    }

    /**
     * @see #setAllowDecimals(Boolean)
     */
    public Boolean getAllowDecimals() {
        return allowDecimals;
    }

    /**
     * Whether to allow decimals in this axis' ticks. When counting integers,
     * like persons or hits on a web page, decimals should be avoided in the
     * labels.
     * <p>
     * Defaults to: true
     */
    public void setAllowDecimals(Boolean allowDecimals) {
        this.allowDecimals = allowDecimals;
    }

    /**
     * @see #setAlternateGridColor(Color)
     */
    public Color getAlternateGridColor() {
        return alternateGridColor;
    }

    /**
     * When using an alternate grid color, a band is painted across the plot
     * area between every other grid line.
     */
    public void setAlternateGridColor(Color alternateGridColor) {
        this.alternateGridColor = alternateGridColor;
    }

    /**
     * @see #setCategories(String...)
     */
    public String[] getCategories() {
        if (categories == null) {
            return new String[] {};
        }
        String[] arr = new String[categories.size()];
        categories.toArray(arr);
        return arr;
    }

    /**
     * <p>
     * If categories are present for the xAxis, names are used instead of
     * numbers for that axis. Since Highcharts 3.0, categories can also be
     * extracted by giving each point a <a href="#series.data">name</a> and
     * setting axis <a href="#xAxis.type">type</a> to <code>category</code>.
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
     *
     * Defaults to <code>null</code>
     * </p>
     */
    public void setCategories(String... categories) {
        this.categories = new ArrayList<String>(Arrays.asList(categories));
    }

    /**
     * Adds category to the categories array
     *
     * @param category
     *            to add
     * @see #setCategories(String...)
     */
    public void addCategory(String category) {
        if (this.categories == null) {
            this.categories = new ArrayList<String>();
        }
        this.categories.add(category);
    }

    /**
     * Removes first occurrence of category in categories array
     *
     * @param category
     *            to remove
     * @see #setCategories(String...)
     */
    public void removeCategory(String category) {
        this.categories.remove(category);
    }

    /**
     * @see #setCeiling(Number)
     */
    public Number getCeiling() {
        return ceiling;
    }

    /**
     * The highest allowed value for automatically computed axis extremes.
     */
    public void setCeiling(Number ceiling) {
        this.ceiling = ceiling;
    }

    /**
     * @see #setClassName(String)
     */
    public String getClassName() {
        return className;
    }

    /**
     * A class name that opens for styling the axis by CSS, especially in
     * Highcharts <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>. The class name is applied to group elements for the
     * grid, axis elements and labels.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @see #setCrosshair(Crosshair)
     */
    public Crosshair getCrosshair() {
        if (crosshair == null) {
            crosshair = new Crosshair();
        }
        return crosshair;
    }

    /**
     * <p>
     * Configure a crosshair that follows either the mouse pointer or the
     * hovered point.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the crosshairs are styled in the
     * <code>.highcharts-crosshair</code>,
     * <code>.highcharts-crosshair-thin</code> or
     * <code>.highcharts-xaxis-category</code> classes.
     * </p>
     * <p>
     * Defaults to: false
     */
    public void setCrosshair(Crosshair crosshair) {
        this.crosshair = crosshair;
    }

    /**
     * @see #setDateTimeLabelFormats(DateTimeLabelFormats)
     */
    public DateTimeLabelFormats getDateTimeLabelFormats() {
        if (dateTimeLabelFormats == null) {
            dateTimeLabelFormats = new DateTimeLabelFormats();
        }
        return dateTimeLabelFormats;
    }

    /**
     * For a datetime axis, the scale will automatically adjust to the
     * appropriate unit. This member gives the default string representations
     * used for each unit. For intermediate values, different units may be used,
     * for example the <code>day</code> unit can be used on midnight and
     * <code>hour</code> unit be used for intermediate values on the same axis.
     * For an overview of the replacement codes, see
     * <a href="#Highcharts.dateFormat">dateFormat</a>.
     *
     * Defaults to:
     *
     * <pre>
     * {
     * 		millisecond: '%H:%M:%S.%L',
     * 		second: '%H:%M:%S',
     * 		minute: '%H:%M',
     * 		hour: '%H:%M',
     * 		day: '%e. %b',
     * 		week: '%e. %b',
     * 		month: '%b \'%y',
     * 		year: '%Y'
     * 	}
     * </pre>
     */
    public void setDateTimeLabelFormats(
            DateTimeLabelFormats dateTimeLabelFormats) {
        this.dateTimeLabelFormats = dateTimeLabelFormats;
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
     *
     * <p>
     * Description of the axis to screen reader users.
     * </p>
     * <p>
     * Defaults to: undefined
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see #setEndOnTick(Boolean)
     */
    public Boolean getEndOnTick() {
        return endOnTick;
    }

    /**
     * Whether to force the axis to end on a tick. Use this option with the
     * <code>maxPadding</code> option to control the axis end.
     * <p>
     * Defaults to: false
     */
    public void setEndOnTick(Boolean endOnTick) {
        this.endOnTick = endOnTick;
    }

    /**
     * @see #setFloor(Number)
     */
    public Number getFloor() {
        return floor;
    }

    /**
     * The lowest allowed value for automatically computed axis extremes.
     * <p>
     * Defaults to: null
     */
    public void setFloor(Number floor) {
        this.floor = floor;
    }

    /**
     * @see #setGridLineColor(Color)
     */
    public Color getGridLineColor() {
        return gridLineColor;
    }

    /**
     * <p>
     * Color of the grid lines extending the ticks across the plot area.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the stroke is given in the
     * <code>.highcharts-grid-line</code> class.
     * </p>
     * <p>
     * Defaults to: #e6e6e6
     */
    public void setGridLineColor(Color gridLineColor) {
        this.gridLineColor = gridLineColor;
    }

    /**
     * @see #setGridLineDashStyle(DashStyle)
     */
    public DashStyle getGridLineDashStyle() {
        return gridLineDashStyle;
    }

    /**
     * The dash or dot style of the grid lines. For possible values, see
     * <a href=
     * "http://jsfiddle.net/gh/get/library/pure/highcharts/highcharts/tree/master/samples/highcharts/plotoptions/series-dashstyle-all/"
     * >this demonstration</a>.
     * <p>
     * Defaults to: Solid
     */
    public void setGridLineDashStyle(DashStyle gridLineDashStyle) {
        this.gridLineDashStyle = gridLineDashStyle;
    }

    /**
     * @see #setGridLineWidth(Number)
     */
    public Number getGridLineWidth() {
        return gridLineWidth;
    }

    /**
     * <p>
     * The width of the grid lines extending the ticks across the plot area.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the stroke width is given in the
     * <code>.highcharts-grid-line</code> class.
     * </p>
     * <p>
     * Defaults to: 0
     */
    public void setGridLineWidth(Number gridLineWidth) {
        this.gridLineWidth = gridLineWidth;
    }

    /**
     * @see #setGridZIndex(Number)
     */
    public Number getGridZIndex() {
        return gridZIndex;
    }

    /**
     * The Z index of the grid lines.
     * <p>
     * Defaults to: 1
     */
    public void setGridZIndex(Number gridZIndex) {
        this.gridZIndex = gridZIndex;
    }

    /**
     * @see #setId(String)
     */
    public String getId() {
        return id;
    }

    /**
     * An id for the axis. This can be used after render time to get a pointer
     * to the axis object through <code>chart.get()</code>.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @see #setLabels(Labels)
     */
    public Labels getLabels() {
        if (labels == null) {
            labels = new Labels();
        }
        return labels;
    }

    /**
     * The axis labels show the number or category for each tick.
     */
    public void setLabels(Labels labels) {
        this.labels = labels;
    }

    /**
     * @see #setLineColor(Color)
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * <p>
     * The color of the line marking the axis itself.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the line stroke is given in the
     * <code>.highcharts-axis-line</code> or <code>.highcharts-xaxis-line</code>
     * class.
     * </p>
     * <p>
     * Defaults to: #ccd6eb
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
     * <p>
     * The width of the line marking the axis itself.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the stroke width is given in the
     * <code>.highcharts-axis-line</code> or <code>.highcharts-xaxis-line</code>
     * class.
     * </p>
     * <p>
     * Defaults to: 1
     */
    public void setLineWidth(Number lineWidth) {
        this.lineWidth = lineWidth;
    }

    /**
     * @see #setLinkedTo(Number)
     */
    public Number getLinkedTo() {
        return linkedTo;
    }

    /**
     * Index of another axis that this axis is linked to. When an axis is linked
     * to a master axis, it will take the same extremes as the master, but as
     * assigned by min or max or by setExtremes. It can be used to show
     * additional info, or to ease reading the chart by duplicating the scales.
     */
    public void setLinkedTo(Number linkedTo) {
        this.linkedTo = linkedTo;
    }

    /**
     * @see #setMaxPadding(Number)
     */
    public Number getMaxPadding() {
        return maxPadding;
    }

    /**
     * Padding of the max value relative to the length of the axis. A padding of
     * 0.05 will make a 100px axis 5px longer. This is useful when you don't
     * want the highest data value to appear on the edge of the plot area. When
     * the axis' <code>max</code> option is set or a max extreme is set using
     * <code>axis.setExtremes()</code>, the maxPadding will be ignored.
     * <p>
     * Defaults to: 0.01
     */
    public void setMaxPadding(Number maxPadding) {
        this.maxPadding = maxPadding;
    }

    /**
     * @see #setMinPadding(Number)
     */
    public Number getMinPadding() {
        return minPadding;
    }

    /**
     * Padding of the min value relative to the length of the axis. A padding of
     * 0.05 will make a 100px axis 5px longer. This is useful when you don't
     * want the lowest data value to appear on the edge of the plot area. When
     * the axis' <code>min</code> option is set or a min extreme is set using
     * <code>axis.setExtremes()</code>, the minPadding will be ignored.
     * <p>
     * Defaults to: 0.01
     */
    public void setMinPadding(Number minPadding) {
        this.minPadding = minPadding;
    }

    /**
     * @see #setMinRange(Number)
     */
    public Number getMinRange() {
        return minRange;
    }

    /**
     * <p>
     * The minimum range to display on this axis. The entire axis will not be
     * allowed to span over a smaller interval than this. For example, for a
     * datetime axis the main unit is milliseconds. If minRange is set to
     * 3600000, you can't zoom in more than to one hour.
     * </p>
     *
     * <p>
     * The default minRange for the x axis is five times the smallest interval
     * between any of the data points.
     * </p>
     *
     * <p>
     * On a logarithmic axis, the unit for the minimum range is the power. So a
     * minRange of 1 means that the axis can be zoomed to 10-100, 100-1000,
     * 1000-10000 etc.
     * </p>
     *
     * <p>
     * Note that the <code>minPadding</code>, <code>maxPadding</code>,
     * <code>startOnTick</code> and <code>endOnTick</code> settings also affect
     * how the extremes of the axis are computed.
     * </p>
     */
    public void setMinRange(Number minRange) {
        this.minRange = minRange;
    }

    /**
     * @see #setMinTickInterval(Number)
     */
    public Number getMinTickInterval() {
        return minTickInterval;
    }

    /**
     * The minimum tick interval allowed in axis values. For example on zooming
     * in on an axis with daily data, this can be used to prevent the axis from
     * showing hours. Defaults to the closest distance between two points on the
     * axis.
     */
    public void setMinTickInterval(Number minTickInterval) {
        this.minTickInterval = minTickInterval;
    }

    /**
     * @see #setMinorGridLineColor(Color)
     */
    public Color getMinorGridLineColor() {
        return minorGridLineColor;
    }

    /**
     * <p>
     * Color of the minor, secondary grid lines.
     * </p>
     *
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the stroke width is given in the
     * <code>.highcharts-minor-grid-line</code> class.
     * </p>
     * <p>
     * Defaults to: #f2f2f2
     */
    public void setMinorGridLineColor(Color minorGridLineColor) {
        this.minorGridLineColor = minorGridLineColor;
    }

    /**
     * @see #setMinorGridLineDashStyle(DashStyle)
     */
    public DashStyle getMinorGridLineDashStyle() {
        return minorGridLineDashStyle;
    }

    /**
     * The dash or dot style of the minor grid lines. For possible values, see
     * <a href=
     * "http://jsfiddle.net/gh/get/library/pure/highcharts/highcharts/tree/master/samples/highcharts/plotoptions/series-dashstyle-all/"
     * >this demonstration</a>.
     * <p>
     * Defaults to: Solid
     */
    public void setMinorGridLineDashStyle(DashStyle minorGridLineDashStyle) {
        this.minorGridLineDashStyle = minorGridLineDashStyle;
    }

    /**
     * @see #setMinorGridLineWidth(Number)
     */
    public Number getMinorGridLineWidth() {
        return minorGridLineWidth;
    }

    /**
     * <p>
     * Width of the minor, secondary grid lines.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the stroke width is given in the
     * <code>.highcharts-grid-line</code> class.
     * </p>
     * <p>
     * Defaults to: 1
     */
    public void setMinorGridLineWidth(Number minorGridLineWidth) {
        this.minorGridLineWidth = minorGridLineWidth;
    }

    /**
     * @see #setMinorTickColor(Color)
     */
    public Color getMinorTickColor() {
        return minorTickColor;
    }

    /**
     * Color for the minor tick marks.
     * <p>
     * Defaults to: #999999
     */
    public void setMinorTickColor(Color minorTickColor) {
        this.minorTickColor = minorTickColor;
    }

    /**
     * @see #setMinorTickInterval(String)
     */
    public String getMinorTickInterval() {
        return minorTickInterval;
    }

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
     * On axes using <a href="#xAxis.categories">categories</a>, minor ticks are
     * not supported.
     * </p>
     */
    public void setMinorTickInterval(String minorTickInterval) {
        this.minorTickInterval = minorTickInterval;
    }

    /**
     * @see #setMinorTickLength(Number)
     */
    public Number getMinorTickLength() {
        return minorTickLength;
    }

    /**
     * The pixel length of the minor tick marks.
     * <p>
     * Defaults to: 2
     */
    public void setMinorTickLength(Number minorTickLength) {
        this.minorTickLength = minorTickLength;
    }

    /**
     * @see #setMinorTickPosition(TickPosition)
     */
    public TickPosition getMinorTickPosition() {
        return minorTickPosition;
    }

    /**
     * The position of the minor tick marks relative to the axis line. Can be
     * one of <code>inside</code> and <code>outside</code>.
     * <p>
     * Defaults to: outside
     */
    public void setMinorTickPosition(TickPosition minorTickPosition) {
        this.minorTickPosition = minorTickPosition;
    }

    /**
     * @see #setMinorTickWidth(Number)
     */
    public Number getMinorTickWidth() {
        return minorTickWidth;
    }

    /**
     * The pixel width of the minor tick mark.
     * <p>
     * Defaults to: 0
     */
    public void setMinorTickWidth(Number minorTickWidth) {
        this.minorTickWidth = minorTickWidth;
    }

    /**
     * @see #setOffset(Number)
     */
    public Number getOffset() {
        return offset;
    }

    /**
     * The distance in pixels from the plot area to the axis line. A positive
     * offset moves the axis with it's line, labels and ticks away from the plot
     * area. This is typically used when two or more axes are displayed on the
     * same side of the plot. With multiple axes the offset is dynamically
     * adjusted to avoid collision, this can be overridden by setting offset
     * explicitly.
     * <p>
     * Defaults to: 0
     */
    public void setOffset(Number offset) {
        this.offset = offset;
    }

    /**
     * @see #setOpposite(Boolean)
     */
    public Boolean getOpposite() {
        return opposite;
    }

    /**
     * Whether to display the axis on the opposite side of the normal. The
     * normal is on the left side for vertical axes and bottom for horizontal,
     * so the opposite sides will be right and top respectively. This is
     * typically used with dual or multiple axes.
     * <p>
     * Defaults to: false
     */
    public void setOpposite(Boolean opposite) {
        this.opposite = opposite;
    }

    /**
     * @see #setPlotBands(PlotBand...)
     */
    public PlotBand[] getPlotBands() {
        if (plotBands == null) {
            return new PlotBand[] {};
        }
        PlotBand[] arr = new PlotBand[plotBands.size()];
        plotBands.toArray(arr);
        return arr;
    }

    /**
     * <p>
     * An array of colored bands stretching across the plot area marking an
     * interval on the axis.
     * </p>
     *
     * <p>
     * In a gauge, a plot band on the Y axis (value axis) will stretch along the
     * perimeter of the gauge.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the plot bands are styled by the
     * <code>.highcharts-plot-band</code> class in addition to the
     * <code>className</code> option.
     * </p>
     */
    public void setPlotBands(PlotBand... plotBands) {
        this.plotBands = new ArrayList<PlotBand>(Arrays.asList(plotBands));
    }

    /**
     * Adds plotBand to the plotBands array
     *
     * @param plotBand
     *            to add
     * @see #setPlotBands(PlotBand...)
     */
    public void addPlotBand(PlotBand plotBand) {
        if (this.plotBands == null) {
            this.plotBands = new ArrayList<PlotBand>();
        }
        this.plotBands.add(plotBand);
    }

    /**
     * Removes first occurrence of plotBand in plotBands array
     *
     * @param plotBand
     *            to remove
     * @see #setPlotBands(PlotBand...)
     */
    public void removePlotBand(PlotBand plotBand) {
        this.plotBands.remove(plotBand);
    }

    /**
     * @see #setPlotLines(PlotLine...)
     */
    public PlotLine[] getPlotLines() {
        if (plotLines == null) {
            return new PlotLine[] {};
        }
        PlotLine[] arr = new PlotLine[plotLines.size()];
        plotLines.toArray(arr);
        return arr;
    }

    /**
     * <p>
     * An array of lines stretching across the plot area, marking a specific
     * value on one of the axes.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the plot lines are styled by the
     * <code>.highcharts-plot-line</code> class in addition to the
     * <code>className</code> option.
     * </p>
     */
    public void setPlotLines(PlotLine... plotLines) {
        this.plotLines = new ArrayList<PlotLine>(Arrays.asList(plotLines));
    }

    /**
     * Adds plotLine to the plotLines array
     *
     * @param plotLine
     *            to add
     * @see #setPlotLines(PlotLine...)
     */
    public void addPlotLine(PlotLine plotLine) {
        if (this.plotLines == null) {
            this.plotLines = new ArrayList<PlotLine>();
        }
        this.plotLines.add(plotLine);
    }

    /**
     * Removes first occurrence of plotLine in plotLines array
     *
     * @param plotLine
     *            to remove
     * @see #setPlotLines(PlotLine...)
     */
    public void removePlotLine(PlotLine plotLine) {
        this.plotLines.remove(plotLine);
    }

    /**
     * @see #setReversed(Boolean)
     */
    public Boolean getReversed() {
        return reversed;
    }

    /**
     * Whether to reverse the axis so that the highest number is closest to the
     * origin. If the chart is inverted, the x axis is reversed by default.
     * <p>
     * Defaults to: false
     */
    public void setReversed(Boolean reversed) {
        this.reversed = reversed;
    }

    /**
     * @see #setShowEmpty(Boolean)
     */
    public Boolean getShowEmpty() {
        return showEmpty;
    }

    /**
     * Whether to show the axis line and title when the axis has no data.
     * <p>
     * Defaults to: true
     */
    public void setShowEmpty(Boolean showEmpty) {
        this.showEmpty = showEmpty;
    }

    /**
     * @see #setShowFirstLabel(Boolean)
     */
    public Boolean getShowFirstLabel() {
        return showFirstLabel;
    }

    /**
     * Whether to show the first tick label.
     * <p>
     * Defaults to: true
     */
    public void setShowFirstLabel(Boolean showFirstLabel) {
        this.showFirstLabel = showFirstLabel;
    }

    /**
     * @see #setShowLastLabel(Boolean)
     */
    public Boolean getShowLastLabel() {
        return showLastLabel;
    }

    /**
     * Whether to show the last tick label.
     * <p>
     * Defaults to: true
     */
    public void setShowLastLabel(Boolean showLastLabel) {
        this.showLastLabel = showLastLabel;
    }

    /**
     * @see #setSoftMax(Number)
     */
    public Number getSoftMax() {
        return softMax;
    }

    /**
     * A soft maximum for the axis. If the series data maximum is less than
     * this, the axis will stay at this maximum, but if the series data maximum
     * is higher, the axis will flex to show all data.
     */
    public void setSoftMax(Number softMax) {
        this.softMax = softMax;
    }

    /**
     * @see #setSoftMin(Number)
     */
    public Number getSoftMin() {
        return softMin;
    }

    /**
     * A soft minimum for the axis. If the series data minimum is greater than
     * this, the axis will stay at this minimum, but if the series data minimum
     * is lower, the axis will flex to show all data.
     */
    public void setSoftMin(Number softMin) {
        this.softMin = softMin;
    }

    /**
     * @see #setStartOfWeek(Number)
     */
    public Number getStartOfWeek() {
        return startOfWeek;
    }

    /**
     * For datetime axes, this decides where to put the tick between weeks. 0 =
     * Sunday, 1 = Monday.
     * <p>
     * Defaults to: 1
     */
    public void setStartOfWeek(Number startOfWeek) {
        this.startOfWeek = startOfWeek;
    }

    /**
     * @see #setStartOnTick(Boolean)
     */
    public Boolean getStartOnTick() {
        return startOnTick;
    }

    /**
     * Whether to force the axis to start on a tick. Use this option with the
     * <code>minPadding</code> option to control the axis start.
     * <p>
     * Defaults to: false
     */
    public void setStartOnTick(Boolean startOnTick) {
        this.startOnTick = startOnTick;
    }

    /**
     * @see #setTickAmount(Number)
     */
    public Number getTickAmount() {
        return tickAmount;
    }

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
    public void setTickAmount(Number tickAmount) {
        this.tickAmount = tickAmount;
    }

    /**
     * @see #setTickColor(Color)
     */
    public Color getTickColor() {
        return tickColor;
    }

    /**
     * <p>
     * Color for the main tick marks.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the stroke is given in the
     * <code>.highcharts-tick</code> class.
     * </p>
     * <p>
     * Defaults to: #ccd6eb
     */
    public void setTickColor(Color tickColor) {
        this.tickColor = tickColor;
    }

    /**
     * @see #setTickInterval(Number)
     */
    public Number getTickInterval() {
        return tickInterval;
    }

    /**
     * <p>
     * The interval of the tick marks in axis units. When <code>null</code>, the
     * tick interval is computed to approximately follow the
     * <a href="#xAxis.tickPixelInterval">tickPixelInterval</a> on linear and
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
     *
     * <p>
     * If the tickInterval is too dense for labels to be drawn, Highcharts may
     * remove ticks.
     * </p>
     *
     * <p>
     * If the chart has multiple axes, the
     * <a href="#chart.alignTicks">alignTicks</a> option may interfere with the
     * <code>tickInterval</code> setting.
     * </p>
     */
    public void setTickInterval(Number tickInterval) {
        this.tickInterval = tickInterval;
    }

    /**
     * @see #setTickLength(Number)
     */
    public Number getTickLength() {
        return tickLength;
    }

    /**
     * The pixel length of the main tick marks.
     * <p>
     * Defaults to: 10
     */
    public void setTickLength(Number tickLength) {
        this.tickLength = tickLength;
    }

    /**
     * @see #setTickPixelInterval(Number)
     */
    public Number getTickPixelInterval() {
        return tickPixelInterval;
    }

    /**
     * <p>
     * If tickInterval is <code>null</code> this option sets the approximate
     * pixel interval of the tick marks. Not applicable to categorized axis.
     * </p>
     *
     * <p>
     * The tick interval is also influenced by the
     * <a href="#xAxis.minTickInterval">minTickInterval</a> option, that, by
     * default prevents ticks from being denser than the data points.
     * </p>
     *
     * <p>
     * Defaults to <code>72</code> for the Y axis and <code>100</code> for the X
     * axis.
     * </p>
     */
    public void setTickPixelInterval(Number tickPixelInterval) {
        this.tickPixelInterval = tickPixelInterval;
    }

    /**
     * @see #setTickPosition(TickPosition)
     */
    public TickPosition getTickPosition() {
        return tickPosition;
    }

    /**
     * The position of the major tick marks relative to the axis line. Can be
     * one of <code>inside</code> and <code>outside</code>.
     * <p>
     * Defaults to: outside
     */
    public void setTickPosition(TickPosition tickPosition) {
        this.tickPosition = tickPosition;
    }

    /**
     * @see #setTickPositions(Number[])
     */
    public Number[] getTickPositions() {
        return tickPositions;
    }

    /**
     * An array defining where the ticks are laid out on the axis. This
     * overrides the default behaviour of
     * <a href="#xAxis.tickPixelInterval">tickPixelInterval</a> and
     * <a href="#xAxis.tickInterval">tickInterval</a>.
     */
    public void setTickPositions(Number[] tickPositions) {
        this.tickPositions = tickPositions;
    }

    /**
     * @see #setTickWidth(Number)
     */
    public Number getTickWidth() {
        return tickWidth;
    }

    /**
     * <p>
     * The pixel width of the major tick marks.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the stroke width is given in the
     * <code>.highcharts-tick</code> class.
     * </p>
     * <p>
     * Defaults to: 1
     */
    public void setTickWidth(Number tickWidth) {
        this.tickWidth = tickWidth;
    }

    /**
     * @see #setTickmarkPlacement(TickmarkPlacement)
     */
    public TickmarkPlacement getTickmarkPlacement() {
        return tickmarkPlacement;
    }

    /**
     * For categorized axes only. If <code>on</code> the tick mark is placed in
     * the center of the category, if <code>between</code> the tick mark is
     * placed between categories. The default is <code>between</code> if the
     * <code>tickInterval</code> is 1, else <code>on</code>.
     * <p>
     * Defaults to: null
     */
    public void setTickmarkPlacement(TickmarkPlacement tickmarkPlacement) {
        this.tickmarkPlacement = tickmarkPlacement;
    }

    /**
     * @see #setTitle(AxisTitle)
     */
    public AxisTitle getTitle() {
        if (title == null) {
            title = new AxisTitle();
        }
        return title;
    }

    /**
     * The axis title, showing next to the axis line.
     */
    public void setTitle(AxisTitle title) {
        this.title = title;
    }

    /**
     * @see #setType(AxisType)
     */
    public AxisType getType() {
        return type;
    }

    /**
     * The type of axis. Can be one of <code>linear</code>,
     * <code>logarithmic</code>, <code>datetime</code> or <code>category</code>.
     * In a datetime axis, the numbers are given in milliseconds, and tick marks
     * are placed on appropriate values like full hours or days. In a category
     * axis, the <a href="#series<line>.data.name">point names</a> of the
     * chart's series are used for categories, if not a
     * <a href="#xAxis.categories">categories</a> array is defined.
     * <p>
     * Defaults to: linear
     */
    public void setType(AxisType type) {
        this.type = type;
    }

    /**
     * @see #setUniqueNames(Boolean)
     */
    public Boolean getUniqueNames() {
        return uniqueNames;
    }

    /**
     * Applies only when the axis <code>type</code> is <code>category</code>.
     * When <code>uniqueNames</code> is true, points are placed on the X axis
     * according to their names. If the same point name is repeated in the same
     * or another series, the point is placed on the same X position as other
     * points of the same name. When <code>uniqueNames</code> is false, the
     * points are laid out in increasing X positions regardless of their names,
     * and the X axis category will take the name of the last point in each
     * position.
     * <p>
     * Defaults to: true
     */
    public void setUniqueNames(Boolean uniqueNames) {
        this.uniqueNames = uniqueNames;
    }

    /**
     * @see #setUnits(TimeUnitMultiples...)
     */
    public TimeUnitMultiples[] getUnits() {
        if (units == null) {
            return new TimeUnitMultiples[] {};
        }
        TimeUnitMultiples[] arr = new TimeUnitMultiples[units.size()];
        units.toArray(arr);
        return arr;
    }

    /**
     * Datetime axis only. An array determining what time intervals the ticks
     * are allowed to fall on. Each array item is an array where the first value
     * is the time unit and the second value another array of allowed multiples.
     * Defaults to:
     *
     * <pre>
     * units: [[
     * 		'millisecond', // unit name
     * 		[1, 2, 5, 10, 20, 25, 50, 100, 200, 500] // allowed multiples
     * 	], [
     * 		'second',
     * 		[1, 2, 5, 10, 15, 30]
     * 	], [
     * 		'minute',
     * 		[1, 2, 5, 10, 15, 30]
     * 	], [
     * 		'hour',
     * 		[1, 2, 3, 4, 6, 8, 12]
     * 	], [
     * 		'day',
     * 		[1]
     * 	], [
     * 		'week',
     * 		[1]
     * 	], [
     * 		'month',
     * 		[1, 3, 6]
     * 	], [
     * 		'year',
     * 		null
     * 	]]
     * </pre>
     */
    public void setUnits(TimeUnitMultiples... units) {
        this.units = new ArrayList<TimeUnitMultiples>(Arrays.asList(units));
    }

    /**
     * Adds unit to the units array
     *
     * @param unit
     *            to add
     * @see #setUnits(TimeUnitMultiples...)
     */
    public void addUnit(TimeUnitMultiples unit) {
        if (this.units == null) {
            this.units = new ArrayList<TimeUnitMultiples>();
        }
        this.units.add(unit);
    }

    /**
     * Removes first occurrence of unit in units array
     *
     * @param unit
     *            to remove
     * @see #setUnits(TimeUnitMultiples...)
     */
    public void removeUnit(TimeUnitMultiples unit) {
        this.units.remove(unit);
    }

    /**
     * @see #setVisible(Boolean)
     */
    public Boolean getVisible() {
        return visible;
    }

    /**
     * Whether axis, including axis title, line, ticks and labels, should be
     * visible.
     * <p>
     * Defaults to: true
     */
    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public void setTitle(String title) {
        AxisTitle t = new AxisTitle();
        t.setText(title);
        this.setTitle(t);
    }

    public void setLinkedTo(ZAxis axis) {
        linkedTo = axis.getAxisIndex();
    }
}
