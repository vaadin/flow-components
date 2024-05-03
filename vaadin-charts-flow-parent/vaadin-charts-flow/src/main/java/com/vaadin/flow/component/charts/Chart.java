package com.vaadin.flow.component.charts;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2019 Vaadin Ltd
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

import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.charts.events.ChartAddSeriesEvent;
import com.vaadin.flow.component.charts.events.ChartAfterPrintEvent;
import com.vaadin.flow.component.charts.events.ChartBeforePrintEvent;
import com.vaadin.flow.component.charts.events.ChartClickEvent;
import com.vaadin.flow.component.charts.events.ChartDrillupAllEvent;
import com.vaadin.flow.component.charts.events.ChartDrillupEvent;
import com.vaadin.flow.component.charts.events.ChartLoadEvent;
import com.vaadin.flow.component.charts.events.ChartRedrawEvent;
import com.vaadin.flow.component.charts.events.ChartSelectionEvent;
import com.vaadin.flow.component.charts.events.DrilldownEvent;
import com.vaadin.flow.component.charts.events.PointClickEvent;
import com.vaadin.flow.component.charts.events.PointLegendItemClickEvent;
import com.vaadin.flow.component.charts.events.PointMouseOutEvent;
import com.vaadin.flow.component.charts.events.PointMouseOverEvent;
import com.vaadin.flow.component.charts.events.PointRemoveEvent;
import com.vaadin.flow.component.charts.events.PointSelectEvent;
import com.vaadin.flow.component.charts.events.PointUnselectEvent;
import com.vaadin.flow.component.charts.events.PointUpdateEvent;
import com.vaadin.flow.component.charts.events.SeriesAfterAnimateEvent;
import com.vaadin.flow.component.charts.events.SeriesCheckboxClickEvent;
import com.vaadin.flow.component.charts.events.SeriesClickEvent;
import com.vaadin.flow.component.charts.events.SeriesHideEvent;
import com.vaadin.flow.component.charts.events.SeriesLegendItemClickEvent;
import com.vaadin.flow.component.charts.events.SeriesMouseOutEvent;
import com.vaadin.flow.component.charts.events.SeriesMouseOverEvent;
import com.vaadin.flow.component.charts.events.SeriesShowEvent;
import com.vaadin.flow.component.charts.events.XAxesExtremesSetEvent;
import com.vaadin.flow.component.charts.events.YAxesExtremesSetEvent;
import com.vaadin.flow.component.charts.events.internal.ConfigurationChangeListener;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.Series;
import com.vaadin.flow.component.charts.util.ChartSerialization;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonObject;
import elemental.json.impl.JreJsonFactory;

@Tag("vaadin-chart")
@NpmPackage(value = "@vaadin/vaadin-charts", version = "6.3.6")
@JsModule("@vaadin/vaadin-charts/vaadin-chart.js")
@HtmlImport("frontend://bower_components/vaadin-charts/vaadin-chart.html")
public class Chart extends Component implements HasStyle, HasSize {

    private Configuration configuration;

    private transient JreJsonFactory jsonFactory = new JreJsonFactory();

    private final ConfigurationChangeListener changeListener = new ProxyChangeForwarder(
            this);

    private final static List<ChartType> TIMELINE_NOT_SUPPORTED = Arrays.asList(
            ChartType.PIE, ChartType.GAUGE, ChartType.SOLIDGAUGE,
            ChartType.PYRAMID, ChartType.FUNNEL);

    /**
     * Creates a new chart with default configuration
     */
    public Chart() {
        configuration = new Configuration();
    }

    /**
     * Creates a new chart with the given type
     *
     * @param type
     * @see #Chart()
     */
    public Chart(ChartType type) {
        this();
        getConfiguration().getChart().setType(type);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        attachEvent.getUI().beforeClientResponse(this, context -> {
            drawChart();
            if (configuration != null) {
                // Start listening to data series events once the chart has been
                // drawn.
                configuration.addChangeListener(changeListener);
            }
        });
    }

