package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2020 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 * 
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import javax.annotation.Generated;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.vaadin.flow.component.charts.model.style.Color;
import com.vaadin.flow.component.charts.util.Util;

/**
 * 
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class PlotOptionsOhlc extends OhlcOptions {

	private Boolean allowPointSelect;
	private Boolean animation;
	private Number animationLimit;
	private String className;
	private Boolean clip;
	private Color color;
	private Boolean colorByPoint;
	private Number colorIndex;
	private String colorKey;
	private ArrayList<Color> colors;
	private Compare compare;
	private Number compareBase;
	private Number cropThreshold;
	private Cursor cursor;
	private DataGrouping dataGrouping;
	private String description;
	private Boolean enableMouseTracking;
	private Boolean exposeElementToA11y;
	private Dimension findNearestPointBy;
	private String gapUnit;
	private Boolean getExtremesFromAll;
	private Number groupPadding;
	private Boolean grouping;
	private ArrayList<String> keys;
	private Number legendIndex;
	private Number lineWidth;
	private String linkedTo;
	private Number maxPointWidth;
	private Number minPointLength;
	private PlotOptionsSeries navigatorOptions;
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
	private Boolean shadow;
	private Boolean showCheckbox;
	private Boolean showInLegend;
	private Boolean showInNavigator;
	private Boolean skipKeyboardNavigation;
	private Boolean softThreshold;
	private States states;
	private Boolean stickyTracking;
	private Number threshold;
	private SeriesTooltip tooltip;
	private Number turboThreshold;
	private Color upColor;
	private Boolean visible;
	private ZoneAxis zoneAxis;
	private ArrayList<Zones> zones;

	public PlotOptionsOhlc() {
	}

	@Override
	public ChartType getChartType() {
		return ChartType.OHLC;
	}

	/**
	 * @see #setAllowPointSelect(Boolean)
	 */
	public Boolean getAllowPointSelect() {
		return allowPointSelect;
	}

	/**
	 * Allow this series' points to be selected by clicking on the markers or
	 * bars.
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
	 * The main color of the series. In line type series it applies to the line
	 * and the point markers unless otherwise specified. In bar type series it
	 * applies to the bars unless a color is specified per point. The default
	 * value is pulled from the <code>options.colors</code> array.
	 * </p>
	 * 
	 * <p>
	 * In <a href=
	 * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
	 * >styled mode</a>, the color can be defined by the <a
	 * href="#plotOptions.series.colorIndex">colorIndex</a> option. Also, the
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
	 * Determines what data value should be used to calculate point color
   * if <code>colorAxis</code> is used.
	 * Requires to set <code>min</code> and <code>max</code> if some custom point
   * property is used or if approximation for data grouping
   * is set to <code>'sum'</code>.
	 * <p>
	 * Defaults to <code>close</code>.
	 */
	public void setColorKey(String colorKey) {
		this.colorKey = colorKey;
	}

	/**
	 * @see #setColors(Color...)
	 */
	public Color[] getColors() {
		if (colors == null) {
			return new Color[]{};
		}
		Color[] arr = new Color[colors.size()];
		colors.toArray(arr);
		return arr;
	}

	/**
	 * A series specific or series type specific color set to apply instead of
	 * the global <a href="#colors">colors</a> when <a
	 * href="#plotOptions.column.colorByPoint">colorByPoint</a> is true.
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
	 * @see #setCropThreshold(Number)
	 */
	public Number getCropThreshold() {
		return cropThreshold;
	}

	/**
	 * When the series contains less points than the crop threshold, all points
	 * are drawn, event if the points fall outside the visible plot area at the
	 * current zoom. The advantage of drawing all points (including markers and
	 * columns), is that animation is performed on updates. On the other hand,
	 * when the series contains more points than the crop threshold, the series
	 * data is cropped to only contain points that fall within the plot area.
	 * The advantage of cropping away invisible points is to increase
	 * performance on large series. .
	 * <p>
	 * Defaults to: 50
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
	 * @see #setDataGrouping(DataGrouping)
	 */
	public DataGrouping getDataGrouping() {
		if (dataGrouping == null) {
			dataGrouping = new DataGrouping();
		}
		return dataGrouping;
	}

	public void setDataGrouping(DataGrouping dataGrouping) {
		this.dataGrouping = dataGrouping;
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
	 * point tooltips and click events on graphs and points. When using shared
	 * tooltips (default in stock charts), mouse tracking is not required. For
	 * large datasets it improves performance.
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
	 * @see #setGroupPadding(Number)
	 */
	public Number getGroupPadding() {
		return groupPadding;
	}

	/**
	 * Padding between each value groups, in x axis units.
	 * <p>
	 * Defaults to: 0.2
	 */
	public void setGroupPadding(Number groupPadding) {
		this.groupPadding = groupPadding;
	}

	/**
	 * @see #setGrouping(Boolean)
	 */
	public Boolean getGrouping() {
		return grouping;
	}

	/**
	 * Whether to group non-stacked columns or to let them render independent of
	 * each other. Non-grouped columns will be laid out individually and overlap
	 * each other.
	 * <p>
	 * Defaults to: true
	 */
	public void setGrouping(Boolean grouping) {
		this.grouping = grouping;
	}

	/**
	 * @see #setKeys(String...)
	 */
	public String[] getKeys() {
		if (keys == null) {
			return new String[]{};
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
	 * @see #setLineWidth(Number)
	 */
	public Number getLineWidth() {
		return lineWidth;
	}

	/**
	 * The pixel width of the line/border. Defaults to <code>1</code>.
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
	 * The <a href="#series.id">id</a> of another series to link to.
	 * Additionally, the value can be ":previous" to link to the previous
	 * series. When two series are linked, only the first one appears in the
	 * legend. Toggling the visibility of this also toggles the linked series.
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
	 * @see #setMinPointLength(Number)
	 */
	public Number getMinPointLength() {
		return minPointLength;
	}

	/**
	 * The minimal height for a column or width for a bar. By default, 0 values
	 * are not shown. To visualize a 0 (or close to zero) point, set the minimal
	 * point length to a pixel value like 3. In stacked column charts,
	 * minPointLength might not be respected for tightly packed values.
	 * <p>
	 * Defaults to: 0
	 */
	public void setMinPointLength(Number minPointLength) {
		this.minPointLength = minPointLength;
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
	 * Available options are the same as any series, documented at <a
	 * class="internal" href="#plotOptions.series">plotOptions</a> and <a
	 * class="internal" href="#series">series</a>.
	 * </p>
	 * 
	 * <p>
	 * These options are merged with options in <a
	 * href="#navigator.series">navigator.series</a>, and will take precedence
	 * if the same option is defined both places.
	 * </p>
	 * <p>
	 * Defaults to: undefined
	 */
	public void setNavigatorOptions(PlotOptionsSeries navigatorOptions) {
		this.navigatorOptions = navigatorOptions;
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

	/**
	 * @see #setNegativeColor(Color)
	 */
	public Color getNegativeColor() {
		return negativeColor;
	}

	/**
	 * The color for the parts of the graph or points that are below the <a
	 * href="#plotOptions.series.threshold">threshold</a>.
	 * <p>
	 * Defaults to: null
	 */
	public void setNegativeColor(Color negativeColor) {
		this.negativeColor = negativeColor;
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
	 * defines the interval of the x values in milliseconds. For example, if a
	 * series contains one value each day, set pointInterval to
	 * <code>24 * 3600 * 1000</code>.
	 * </p>
	 * <p>
	 * Since Highstock 2.1, it can be combined with
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
	 * On datetime series, this allows for setting the <a
	 * href="#plotOptions.series.pointInterval">pointInterval</a> to irregular
	 * time units, <code>day</code>, <code>month</code> and <code>year</code>. A
	 * day is usually the same as 24 hours, but pointIntervalUnit also takes the
	 * DST crossover into consideration when dealing with local time. Combine
	 * this option with <code>pointInterval</code> to draw weeks, quarters, 6
	 * months, 10 years etc.
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
	 * Note that pointPlacement needs a <a
	 * href="#plotOptions.series.pointRange">pointRange</a> to work. For column
	 * series this is computed, but for line-type series it needs to be set.
	 * </p>
	 * <p>
	 * Defaults to <code>null</code> in cartesian charts, <code>"between"</code>
	 * in polar charts.
	 * <p>
	 * Defaults to: null
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
	 * The width of each point on the x axis. For example in a column chart with
	 * one value each day, the pointRange would be 1 day (= 24 * 3600 * 1000
	 * milliseconds). This is normally computed automatically, but this option
	 * can be used to override the automatic value. In a series on a categorized
	 * axis the pointRange is 1 by default.
	 * <p>
	 * Defaults to: null
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
	 * on what value to start. On a datetime X axis, the number will be given as
	 * milliseconds since 1970-01-01, for example
	 * <code>Date.UTC(2011, 0, 1)</code>.
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
	 * <p>
	 * Defaults to: null
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
	 * true, the checkbox next to the series name in the legend will be checked
	 * for a selected series.
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
	 * Whether to apply a drop shadow to the graph line. Since 1.1.7 the shadow
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
	 * series' graph or markers. This also implies the tooltip when not shared.
	 * When <code>stickyTracking</code> is false, the tooltip will be hidden
	 * when moving the mouse between series. Defaults to true for line and area
	 * type series, but to false for columns, candlesticks etc.
	 * <p>
	 * Defaults to: true
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
	 * this is only used in conjunction with <a
	 * href="#plotOptions.series.negativeColor">negativeColor</a>.
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
	 * @see #setUpColor(Color)
	 */
	public Color getUpColor() {
		return upColor;
	}

	/**
	 * Line color for up points.
	 */
	public void setUpColor(Color upColor) {
		this.upColor = upColor;
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
			return new Zones[]{};
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
