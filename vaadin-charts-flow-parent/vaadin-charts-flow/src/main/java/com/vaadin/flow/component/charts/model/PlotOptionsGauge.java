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
import java.util.ArrayList;
import java.util.Arrays;

import com.vaadin.flow.component.charts.model.style.Color;

/**
 * General plotting options for the gauge series type. Requires
 * <code>highcharts-more.js</code>
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class PlotOptionsGauge extends GaugeOptions {

	private Boolean animation;
	private Number animationLimit;
	private String className;
	private Boolean clip;
	private Color color;
	private Number colorIndex;
	private Boolean crisp;
	private Cursor cursor;
	private DataLabels dataLabels;
	private String description;
	private Dial dial;
	private Boolean enableMouseTracking;
	private Boolean exposeElementToA11y;
	private Dimension findNearestPointBy;
	private Boolean getExtremesFromAll;
	private ArrayList<String> keys;
	private String linkedTo;
	private Color negativeColor;
	private Number opacity;
	private Number overshoot;
	private Pivot pivot;
	private String _fn_pointDescriptionFormatter;
	private Boolean selected;
	private Boolean showCheckbox;
	private Boolean showInLegend;
	private Boolean skipKeyboardNavigation;
	private Boolean stickyTracking;
	private Number threshold;
	private SeriesTooltip tooltip;
	private Boolean visible;
	private Boolean wrap;

	public PlotOptionsGauge() {
	}

	@Override
	public ChartType getChartType() {
		return ChartType.GAUGE;
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
	 * @see #setCrisp(Boolean)
	 */
	public Boolean getCrisp() {
		return crisp;
	}

	/**
	 * When true, each point or column edge is rounded to its nearest pixel
	 * in order to render sharp on screen. In some cases, when there are a lot of
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
	 * @see #setDataLabels(DataLabels)
	 */
	public DataLabels getDataLabels() {
		if (dataLabels == null) {
			dataLabels = new DataLabels();
		}
		return dataLabels;
	}

	/**
	 * Data labels for the gauge. For gauges, the data labels are enabled by
	 * default and shown in a bordered box below the point.
	 */
	public void setDataLabels(DataLabels dataLabels) {
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
	 * @see #setDial(Dial)
	 */
	public Dial getDial() {
		if (dial == null) {
			dial = new Dial();
		}
		return dial;
	}

	/**
	 * <p>
	 * Options for the dial or arrow pointer of the gauge.
	 * </p>
	 * 
	 * <p>
	 * In <a href=
	 * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
	 * >styled mode</a>, the dial is styled with the
	 * <code>.highcharts-gauge-series .highcharts-dial</code> rule.
	 * </p>
	 */
	public void setDial(Dial dial) {
		this.dial = dial;
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
	 * The color for the parts of the graph or points that are below the <a
	 * href="#plotOptions.series.threshold">threshold</a>.
	 * <p>
	 * Defaults to: null
	 */
	public void setNegativeColor(Color negativeColor) {
		this.negativeColor = negativeColor;
	}

	/**
	 * @see #setOvershoot(Number)
	 */
	public Number getOvershoot() {
		return overshoot;
	}

	/**
	 * Allow the dial to overshoot the end of the perimeter axis by this many
	 * degrees. Say if the gauge axis goes from 0 to 60, a value of 100, or
	 * 1000, will show 5 degrees beyond the end of the axis.
	 * <p>
	 * Defaults to: 0
	 */
	public void setOvershoot(Number overshoot) {
		this.overshoot = overshoot;
	}

	/**
	 * @see #setPivot(Pivot)
	 */
	public Pivot getPivot() {
		if (pivot == null) {
			pivot = new Pivot();
		}
		return pivot;
	}

	/**
	 * <p>
	 * Options for the pivot or the center point of the gauge.
	 * </p>
	 * 
	 * <p>
	 * In <a href=
	 * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
	 * >styled mode</a>, the pivot is styled with the
	 * <code>.highcharts-gauge-series .highcharts-pivot</code> rule.
	 * </p>
	 */
	public void setPivot(Pivot pivot) {
		this.pivot = pivot;
	}

	public String getPointDescriptionFormatter() {
		return _fn_pointDescriptionFormatter;
	}

	public void setPointDescriptionFormatter(
			String _fn_pointDescriptionFormatter) {
		this._fn_pointDescriptionFormatter = _fn_pointDescriptionFormatter;
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
	 * Defaults to false for gauge series.
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
	 * @see #setWrap(Boolean)
	 */
	public Boolean getWrap() {
		return wrap;
	}

	/**
	 * When this option is <code>true</code>, the dial will wrap around the
	 * axes. For instance, in a full-range gauge going from 0 to 360, a value of
	 * 400 will point to 40. When <code>wrap</code> is <code>false</code>, the
	 * dial stops at 360.
	 * <p>
	 * Defaults to: true
	 */
	public void setWrap(Boolean wrap) {
		this.wrap = wrap;
	}
}
