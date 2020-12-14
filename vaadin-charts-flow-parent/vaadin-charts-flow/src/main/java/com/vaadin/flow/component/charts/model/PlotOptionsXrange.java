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

import java.util.ArrayList;
import java.util.Arrays;

import com.vaadin.flow.component.charts.model.style.Color;

/**
 * Options for the xrange series type.
 */
public class PlotOptionsXrange extends AbstractPlotOptions {

    private Boolean allowPointSelect;
    private Boolean animation;
    private Number animationLimit;
    private Color borderColor;
    private Number borderRadius;
    private Number borderWidth;
    private Boolean centerInCategory;
    private String className;
    private Boolean clip;
    private Color color;
    private Boolean colorByPoint;
    private Number colorIndex;
    private String colorKey;
    private ArrayList<Color> colors;
    private Boolean connectEnds;
    private Boolean connectNulls;
    private Cursor cursor;
    private DashStyle dashStyle;
    private DataLabels dataLabels;
    private String description;
    private Boolean enableMouseTracking;
    private Boolean grouping;
    private Number groupPadding;
    private ArrayList<String> keys;
    private String linkedTo;
    private Number maxPointWidth;
    private Number minPointLength;
    private Number opacity;
    private PartialFill partialFill;
    private String _fn_pointDescriptionFormatter;
    private Number pointPadding;
    private Number pointWidth;
    private Boolean selected;
    private Boolean shadow;
    private Boolean showCheckbox;
    private Boolean showInLegend;
    private Boolean skipKeyboardNavigation;
    private States states;
    private Boolean stickyTracking;
    private SeriesTooltip tooltip;
    private Number turboThreshold;
    private Boolean visible;
    private ZoneAxis zoneAxis;
    private ArrayList<Zones> zones;

    public PlotOptionsXrange() {
    }

    @Override
    public ChartType getChartType() {
        return ChartType.XRANGE;
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
     * @see #setBorderColor(Color)
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * <p>
     * The color of the border surrounding each column or bar.
     * </p>
     * 
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the border stroke can be set with the
     * <code>.highcharts-point</code> rule.
     * </p>
     * <p>
     * Defaults to: #ffffff
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * @see #setBorderRadius(Number)
     */
    public Number getBorderRadius() {
        return borderRadius;
    }

    /**
     * The corner radius of the border surrounding each column or bar.
     * <p>
     * Defaults to: 0
     */
    public void setBorderRadius(Number borderRadius) {
        this.borderRadius = borderRadius;
    }

    /**
     * @see #setBorderWidth(Number)
     */
    public Number getBorderWidth() {
        return borderWidth;
    }

    /**
     * <p>
     * The width of the border surrounding each column or bar.
     * </p>
     * 
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the stroke width can be set with the
     * <code>.highcharts-point</code> rule.
     * </p>
     * <p>
     * Defaults to: 1
     */
    public void setBorderWidth(Number borderWidth) {
        this.borderWidth = borderWidth;
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
     * @see #setColors(Color...)
     */
    public Color[] getColors() {
        if (colors == null) {
            return new Color[] {};
        }
        Color[] arr = new Color[colors.size()];
        colors.toArray(arr);
        return arr;
    }

    /**
     * A series specific or series type specific color set to apply instead of
     * the global <a href="#colors">colors</a> when
     * <a href="#plotOptions.column.colorByPoint">colorByPoint</a> is true.
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

    public String getPointDescriptionFormatter() {
        return _fn_pointDescriptionFormatter;
    }

    public void setPointDescriptionFormatter(
            String _fn_pointDescriptionFormatter) {
        this._fn_pointDescriptionFormatter = _fn_pointDescriptionFormatter;
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
     * @see #setPointWidth(Number)
     */
    public Number getPointWidth() {
        return pointWidth;
    }

    /**
     * A pixel value specifying a fixed width for each column or bar. When
     * <code>null</code>, the width is calculated from the
     * <code>pointPadding</code> and <code>groupPadding</code>.
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
            return new Zones[] {};
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
     * @see #setCenterInCategory(Boolean)
     */
    public Boolean getCenterInCategory() {
        return centerInCategory;
    }

    /**
     * When true, the columns will center in the category, ignoring null or
     * missing points. When false, space will be reserved for null or missing
     * points.
     * <p>
     * Defaults to false.
     */
    public void setCenterInCategory(Boolean centerInCategory) {
        this.centerInCategory = centerInCategory;
    }

    /**
     * @see #setClip(Boolean)
     */
    public Boolean getClip() {
        return clip;
    }

    /**
     * Disable this option to allow series rendering in the whole plotting area.
     * <p>
     * Note: Clipping should be always enabled when zoomType is set
     */
    public void setClip(Boolean clip) {
        this.clip = clip;
    }

    /**
     * @see #setColorKey(String)
     */
    public String getColorKey() {
        return colorKey;
    }

    /**
     * Determines what data value should be used to calculate point color if
     * colorAxis is used. Requires to set min and max if some custom point
     * property is used or if approximation for data grouping is set to 'sum'.
     * <p>
     * Defaults to y.
     */
    public void setColorKey(String colorKey) {
        this.colorKey = colorKey;
    }

    /**
     * @see #setConnectEnds(Boolean)
     */
    public Boolean getConnectEnds() {
        return connectEnds;
    }

    /**
     * Polar charts only. Whether to connect the ends of a line series plot
     * across the extremes.
     * <p>
     * Defaults to: true
     */
    public void setConnectEnds(Boolean connectEnds) {
        this.connectEnds = connectEnds;
    }

    /**
     * @see #setConnectNulls(Boolean)
     */
    public Boolean getConnectNulls() {
        return connectNulls;
    }

    public void setConnectNulls(Boolean connectNulls) {
        this.connectNulls = connectNulls;
    }

    /**
     * @see #setDashStyle(DashStyle)
     */
    public DashStyle getDashStyle() {
        return dashStyle;
    }

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
     * <p>
     * Defaults to: Solid
     */
    public void setDashStyle(DashStyle dashStyle) {
        this.dashStyle = dashStyle;
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
     * Defaults to 1.
     */
    public void setOpacity(Number opacity) {
        this.opacity = opacity;
    }

    /**
     * @see #setPartialFill(PartialFill)
     */
    public PartialFill getPartialFill() {
        if (partialFill == null) {
            partialFill = new PartialFill();
        }
        return partialFill;
    }

    /**
     * Partial fill configuration for series points, typically used to visualize
     * how much of a task is performed.
     */
    public void setPartialFill(PartialFill partialFill) {
        this.partialFill = partialFill;
    }
}
