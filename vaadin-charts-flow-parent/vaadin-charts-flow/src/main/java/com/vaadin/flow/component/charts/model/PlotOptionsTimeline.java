package com.vaadin.flow.component.charts.model;

import java.util.ArrayList;
import java.util.Arrays;

import com.vaadin.flow.component.charts.model.style.Color;

/**
 * Plot options for {@link ChartType#TIMELINE} charts.
 */
public class PlotOptionsTimeline extends AbstractPlotOptions {

    private Boolean allowPointSelect;
    private Boolean animation;
    private String className;
    private Boolean clip;
    private Color color;
    private Boolean colorByPoint;
    private Number colorIndex;
    private Cursor cursor;
    private Boolean crisp;
    private DataLabels dataLabels;
    private String description;
    private Boolean enableMouseTracking;
    private Boolean exposeElementToA11y;
    private Boolean ignoreHiddenPoint;
    private ArrayList<String> keys;
    private String legendType;
    private String linecap;
    private String linkedTo;
    private Marker marker;
    private Number opacity;
    private String _fn_pointDescriptionFormatter;
    private Boolean selected;
    private Boolean shadow;
    private Boolean showCheckbox;
    private Boolean showInLegend;
    private Boolean skipKeyboardNavigation;
    private States states;
    private Boolean stickyTracking;
    private SeriesTooltip tooltip;
    private Boolean visible;
    private Number gapSize;
    private String gapUnit;
    private Number legendIndex;
    private PlotOptionsSeries navigatorOptions;
    private Number pointRange;
    private Boolean showInNavigator;

    public PlotOptionsTimeline() {
    }

    @Override
    public ChartType getChartType() {
        return ChartType.TIMELINE;
    }

    /**
     * @see #setAllowPointSelect(Boolean)
     */
    public Boolean getAllowPointSelect() {
        return allowPointSelect;
    }

    /**
     * Allow this series' points to be selected by clicking on the markers, bars
     * or pie slices.
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
     * Note: Clipping should be always enabled when chart.zoomType is set
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
     * >styled mode</a>, the color can be defined by the
     * <a href="#plotOptions.series.colorIndex">colorIndex</a> option. Also, the
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
     * Defaults to <code>true</code>
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
     * @see #setCrisp(Boolean)
     */
    public Boolean getCrisp() {
        return crisp;
    }

    /**
     * When true, each point or column edge is rounded to its nearest pixel in
     * order to render sharp on screen. In some cases, when there are a lot of
     * densely packed columns, this leads to visible difference in column widths
     * or distance between columns. In these cases, setting crisp to false may
     * look better, even though each column is rendered blurry.
     * <p>
     * Defaults to <code>true</code>.
     */
    public void setCrisp(Boolean crisp) {
        this.crisp = crisp;
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
     * <p>
     * Options for the series data labels, appearing next to each data point.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the data labels can be styled wtih the
     * <code>.highcharts-data-label-box</code> and
     * <code>.highcharts-data-label</code> class names (<a href=
     * "http://jsfiddle.net/gh/get/library/pure/highcharts/highcharts/tree/master/samples/highcharts/css/series-datalabels"
     * >see example</a>).
     * </p>
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
     * @see #setIgnoreHiddenPoint(Boolean)
     */
    public Boolean getIgnoreHiddenPoint() {
        return ignoreHiddenPoint;
    }

    /**
     * Defaults to <code>true</code>
     */
    public void setIgnoreHiddenPoint(Boolean ignoreHiddenPoint) {
        this.ignoreHiddenPoint = ignoreHiddenPoint;
    }

    /**
     * @see #setKeys(String...)
     */
    public String[] getKeys() {
        if (keys == null) {
            return new String[] {};
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
     * @see #setLegendType(String)
     */
    public String getLegendType() {
        return legendType;
    }

    /**
     * Defaults to <code>point</>.
     */
    public void setLegendType(String legendType) {
        this.legendType = legendType;
    }

    /**
     * @see #setLinecap(String)
     */
    public String getLinecap() {
        return linecap;
    }

    /**
     * The line cap used for line ends and line joins on the graph.
     * <p>
     * Defaults to: round
     */
    public void setLinecap(String linecap) {
        this.linecap = linecap;
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
     * @see #setMarker(Marker)
     */
    public Marker getMarker() {
        if (marker == null) {
            marker = new Marker();
        }
        return marker;
    }

    /**
     * <p>
     * Options for the point markers of line-like series. Properties like
     * <code>fillColor</code>, <code>lineColor</code> and <code>lineWidth</code>
     * define the visual appearance of the markers. Other series types, like
     * column series, don't have markers, but have visual options on the series
     * level instead.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the markers can be styled with the
     * <code>.highcharts-point</code>, <code>.highcharts-point-hover</code> and
     * <code>.highcharts-point-select</code> class names.
     * </p>
     */
    public void setMarker(Marker marker) {
        this.marker = marker;
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
     * @see #setShadow(Boolean)
     */
    public Boolean getShadow() {
        return shadow;
    }

    /**
     * Whether to apply a drop shadow to the graph line. Since 2.3 the shadow
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
     * @see #setGapSize(Number)
     */
    public Number getGapSize() {
        return gapSize;
    }

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
     * <p>
     * Defaults to: 0
     */
    public void setGapSize(Number gapSize) {
        this.gapSize = gapSize;
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
     * @see #setNavigatorOptions(PlotOptionsSeries)
     */
    public PlotOptionsSeries getNavigatorOptions() {
        return navigatorOptions;
    }

    /**
     * <p>
     * Options for the corresponding navigator series if
     * <code>showInNavigator</code> is <code>true</code> for this series.
     * Available options are the same as any series, documented at
     * <a class="internal" href="#plotOptions.series">plotOptions</a> and
     * <a class="internal" href="#series">series</a>.
     * </p>
     *
     * <p>
     * These options are merged with options in
     * <a href="#navigator.series">navigator.series</a>, and will take
     * precedence if the same option is defined both places.
     * </p>
     * <p>
     * Defaults to: undefined
     */
    public void setNavigatorOptions(PlotOptionsSeries navigatorOptions) {
        this.navigatorOptions = navigatorOptions;
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
     * can be used to override the automatic value.
     * <p>
     * Defaults to: 0
     */
    public void setPointRange(Number pointRange) {
        this.pointRange = pointRange;
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

}