    JreJsonFactory getJsonFactory() {
        if (jsonFactory == null) {
            jsonFactory = new JreJsonFactory();
        }

        return jsonFactory;
    }

    /**
     * Draws a chart using the current configuration.
     *
     * @see #drawChart(boolean)
     */
    public void drawChart() {
        drawChart(false);
    }

    /**
     * Draws a chart using the current configuration.
     *
     * <p>
     * The chart takes the current configuration from
     * {@link #getConfiguration()}.
     * </p>
     *
     * <p>
     * Note that if you modify the underlying {@link Series} directly, the chart
     * will automatically be updated.
     * </p>
     *
     * <p>
     * Note that you don't need to call this method if {@link Configuration} is
     * ready before element is attached.
     * </p>
     *
     * @param resetConfiguration
     *            defines whether the chart should be redrawn or not
     * @see #getConfiguration()
     */
    public void drawChart(boolean resetConfiguration) {
        validateTimelineAndConfiguration();

        final JsonObject configurationNode = getJsonFactory()
                .parse(ChartSerialization.toJSON(configuration));

        getElement().callFunction("update", configurationNode,
                resetConfiguration);
    }

    /**
     * Determines if the chart is a timeline chart or a normal chart. The
     * following chart types do not support timeline:
     * <ul>
     * <li>ChartType.PIE</li>
     * <li>ChartType.GAUGE</li>
     * <li>ChartType.SOLIDGAUGE</li>
     * <li>ChartType.PYRAMID</li>
     * <li>ChartType.FUNNEL</li>
     * </ul>
     * Enabling timeline in these unsupported chart types results in an
     * <code>IllegalArgumentException</code>
     *
     * @param timeline
     *            true for timeline chart
     */
    public void setTimeline(Boolean timeline) {
        getElement().setProperty("timeline", timeline);
    }

    private void validateTimelineAndConfiguration() {
        if (getElement().getProperty("timeline", false)) {
            final ChartType type = getConfiguration().getChart().getType();
            if (TIMELINE_NOT_SUPPORTED.contains(type)) {
                throw new IllegalArgumentException(
                        "Timeline is not supported for chart type '" + type
                                + "'");
            }
        }
    }

    /**
     * The series or point visibility is toggled by default if user clicks the
     * legend item that corresponds to the series or point. Calling
     * setVisibilityTogglingDisabled( <code>true</code>) will disable this
     * behavior.
     *
     * @param disabled
     */
    public void setVisibilityTogglingDisabled(boolean disabled) {
        getElement().setProperty("_visibilityTogglingDisabled", disabled);
    }

    /**
     * @return the chart configuration that is used for this chart
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Adds a chart add series listener, which will be notified after a new
     * series is added to the chart
     *
     * @param listener
     */
    public Registration addChartAddSeriesListener(
            ComponentEventListener<ChartAddSeriesEvent> listener) {
        return addListener(ChartAddSeriesEvent.class, listener);
    }

    /**
     * Adds a chart after print listener, which will be notified after the chart
     * is printed using the print menu
     *
     * @param listener
     */
    public Registration addChartAfterPrintListener(
            ComponentEventListener<ChartAfterPrintEvent> listener) {
        return addListener(ChartAfterPrintEvent.class, listener);
    }

    /**
     * Adds a chart before print listener, which will be notified before the
     * chart is printed using the print menu
     *
     * @param listener
     */
    public Registration addChartBeforePrintListener(
            ComponentEventListener<ChartBeforePrintEvent> listener) {
        return addListener(ChartBeforePrintEvent.class, listener);
    }

    /**
     * Adds chart click listener, which will be notified of clicks on the chart
     * area
     *
     * @param listener
     */
    public Registration addChartClickListener(
            ComponentEventListener<ChartClickEvent> listener) {
        return addListener(ChartClickEvent.class, listener);
    }

    /**
     * Adds chart drillup listener, which will be notified of clicks on the
     * 'Back to previous series' button.
     *
     * @param listener
     */
    public Registration addChartDrillupListener(
            ComponentEventListener<ChartDrillupEvent> listener) {
        return addListener(ChartDrillupEvent.class, listener);
    }

