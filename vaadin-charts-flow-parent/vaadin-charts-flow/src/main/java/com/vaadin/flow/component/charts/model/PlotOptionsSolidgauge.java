package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2018 Vaadin Ltd
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

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A gauge showing values using a filled arc with colors indicating the value.
 * The solid gauge plots values against the <code>yAxis</code>, which is
 * extended with some color options, <a href="#yAxis.minColor">minColor</a>, <a
 * href="#yAxis.maxColor">maxColor</a> and <a href="#yAxis.stops">stops</a>, to
 * control the color of the gauge itself.
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class PlotOptionsSolidgauge extends GaugeOptions {

	private Boolean animation;
	private Number animationLimit;
	private String className;
	private Number colorIndex;
	private Cursor cursor;
	private DataLabels dataLabels;
	private String description;
	private Boolean enableMouseTracking;
	private Boolean exposeElementToA11y;
	private Dimension findNearestPointBy;
	private Boolean getExtremesFromAll;
	private ArrayList<String> keys;
	private String linecap;
	private Number overshoot;
	private String _fn_pointDescriptionFormatter;
	private Boolean rounded;
	private Boolean selected;
	private Boolean showCheckbox;
	private Boolean showInLegend;
	private Boolean skipKeyboardNavigation;
	private Boolean stickyTracking;
	private Number threshold;
	private SeriesTooltip tooltip;
	private Boolean visible;
	private Boolean wrap;

	public PlotOptionsSolidgauge() {
	}

	@Override
	public ChartType getChartType() {
		return ChartType.SOLIDGAUGE;
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
	 * @see #setLinecap(String)
	 */
	public String getLinecap() {
		return linecap;
	}

	/**
	 * Whether the strokes of the solid gauge should be <code>round</code> or
	 * <code>square</code>.
	 * <p>
	 * Defaults to: round
	 */
	public void setLinecap(String linecap) {
		this.linecap = linecap;
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

	public String getPointDescriptionFormatter() {
		return _fn_pointDescriptionFormatter;
	}

	public void setPointDescriptionFormatter(
			String _fn_pointDescriptionFormatter) {
		this._fn_pointDescriptionFormatter = _fn_pointDescriptionFormatter;
	}

	/**
	 * @see #setRounded(Boolean)
	 */
	public Boolean getRounded() {
		return rounded;
	}

	/**
	 * Wether to draw rounded edges on the gauge.
	 * <p>
	 * Defaults to: false
	 */
	public void setRounded(Boolean rounded) {
		this.rounded = rounded;
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
	 * The threshold or base level for the gauge.
	 * <p>
	 * Defaults to: null
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
