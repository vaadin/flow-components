package com.vaadin.addon.charts.model;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2014 Vaadin Ltd
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chart's configuration root object containing all the child objects that are
 * used to configure chart, axes, legend, titles etc.
 */
public class Configuration extends AbstractConfigurationObject
        implements ChartConfiguration {

    private ChartModel chart;
    private Title title;
    private Subtitle subtitle;
    private AxisList<XAxis> xAxis;
    private AxisList<YAxis> yAxis;
    private AxisList<ZAxis> zAxis;
    private AxisList<ColorAxis> colorAxis;
    private Tooltip tooltip;
    private Legend legend;
    private Credits credits;
    private Map<String, AbstractPlotOptions> plotOptions = new HashMap<>();
    private HTMLLabels labels;

    private List<Series> series = new ArrayList<>();
    private Drilldown drilldown;

    private PaneList pane;
    private Exporting exporting = new Exporting(false);
    private RangeSelector rangeSelector;
    private Scrollbar scrollbar;
    private Loading loading;
    private Navigation navigation;
    private NoData noData;
    private Navigator navigator;

    // TODO Enable it again
    // @JsonIgnore
    // private final List<ConfigurationChangeListener> changeListeners = new
    // ArrayList<ConfigurationChangeListener>();

    /**
     * @see #setChart(ChartModel)
     */
    public ChartModel getChart() {
        if (chart == null) {
            setChart(new ChartModel());
        }
        return chart;
    }

    /**
     * Sets options regarding the chart and plot areas as well as general chart
     * options.
     * 
     * @param chart
     */
    public void setChart(ChartModel chart) {
        this.chart = chart;
    }

    /**
     * @see #setSeries(List)
     */
    public List<Series> getSeries() {
        return Collections.unmodifiableList(series);
    }

    /**
     * Adds a single series to the list of series in this configuration.
     * 
     * @param series
     */
    public void addSeries(Series series) {
        this.series.add(series);
        series.setConfiguration(this);
        addSeriesToDrilldownConfiguration(series);
    }

    /**
     * Sets the actual series to append to the chart. Series objects contains
     * the data content (individual points/columns etc.) of the plot. The series
     * object could have data (the content), name, labels, tooltips, markers
     * etc.
     * 
     * <br />
     * <br />
     * In addition to the attributes listed above, any member of the plotOptions
     * for that specific type of plot can be added to a series individually. For
     * example, even though a general lineWidth is specified in
     * AbstractPlotOptions, an individual lineWidth can be specified for each
     * series (e.g. to enable each series have different lineWidth).
     * 
     * @param series
     */
    public void setSeries(List<Series> series) {
        this.series = series;
        for (Series s : series) {
            s.setConfiguration(this);
            addSeriesToDrilldownConfiguration(s);
        }
    }

    /**
     * @see #setSeries(List)
     * @param series
     */
    public void setSeries(Series... series) {
        setSeries(Arrays.asList(series));
    }

    /**
     * If series is a {@link DataSeries} and has drilldown Series that haven't
     * be added to the {@link Drilldown} configuration object they will be added
     * at this point
     * 
     * @param series
     */
    private void addSeriesToDrilldownConfiguration(Series series) {
        if (series instanceof DataSeries) {
            DataSeries dataSeries = (DataSeries) series;
            if (dataSeries.hasDrilldownSeries()) {
                Drilldown drilldown = getDrilldown();
                for (Series s : ((DataSeries) series).getDrilldownSeries()) {
                    drilldown.addSeries(s);
                    addSeriesToDrilldownConfiguration(s);
                }
            }
        }
    }

    /**
     * Configuration options for drill down, the concept of inspecting
     * increasingly high resolution data through clicking on chart items like
     * columns or pie slices.
     * 
     * @return current drilldown configuration
     */
    public Drilldown getDrilldown() {
        if (drilldown == null) {
            drilldown = new Drilldown();
            drilldown.setConfiguration(this);
        }
        return drilldown;
    }

    /**
     * @see #setTitle(Title)
     */
    public Title getTitle() {
        if (title == null) {
            title = new Title();
        }
        return title;
    }

    /**
     * The main title of the chart.
     * 
     * @param title
     */
    public void setTitle(Title title) {
        this.title = title;
    }

    /**
     * Sets the new chart's main title to the given string
     * 
     * @param text
     *            Text of title
     */
    public void setTitle(String text) {
        title = new Title(text);
    }

    /**
     * @return The chart's subtitle
     */
    public Subtitle getSubTitle() {
        if (subtitle == null) {
            subtitle = new Subtitle();
        }
        return subtitle;
    }

    /**
     * Sets the subtitle to the given text
     * 
     * @param text
     *            Text of subtitle
     */
    public void setSubTitle(String text) {
        subtitle = new Subtitle(text);
    }

    /**
     * Sets the chart's subtitle
     * 
     * @param subTitle
     */
    public void setSubTitle(Subtitle subTitle) {
        subtitle = subTitle;
    }

    /**
     * Returns the X-axis or category axis. Normally this is the horizontal
     * axis, though if the chart is inverted this is the vertical axis. In case
     * of multiple axes defined, the first axis is returned. An axis will be
     * created if no axis is defined.
     * 
     * @return the X-axis or category axis.
     */
    public XAxis getxAxis() {

        if (xAxis == null) {
            xAxis = new AxisList<>();
        }

        if (xAxis.getNumberOfAxes() == 0) {
            XAxis x = new XAxis();
            x.setConfiguration(this);
            xAxis.addAxis(x);
        }

        return xAxis.getAxis(0);
    }

    /**
     * @return the number of X-axes defined
     */
    public int getNumberOfxAxes() {
        if (xAxis == null) {
            return 0;
        } else {
            return xAxis.getNumberOfAxes();
        }
    }

    /**
     * @return The X-axis with the given index or null if the index is not valid
     */
    public XAxis getxAxis(int index) {
        if (index >= 0 && xAxis != null && getNumberOfxAxes() > index) {
            return xAxis.getAxis(index);
        } else {
            return null;
        }
    }

    /**
     * Removes all defined X-axes
     */
    public void removexAxes() {
        xAxis = null;
    }

    /**
     * Adds an X-axis to the configuration
     * 
     * @param axis
     *            The X-Axis to add.
     * @see #getxAxis()
     */
    public void addxAxis(XAxis axis) {
        if (xAxis == null) {
            xAxis = new AxisList<>();
        }
        if (axis.getConfiguration() == null) {
            axis.setConfiguration(this);
        }
        xAxis.addAxis(axis);
    }

    /**
     * Returns the Y-axis or value axis. Normally this is the vertical axis,
     * though if the chart is inverted this is the horizontal axis. In case
     * there are multiple axes defined (a list), only the first axis is
     * returned. If none exist, a Y-axis will be created.
     * 
     * @return The first Y-axis
     * @see #getyAxes()
     */
    public YAxis getyAxis() {
        if (yAxis == null) {
            yAxis = new AxisList<>();
        }

        if (yAxis.getNumberOfAxes() == 0) {
            YAxis y = new YAxis();
            y.setConfiguration(this);
            yAxis.addAxis(y);
        }

        return yAxis.getAxis(0);
    }

    /**
     * @return The number of Y-axes defined
     */
    public int getNumberOfyAxes() {
        if (yAxis == null) {
            return 0;
        } else {
            return yAxis.getNumberOfAxes();
        }
    }

    /**
     * @return The Y-axis with the given index or null if the index is not valid
     */
    public YAxis getyAxis(int index) {
        if (index >= 0 && yAxis != null && getNumberOfyAxes() > index) {
            return yAxis.getAxis(index);
        } else {
            return null;
        }
    }

    /**
     * Removes all defined Y-axes
     */
    public void removeyAxes() {
        yAxis = null;
    }

    /**
     * Adds a Y-axis.
     *
     * @param axis
     *            The Y-axis to add.
     * @see #getyAxes()
     * @see #getyAxis()
     */
    public void addyAxis(YAxis axis) {
        if (yAxis == null) {
            yAxis = new AxisList<>();
        }
        if (axis.getConfiguration() == null) {
            axis.setConfiguration(this);
        }
        yAxis.addAxis(axis);
    }

    /**
     * @return A list of all defined Y-axes, null if none are found.
     */
    public AxisList<YAxis> getyAxes() {
        if (yAxis != null) {
            return yAxis;
        } else {
            return null;
        }
    }

    /**
     * Returns the Z-axis. In case there are multiple axes defined (a list),
     * only the first axis is returned. If none exist, a Z-axis will be created.
     *
     * @return The first Z-axis
     * @see #getzAxes()
     */
    public ZAxis getzAxis() {
        if (zAxis == null) {
            zAxis = new AxisList<>();
        }

        if (zAxis.getNumberOfAxes() == 0) {
            ZAxis y = new ZAxis();
            y.setConfiguration(this);
            zAxis.addAxis(y);
        }

        return zAxis.getAxis(0);
    }

    /**
     * @return The number of Z-axes defined
     */
    public int getNumberOfzAxes() {
        if (zAxis == null) {
            return 0;
        } else {
            return zAxis.getNumberOfAxes();
        }
    }

    /**
     * @return The Z-axis with the given index or null if the index is not valid
     */
    public ZAxis getzAxis(int index) {
        if (index >= 0 && zAxis != null && getNumberOfzAxes() > index) {
            return zAxis.getAxis(index);
        } else {
            return null;
        }
    }

    /**
     * Removes all defined Z-axes
     */
    public void removezAxes() {
        zAxis = null;
    }

    /**
     * Adds a Z-axis.
     *
     * @param axis
     *            The Z-axis to add.
     * @see #getzAxes()
     * @see #getzAxis()
     */
    public void addzAxis(ZAxis axis) {
        if (zAxis == null) {
            zAxis = new AxisList<>();
        }
        if (axis.getConfiguration() == null) {
            axis.setConfiguration(this);
        }
        zAxis.addAxis(axis);
    }

    /**
     * @return A list of all defined Z-axes, null if none are found.
     */
    public AxisList<ZAxis> getzAxes() {
        if (zAxis != null) {
            return zAxis;
        } else {
            return null;
        }
    }

    /**
     * @see #setTooltip(Tooltip)
     * @return the tooltip options for this chart. If none have been set, a new
     *         Tooltip object with the default options is created.
     */
    public Tooltip getTooltip() {
        if (tooltip == null) {
            tooltip = new Tooltip();
        }
        return tooltip;
    }

    /**
     * Sets the options for the tooltip that appears when the user hovers over a
     * series or point.
     * 
     * @param tooltip
     */
    public void setTooltip(Tooltip tooltip) {
        this.tooltip = tooltip;
    }

    /**
     * @see #setCredits(Credits)
     */
    public Credits getCredits() {
        if (credits == null) {
            credits = new Credits();
        }
        return credits;
    }

    /**
     * Sets/changes the credits label that is added in the lower right corner of
     * the chart.
     * 
     * @param credits
     */
    public void setCredits(Credits credits) {
        this.credits = credits;
    }

    /**
     * Disables the credits by setting a Credits object with the enabled
     * property set to false.
     */
    public void disableCredits() {
        Credits disabled = new Credits();
        disabled.setEnabled(false);
        setCredits(disabled);
    }

    /**
     * @see #setLegend(Legend)
     */
    public Legend getLegend() {
        if (legend == null) {
            legend = new Legend();
        }
        return legend;
    }

    /**
     * Sets the legend. The legend is a box containing a symbol and name for
     * each series item or point item in the chart.
     * 
     * @param legend
     */
    public void setLegend(Legend legend) {
        this.legend = legend;
    }

    /**
     * Returns all plot options used by this configuration.
     * 
     * @see #setPlotOptions(AbstractPlotOptions)
     */
    public Collection<AbstractPlotOptions> getPlotOptions() {
        return plotOptions.values();
    }

    /**
     * Returns the plot options for a specific chart type used by this
     * configuration.
     * <p>
     * Returns null if no plot options was found for the type
     * </p>
     *
     * @see #setPlotOptions(AbstractPlotOptions)
     *
     * @param type
     */
    public AbstractPlotOptions getPlotOptions(ChartType type) {
        return plotOptions.get(type.toString());
    }

    /**
     * Sets component-wide default plot options.
     * <p>
     * If the component contains several chart types, this method can be called
     * with several plot option types. Subsequent calls with the same plot type
     * will replace previous options for that specific chart type i.e only the
     * latest options for each chart type is honored.
     * <p>
     * {@link PlotOptionsSeries} is a special plot options type that can be used
     * to define rules for all chart types.
     * 
     * @see AbstractPlotOptions
     * 
     * @param plotOptions
     */
    public void setPlotOptions(AbstractPlotOptions... plotOptions) {
        this.plotOptions.clear();
        for (AbstractPlotOptions po : plotOptions) {
            addPlotOptions(po);
        }
    }

    /**
     * Adds plot options
     * 
     * @see #setPlotOptions(AbstractPlotOptions)
     * 
     * @param plotOptions
     */
    public void addPlotOptions(AbstractPlotOptions plotOptions) {
        if (plotOptions instanceof PlotOptionsSeries) {
            this.plotOptions.put("series", plotOptions);
        } else {
            this.plotOptions.put(plotOptions.getChartType().toString(),
                    plotOptions);
        }
    }

    /**
     * @see #setLabels(HTMLLabels)
     * @return Labels or null if not defined
     */
    public HTMLLabels getLabels() {
        return labels;
    }

    /**
     * Sets HTML labels that can be positioned anywhere in the chart area.
     *
     * @param labels
     */
    public void setLabels(HTMLLabels labels) {
        this.labels = labels;
    }

    /**
     * Sets whether to enable exporting
     * 
     * @param exporting
     * @see Exporting
     * @see #setExporting(Exporting)
     */
    public void setExporting(Boolean exporting) {
        this.exporting.setEnabled(exporting);
    }

    /**
     * Sets the exporting module settings.
     * 
     * @param exporting
     * @see Exporting
     */
    public void setExporting(Exporting exporting) {
        this.exporting = exporting;
    }

    /**
     * @see #setExporting(Exporting)
     */
    public Exporting getExporting() {
        return exporting;
    }

    /**
     * @see #setExporting(Boolean)
     */
    public Boolean isExporting() {
        return exporting.getEnabled();
    }

    /**
     * @see #addPane(Pane)
     */
    public Pane getPane() {
        if (pane == null) {
            pane = new PaneList();
        }
        if (pane.getNumberOfPanes() == 0) {
            pane.addPane(new Pane());
        }

        return pane.getPane(0);
    }

    /**
     * Adds a pane. This applies only to polar charts and angular gauges. This
     * configuration object holds general options for the combined X and Y -axes
     * set. Each XAxis or YAxis can reference the pane by index.
     * 
     * @param pane
     */
    public void addPane(Pane pane) {
        if (this.pane == null) {
            this.pane = new PaneList();
        }
        this.pane.addPane(pane);
    }

    /**
     * Set settings for range selector.
     * <p>
     * This is only valid if the chart is configured to use timeline. See
     * {@link com.vaadin.addon.charts.Chart#setTimeline(boolean)}
     *
     * @param rangeSelector
     * @see RangeSelector
     */
    public void setRangeSelector(RangeSelector rangeSelector) {
        this.rangeSelector = rangeSelector;
    }

    /**
     * @see #setRangeSelector
     */
    public RangeSelector getRangeSelector() {
        if (rangeSelector == null) {
            rangeSelector = new RangeSelector();
        }
        return rangeSelector;
    }

    /**
     * @see #setScrollbar(Scrollbar)
     */
    public Scrollbar getScrollbar() {
        if (scrollbar == null) {
            scrollbar = new Scrollbar();
        }
        return scrollbar;
    }

    /**
     * Set configuration for the scrollbar.
     * 
     * Allows panning over the X axis of the chart
     * 
     * @param scrollbar
     */
    public void setScrollbar(Scrollbar scrollbar) {
        this.scrollbar = scrollbar;
    }

    /**
     * @see #setNoData(NoData)
     */
    public NoData getNoData() {
        if (noData == null) {
            noData = new NoData();
        }
        return noData;
    }

    /**
     * Set options for displaying a message when no data is available.
     * 
     * The actual text to display is set in the {@link Lang#setNoData(String)}
     * 
     * @param noData
     */
    public void setNoData(NoData noData) {
        this.noData = noData;
    }

    /**
     * @see #setNavigation(Navigation)
     */
    public Navigation getNavigation() {
        if (navigation == null) {
            navigation = new Navigation();
        }
        return navigation;
    }

    /**
     * Set options for buttons and menus appearing in the exporting module.
     * 
     * @param navigation
     */
    public void setNavigation(Navigation navigation) {
        this.navigation = navigation;
    }

    /**
     * @see #setLoading(Loading)
     */
    public Loading getLoading() {
        if (loading == null) {
            loading = new Loading();
        }
        return loading;
    }

    /**
     * Set loading options control the appearance of the loading screen that
     * covers the plot area on chart operations.
     * 
     * @param loading
     */
    public void setLoading(Loading loading) {
        this.loading = loading;
    }

    /**
     * @see #setNavigator(Navigator)
     */
    public Navigator getNavigator() {
        if (navigator == null) {
            navigator = new Navigator();
        }
        return navigator;
    }

    /**
     * Set configuration for the navigator
     *
     * @param navigator
     */
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    /**
     * Reverses the ListSeries (transposes it such that categories would be
     * series names and vice versa) to help stacking
     * 
     * throws {@link IllegalStateException} if series are not ListSeries type
     */
    public void reverseListSeries() {
        List<Series> newSeries = new ArrayList<>();
        String[] newCategories = new String[series.size()];

        for (int j = 0; j < getxAxis().getCategories().length; j++) {

            String name = getxAxis().getCategories()[j];
            List<Number> numbers = new ArrayList<>();
            for (int i = 0; i < series.size(); i++) {
                if (series.get(i) instanceof ListSeries) {
                    numbers.add(((ListSeries) series.get(i)).getData()[j]);
                    newCategories[i] = series.get(i).getName();
                } else {
                    throw new IllegalStateException();
                }
            }
            newSeries.add(new ListSeries(name, numbers.toArray(new Number[1])));
        }
        series = newSeries;
        getxAxis().setCategories(newCategories);
    }

    /**
     * Notifies listeners that a data point has been added
     * 
     * @param series
     * @param value
     */
    void fireDataAdded(Series series, Number value) {
        // TODO Enable it again
        // DataAddedEvent dataAddedEvent = new DataAddedEvent(series, value);
        // for (ConfigurationChangeListener listener : changeListeners) {
        // listener.dataAdded(dataAddedEvent);
        // }
    }

    /**
     * Notifies listeners that a data point has been added
     * 
     * @param shift
     */
    void fireDataAdded(Series series, DataSeriesItem item, boolean shift) {
        // TODO Enable it again
        // DataAddedEvent dataAddedEvent = new DataAddedEvent(series, item,
        // shift);
        // for (ConfigurationChangeListener listener : changeListeners) {
        // listener.dataAdded(dataAddedEvent);
        // }
    }

    /** Notifies listeners that a data point has been removed */
    void fireDataRemoved(Series series, int index) {
        // DataRemovedEvent dataRemovedEvent = new DataRemovedEvent(series,
        // index);
        // for (ConfigurationChangeListener listener : changeListeners) {
        // listener.dataRemoved(dataRemovedEvent);
        // }
    }

    /** Notifies listeners that a data point has been updated */
    void fireDataUpdated(Series series, Number value, int pointIndex) {
        // TODO Enable it again
        // DataUpdatedEvent dataUpdatedEvent = new DataUpdatedEvent(series,
        // value,
        // pointIndex);
        // for (ConfigurationChangeListener listener : changeListeners) {
        // listener.dataUpdated(dataUpdatedEvent);
        // }
    }

    /** Notifies listeners that a data point has been updated */
    void fireDataUpdated(Series series, DataSeriesItem item, int pointIndex) {
        // TODO Enable it again
        // DataUpdatedEvent dataUpdatedEvent = new DataUpdatedEvent(series,
        // item,
        // pointIndex);
        // for (ConfigurationChangeListener listener : changeListeners) {
        // listener.dataUpdated(dataUpdatedEvent);
        // }
    }

    /**
     * Notifies listeners that a data series has been updated.
     * 
     * @param series
     *            The updated series
     */
    void fireSeriesChanged(Series series) {
        // TODO Enable it again
        // SeriesChangedEvent event = new SeriesChangedEvent(series);
        // for (ConfigurationChangeListener listener : changeListeners) {
        // listener.seriesChanged(event);
        // }
    }

    /** Notifies listeners that a series is enabled or disabled */
    void fireSeriesEnabled(Series series, boolean enabled) {
        // TODO Enable it again
        // SeriesStateEvent seriesEnablationEvent = new SeriesStateEvent(series,
        // enabled);
        // for (ConfigurationChangeListener listener : changeListeners) {
        // listener.seriesStateChanged(seriesEnablationEvent);
        // }
    }

    /**
     * Fires animation change event.
     * 
     * @param animation
     *            Whether or not animation has been changed.
     */
    void fireAnimationChanged(boolean animation) {
        // TODO Enable it again
        // for (ConfigurationChangeListener listener : changeListeners) {
        // listener.animationChanged(animation);
        // }
    }

    /**
     * Gets the axis dimension.
     * 
     * @param axis
     *            Axis to check.
     * @return Dimension, as defined in ChartClientRpc.
     */
    private short getAxisDimension(Axis axis) {
        // TODO Enable it again
        // if (xAxis.contains(axis)) {
        // return ChartClientRpc.X_AXIS;
        // } else if (yAxis.contains(axis)) {
        // return ChartClientRpc.Y_AXIS;
        // } else if (zAxis.getAxes().contains(axis)) {
        // return ChartClientRpc.Z_AXIS;
        // } else if (colorAxis.getAxes().contains(axis)) {
        // return ChartClientRpc.COLOR_AXIS;
        // } else {
        return -1;
        // }
    }

    /**
     * Returns axis index in the dimension.
     * 
     * @param dimension
     *            Dimension of the axis.
     * @param axis
     *            Axis to get index for.
     * @return Index of the axis at given dimension.
     */
    private int getAxisIndex(short dimension, Axis axis) {
        // TODO Enable it again
        // switch (dimension) {
        // case ChartClientRpc.X_AXIS:
        // return xAxis.indexOf(axis);
        // case ChartClientRpc.Y_AXIS:
        // return yAxis.indexOf(axis);
        // case ChartClientRpc.Z_AXIS:
        // return zAxis.indexOf(axis);
        // case ChartClientRpc.COLOR_AXIS:
        // return colorAxis.getAxes().indexOf(axis);
        // default:
        return -1;
        // }
    }

    /**
     * Fires axis rescaled event.
     * 
     * @param axis
     *            Axis that is the source of the event.
     * @param minimum
     *            New minimum.
     * @param maximum
     *            New maximum.
     * @param redraw
     *            Whether or not to redraw.
     * @param animate
     *            Whether or not to animate.
     */
    @Override
    public void fireAxesRescaled(Axis axis, Number minimum, Number maximum,
            boolean redraw, boolean animate) {
        // TODO Enable it again
        // // determine the dimension of the axis, either x or y
        // short axisType = getAxisDimension(axis);
        //
        // int axisIndex = getAxisIndex(axisType, axis);
        //
        // AxisRescaledEvent event = new AxisRescaledEvent(axisType, axisIndex,
        // minimum, maximum, redraw, animate);
        // for (ConfigurationChangeListener listener : changeListeners) {
        // listener.axisRescaled(event);
        // }
    }

    /**
     * Reset zoom level by setting axes extremes to null
     */
    public void resetZoom() {
        this.resetZoom(true, true);
    }

    /**
     * Reset zoom level by setting axes extremes to null
     * 
     * @param redraw
     *            Whether or not to redraw.
     * @param animate
     *            Whether or not to animate.
     */
    public void resetZoom(boolean redraw, boolean animate) {
        // TODO Enable it again
        // for (ConfigurationChangeListener listener : changeListeners) {
        // listener.resetZoom(redraw, animate);
        // }
    }

    /**
     * Fires point sliced event
     * 
     * @param series
     * @param index
     * @param sliced
     * @param redraw
     * @param animation
     */
    void fireItemSliced(Series series, int index, boolean sliced,
            boolean redraw, boolean animation) {
        // TODO Enable it again
        // ItemSlicedEvent event = new ItemSlicedEvent(series, index, sliced,
        // redraw, animation);
        // for (ConfigurationChangeListener listener : changeListeners) {
        // listener.itemSliced(event);
        // }

    }

    /**
     * Sets the listener to be notified of e.g. data series changes
     * <p>
     * This method is used internally by the library. Usage by the end user will
     * cause unexpected behavior.
     * 
     * @param listener
     *            Listener to add. //
     */
    // public void addChangeListener(ConfigurationChangeListener listener) {
    // TODO Enable it again
    // if (!changeListeners.contains(listener)) {
    // changeListeners.add(listener);
    // }
    // }

    /**
     * Removes a change listener.
     * 
     * @param listener
     *            Listener to remove.
     */
    // public void removeChangeListener(ConfigurationChangeListener listener) {
    // TODO Enable it again
    // changeListeners.remove(listener);
    // }

    /**
     * Returns the color axis. This is used in color-based diagrams, like heat
     * maps. In case of multiple axes defined, the first axis is returned. An
     * axis will be created if no axis is defined.
     *
     * @return the color axis.
     */
    public ColorAxis getColorAxis() {

        if (colorAxis == null) {
            colorAxis = new AxisList<>();
        }

        if (colorAxis.getNumberOfAxes() == 0) {
            ColorAxis c = new ColorAxis();
            c.setConfiguration(this);
            colorAxis.addAxis(c);
        }

        return colorAxis.getAxis(0);
    }

    /**
     * @return the number of color axes defined
     */
    public int getNumberOfColorAxes() {
        if (colorAxis == null) {
            return 0;
        } else {
            return colorAxis.getNumberOfAxes();
        }
    }

    /**
     * @return The Color-axis with the given index or null if the index is not
     *         valid
     */
    public ColorAxis getColorAxis(int index) {
        if (index > 0 && colorAxis != null && getNumberOfColorAxes() > index) {
            return colorAxis.getAxis(index);
        } else {
            return null;
        }
    }

    /**
     * Removes all defined color-axes
     */
    public void removeColorAxes() {
        colorAxis = null;
    }

    /**
     * Adds a color-axis to the configuration
     *
     * @param axis
     *            The color Axis to add.
     * @see #getColorAxis()
     */
    public void addColorAxis(ColorAxis axis) {
        if (colorAxis == null) {
            colorAxis = new AxisList<>();
        }
        if (axis.getConfiguration() == null) {
            axis.setConfiguration(this);
        }
        colorAxis.addAxis(axis);
    }

}