    /**
     * Adds chart drillupall listener, which will be notified after all the
     * series have been drilled up in a chart with multiple drilldown series.
     *
     * @param listener
     */
    public Registration addChartDrillupAllListener(
            ComponentEventListener<ChartDrillupAllEvent> listener) {
        return addListener(ChartDrillupAllEvent.class, listener);
    }

    /**
     * Adds a chart load listener, which will be notified after a chart is
     * loaded
     *
     * @param listener
     */
    public Registration addChartLoadListener(
            ComponentEventListener<ChartLoadEvent> listener) {
        return addListener(ChartLoadEvent.class, listener);
    }

    /**
     * Adds a chart redraw listener, which will be notified after a chart is
     * redrawn
     *
     * @param listener
     */
    public Registration addChartRedrawListener(
            ComponentEventListener<ChartRedrawEvent> listener) {
        return addListener(ChartRedrawEvent.class, listener);
    }

    /**
     * Adds checkbox click listener, which will be notified when user has
     * clicked a checkbox in the legend
     *
     * @param listener
     */
    public Registration addCheckBoxClickListener(
            ComponentEventListener<SeriesCheckboxClickEvent> listener) {
        return addListener(SeriesCheckboxClickEvent.class, listener);
    }

    /**
     * Sets the Chart drilldown handler that's responsible for returning the
     * drilldown series for each drilldown callback when doing async drilldown
     *
     * @param listener
     * @see DataSeries#addItemWithDrilldown(DataSeriesItem) addItemWithDrilldown
     *      to find out how to enable async drilldown
     */
    public Registration addDrilldownListener(
            ComponentEventListener<DrilldownEvent> listener) {
        return addListener(DrilldownEvent.class, listener);
    }

    /**
     * Adds a chart selection listener
     *
     * <p>
     * Note that if a chart selection listener is set, default action for
     * selection is prevented. Most commonly this means that client side zoom
     * doesn't work and you are responsible for setting the zoom, etc in the
     * listener implementation.
     *
     * @param listener
     */
    public Registration addChartSelectionListener(
            ComponentEventListener<ChartSelectionEvent> listener) {
        return addListener(ChartSelectionEvent.class, listener);
    }

    /**
     * Adds a series legend item click listener, which will be notified of
     * clicks on the legend's items corresponding to a Series
     * <p>
     * Note that by default, clicking on a legend item toggles the visibility of
     * its associated series. To disable this behavior call
     * setVisibilityTogglingDisabled(<code>true</code>)
     *
     * @param listener
     * @see #setVisibilityTogglingDisabled(boolean)
     */
    public Registration addSeriesLegendItemClickListener(
            ComponentEventListener<SeriesLegendItemClickEvent> listener) {
        return addListener(SeriesLegendItemClickEvent.class, listener);
    }

    /**
     * Adds a point legend item click listener, which will be notified of clicks
     * on the legend's items corresponding to a Point
     * <p>
     * Note that by default, clicking on a legend item toggles the visibility of
     * its associated point. To disable this behavior call
     * setVisibilityTogglingDisabled(<code>true</code>)
     *
     * @param listener
     * @see #setVisibilityTogglingDisabled(boolean)
     */
    public Registration addPointLegendItemClickListener(
            ComponentEventListener<PointLegendItemClickEvent> listener) {
        return addListener(PointLegendItemClickEvent.class, listener);
    }

    /**
     * Adds a series after animate listener, which will be notified after a
     * series is animated
     *
     * @param listener
     */
    public Registration addSeriesAfterAnimateListener(
            ComponentEventListener<SeriesAfterAnimateEvent> listener) {
        return addListener(SeriesAfterAnimateEvent.class, listener);
    }

    /**
     * Adds a series click listener, which will be notified of clicks on the
     * series in the chart
     *
     * @param listener
     */
    public Registration addSeriesClickListener(
            ComponentEventListener<SeriesClickEvent> listener) {
        return addListener(SeriesClickEvent.class, listener);
    }

