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
 * The size of the point shape is determined by its value relative to its
 * siblings values. Requires the module <code>heatmap.js</code> as well, if
 * functionality such as the
 * <a href="http://api.highcharts.com/highmaps#colorAxis">colorAxis</a> is to be
 * used.
 */
public class PlotOptionsTreemap extends AbstractPlotOptions {

    private Boolean allowDrillToNode;
    private Boolean allowPointSelect;
    private Boolean alternateStartingDirection;
    private Boolean animation;
    private Number animationLimit;
    private Color borderColor;
    private Number borderWidth;
    private String className;
    private Color color;
    private Boolean colorByPoint;
    private Number colorIndex;
    private ArrayList<Color> colors;
    private Boolean crisp;
    private Number cropThreshold;
    private Cursor cursor;
    private DataLabels dataLabels;
    private String description;
    private Boolean enableMouseTracking;
    private Boolean exposeElementToA11y;
    private Dimension findNearestPointBy;
    private Boolean getExtremesFromAll;
    private Boolean ignoreHiddenPoint;
    private Boolean interactByLeaf;
    private ArrayList<String> keys;
    private TreeMapLayoutAlgorithm layoutAlgorithm;
    private TreeMapLayoutStartingDirection layoutStartingDirection;
    private Boolean levelIsConstant;
    private ArrayList<Level> levels;
    private String linkedTo;
    private Number maxPointWidth;
    private Number opacity;
    private String _fn_pointDescriptionFormatter;
    private Boolean selected;
    private Boolean shadow;
    private Boolean showCheckbox;
    private Boolean showInLegend;
    private Boolean skipKeyboardNavigation;
    private Number sortIndex;
    private States states;
    private Boolean stickyTracking;
    private SeriesTooltip tooltip;
    private Number turboThreshold;
    private Boolean visible;
    private ZoneAxis zoneAxis;
    private ArrayList<Zones> zones;

    public PlotOptionsTreemap() {
    }

    @Override
    public ChartType getChartType() {
        return ChartType.TREEMAP;
    }

    /**
     * @see #setAllowDrillToNode(Boolean)
     */
    public Boolean getAllowDrillToNode() {
        return allowDrillToNode;
    }

    /**
     * When enabled the user can click on a point which is a parent and zoom in
     * on its children.
     * <p>
     * Defaults to: false
     */
    public void setAllowDrillToNode(Boolean allowDrillToNode) {
        this.allowDrillToNode = allowDrillToNode;
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
     * @see #setAlternateStartingDirection(Boolean)
     */
    public Boolean getAlternateStartingDirection() {
        return alternateStartingDirection;
    }

    /**
     * Enabling this option will make the treemap alternate the drawing
     * direction between vertical and horizontal. The next levels starting
     * direction will always be the opposite of the previous.
     * <p>
     * Defaults to: false
     */
    public void setAlternateStartingDirection(
            Boolean alternateStartingDirection) {
        this.alternateStartingDirection = alternateStartingDirection;
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
     * The color of the border surrounding each tree map item.
     * <p>
     * Defaults to: #e6e6e6
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
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
     * performance on large series.
     * <p>
     * Defaults to: 300
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
     * @see #setIgnoreHiddenPoint(Boolean)
     */
    public Boolean getIgnoreHiddenPoint() {
        return ignoreHiddenPoint;
    }

    /**
     * Whether to ignore hidden points when the layout algorithm runs. If
     * <code>false</code>, hidden points will leave open spaces.
     * <p>
     * Defaults to: true
     */
    public void setIgnoreHiddenPoint(Boolean ignoreHiddenPoint) {
        this.ignoreHiddenPoint = ignoreHiddenPoint;
    }

    /**
     * @see #setInteractByLeaf(Boolean)
     */
    public Boolean getInteractByLeaf() {
        return interactByLeaf;
    }

    /**
     * This option decides if the user can interact with the parent nodes or
     * just the leaf nodes. When this option is undefined, it will be true by
     * default. However when allowDrillToNode is true, then it will be false by
     * default.
     */
    public void setInteractByLeaf(Boolean interactByLeaf) {
        this.interactByLeaf = interactByLeaf;
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
     * @see #setLayoutAlgorithm(TreeMapLayoutAlgorithm)
     */
    public TreeMapLayoutAlgorithm getLayoutAlgorithm() {
        return layoutAlgorithm;
    }

    /**
     * This option decides which algorithm is used for setting position and
     * dimensions of the points. Can be one of <code>sliceAndDice</code>,
     * <code>stripes</code>, <code>squarified</code> or <code>strip</code>.
     * <p>
     * Defaults to: sliceAndDice
     */
    public void setLayoutAlgorithm(TreeMapLayoutAlgorithm layoutAlgorithm) {
        this.layoutAlgorithm = layoutAlgorithm;
    }

    /**
     * @see #setLayoutStartingDirection(TreeMapLayoutStartingDirection)
     */
    public TreeMapLayoutStartingDirection getLayoutStartingDirection() {
        return layoutStartingDirection;
    }

    /**
     * Defines which direction the layout algorithm will start drawing. Possible
     * values are "vertical" and "horizontal".
     * <p>
     * Defaults to: vertical
     */
    public void setLayoutStartingDirection(
            TreeMapLayoutStartingDirection layoutStartingDirection) {
        this.layoutStartingDirection = layoutStartingDirection;
    }

    /**
     * @see #setLevelIsConstant(Boolean)
     */
    public Boolean getLevelIsConstant() {
        return levelIsConstant;
    }

    /**
     * Used together with the levels and allowDrillToNode options. When set to
     * false the first level visible when drilling is considered to be level
     * one. Otherwise the level will be the same as the tree structure.
     * <p>
     * Defaults to: true
     */
    public void setLevelIsConstant(Boolean levelIsConstant) {
        this.levelIsConstant = levelIsConstant;
    }

    /**
     * @see #setLevels(Level...)
     */
    public Level[] getLevels() {
        if (levels == null) {
            return new Level[] {};
        }
        Level[] arr = new Level[levels.size()];
        levels.toArray(arr);
        return arr;
    }

    /**
     * Set options on specific levels. Takes precedence over series options, but
     * not point options.
     */
    public void setLevels(Level... levels) {
        this.levels = new ArrayList<Level>(Arrays.asList(levels));
    }

    /**
     * Adds level to the levels array
     *
     * @param level
     *            to add
     * @see #setLevels(Level...)
     */
    public void addLevel(Level level) {
        if (this.levels == null) {
            this.levels = new ArrayList<Level>();
        }
        this.levels.add(level);
    }

    /**
     * Removes first occurrence of level in levels array
     *
     * @param level
     *            to remove
     * @see #setLevels(Level...)
     */
    public void removeLevel(Level level) {
        this.levels.remove(level);
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
     * The opacity of a point in treemap. When a point has children, the
     * visibility of the children is determined by the opacity.
     * <p>
     * Defaults to: 0.15
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
     * Whether to display this series type or specific series item in the
     * legend.
     * <p>
     * Defaults to: false
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
     * @see #setSortIndex(Number)
     */
    public Number getSortIndex() {
        return sortIndex;
    }

    /**
     * The sort index of the point inside the treemap level.
     */
    public void setSortIndex(Number sortIndex) {
        this.sortIndex = sortIndex;
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
