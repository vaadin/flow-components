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

import java.util.ArrayList;
import java.util.Arrays;

import com.vaadin.flow.component.charts.model.style.Color;

/**
 * <p>
 * The heatmap series type. This series type is available both in Highcharts and
 * Highmaps.
 * </p>
 *
 * <p>
 * The colors of each heat map point is usually determined by its value and
 * controlled by settings on the <a href="#colorAxis">colorAxis</a>.
 * </p>
 */
public class PlotOptionsHeatmap extends AbstractPlotOptions {

    private Boolean allowPointSelect;
    private Boolean animation;
    private Number animationLimit;
    private Color borderColor;
    private Number borderRadius;
    private Number borderWidth;
    private String className;
    private Boolean clip;
    private Color color;
    private Boolean colorByPoint;
    private Number colorIndex;
    private String colorKey;
    private ArrayList<Color> colors;
    private Number colsize;
    private Boolean crisp;
    private Number cropThreshold;
    private Cursor cursor;
    private DataLabels dataLabels;
    private String description;
    private Boolean enableMouseTracking;
    private Boolean exposeElementToA11y;
    private Dimension findNearestPointBy;
    private Boolean getExtremesFromAll;
    private ArrayList<String> keys;
    private String linkedTo;
    private Number maxPointWidth;
    private Number opacity;
    private String _fn_pointDescriptionFormatter;
    private Number rowsize;
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

    public PlotOptionsHeatmap() {
    }

    @Override
    public ChartType getChartType() {
        return ChartType.HEATMAP;
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
     * @see #setClip(Boolean)
     */
    public Boolean getClip() {
        return clip;
    }

    /**
     * Disable this option to allow series rendering in the whole plotting area.
     * Note that clipping should be always enabled when chart.zoomType is set.
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
     * The main color of the series. In heat maps this color is rarely used, as
     * we mostly use the color to denote the value of each point. Unless options
     * are set in the <a href="#colorAxis">colorAxis</a>, the default value is
     * pulled from the <a href="#colors">options.colors</a> array.
     * <p>
     * Defaults to: null
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
     * Determines what data value should be used to calculate point color if
     * <code>colorAxis</code> is used. Requires to set <code>min</code> and
     * <code>max</code> if some custom point property is used or if
     * approximation for data grouping is set to <code>'sum'</code>.
     * <p>
     * Defaults to <code>value</code>.
     */
    public void setColorKey(String colorKey) {
        this.colorKey = colorKey;
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
     * @see #setColsize(Number)
     */
    public Number getColsize() {
        return colsize;
    }

    /**
     * The column size - how many X axis units each column in the heatmap should
     * span.
     * <p>
     * Defaults to: 1
     */
    public void setColsize(Number colsize) {
        this.colsize = colsize;
    }

    /**
     * @see #setCrisp(Boolean)
     */
    public Boolean getCrisp() {
        return crisp;
    }

    /**
     * When true, each column edge is rounded to its nearest pixel in order to
     * render sharp on screen. In some cases, when there are a lot of densely
     * packed columns, this leads to visible difference in column widths or
     * distance between columns. In these cases, setting <code>crisp</code> to
     * <code>false</code> may look better, even though each column is rendered
     * blurry.
     * <p>
     * Defaults to: true
     */
    public void setCrisp(Boolean crisp) {
        this.crisp = crisp;
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
     * @see #setRowsize(Number)
     */
    public Number getRowsize() {
        return rowsize;
    }

    /**
     * The row size - how many Y axis units each heatmap row should span.
     * <p>
     * Defaults to: 1
     */
    public void setRowsize(Number rowsize) {
        this.rowsize = rowsize;
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
}