    /**
     * Adds a series hide listener, which will be notified when a series is
     * hidden
     *
     * @param listener
     */
    public Registration addSeriesHideListener(
            ComponentEventListener<SeriesHideEvent> listener) {
        return addListener(SeriesHideEvent.class, listener);
    }

    /**
     * Adds a point mouse out listener, which will be notified when the mouse
     * exits the neighborhood of a series
     *
     * @param listener
     */
    public Registration addSeriesMouseOutListener(
            ComponentEventListener<SeriesMouseOutEvent> listener) {
        return addListener(SeriesMouseOutEvent.class, listener);
    }

    /**
     * Adds a point mouse out listener, which will be notified when the mouse
     * enters the neighborhood of a series
     *
     * @param listener
     */
    public Registration addSeriesMouseOverListener(
            ComponentEventListener<SeriesMouseOverEvent> listener) {
        return addListener(SeriesMouseOverEvent.class, listener);
    }

    /**
     * Adds a series show listener, which will be notified when a series is
     * shown
     *
     * @param listener
     */
    public Registration addSeriesShowListener(
            ComponentEventListener<SeriesShowEvent> listener) {
        return addListener(SeriesShowEvent.class, listener);
    }

    /**
     * Adds a point click listener, which will be notified of clicks on the
     * points, bars or columns in the chart
     *
     * @param listener
     */
    public Registration addPointClickListener(
            ComponentEventListener<PointClickEvent> listener) {
        return addListener(PointClickEvent.class, listener);
    }

    /**
     * Adds a point mouse out listener, which will be notified when the mouse
     * exits the neighborhood of a data point
     *
     * @param listener
     */
    public Registration addPointMouseOutListener(
            ComponentEventListener<PointMouseOutEvent> listener) {
        return addListener(PointMouseOutEvent.class, listener);
    }

    /**
     * Adds a point mouse over listener, which will be notified when the mouse
     * enters the neighborhood of a data point
     *
     * @param listener
     */
    public Registration addPointMouseOverListener(
            ComponentEventListener<PointMouseOverEvent> listener) {
        return addListener(PointMouseOverEvent.class, listener);
    }

    /**
     * Adds a point remove listener, which will be notified when a data point is
     * removed.
     *
     * @param listener
     */
    public Registration addPointRemoveListener(
            ComponentEventListener<PointRemoveEvent> listener) {
        return addListener(PointRemoveEvent.class, listener);
    }

    /**
     * Adds a point select listener, which will be notified when a data point is
     * selected.
     *
     * @param listener
     */
    public Registration addPointSelectListener(
            ComponentEventListener<PointSelectEvent> listener) {
        return addListener(PointSelectEvent.class, listener);
    }

    /**
     * Adds a point unselect listener, which will be notified when a data point
     * is unselected.
     *
     * @param listener
     */
    public Registration addPointUnselectListener(
            ComponentEventListener<PointUnselectEvent> listener) {
        return addListener(PointUnselectEvent.class, listener);
    }

    /**
     * Adds a point update listener, which will be notified when a data point is
     * updated.
     *
     * @param listener
     */
    public Registration addPointUpdateListener(
            ComponentEventListener<PointUpdateEvent> listener) {
        return addListener(PointUpdateEvent.class, listener);
    }

    /**
     * Adds a x axes extremes set listener, which will be notified when an x
     * axis extremes are set
     *
     * @param listener
     */
    public Registration addXAxesExtremesSetListener(
            ComponentEventListener<XAxesExtremesSetEvent> listener) {
        return addListener(XAxesExtremesSetEvent.class, listener);
    }

    /**
     * Adds a y axes extremes set listener, which will be notified when an y
     * axis extremes are set
     *
     * @param listener
     */
    public Registration addYAxesExtremesSetListener(
            ComponentEventListener<YAxesExtremesSetEvent> listener) {
        return addListener(YAxesExtremesSetEvent.class, listener);
    }

}
