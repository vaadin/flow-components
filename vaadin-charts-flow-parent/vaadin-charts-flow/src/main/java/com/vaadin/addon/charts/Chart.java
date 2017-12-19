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
package com.vaadin.addon.charts;
import com.vaadin.addon.charts.events.ConfigurationChangeListener;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.util.ChartSerialization;
import com.vaadin.shared.Registration;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tag;
import com.vaadin.ui.common.HtmlImport;
import com.vaadin.ui.event.AttachEvent;

import com.vaadin.ui.event.ComponentEventListener;
import elemental.json.JsonObject;
import elemental.json.impl.JreJsonFactory;

@Tag("vaadin-chart")
@HtmlImport("frontend://bower_components/vaadin-charts/vaadin-chart.html")
public class Chart extends Component {

    private Configuration configuration;
    private final JreJsonFactory jsonFactory = new JreJsonFactory();
    private final ConfigurationChangeListener changeListener = new ProxyChangeForwarder(
            this, jsonFactory);

    /**
     * Creates a new chart with default configuration
     */
    public Chart() {
        configuration = new Configuration();
    }

    /**
     * Creates a new chart with the given type
     *
     * @see #Chart()
     * @param type
     */
    public Chart(ChartType type) {
        this();
        getConfiguration().getChart().setType(type);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        attachEvent.getUI().beforeClientResponse(this, () -> {
            drawChart();
            if (configuration != null) {
                // Start listening to data series events once the chart has been
                // drawn.
                configuration.addChangeListener(changeListener);
            }
        });
    }

    /**
     * Draws a chart with the given configuration as a starting point.
     *
     * @see #drawChart(boolean)
     */
    public void drawChart() {
        drawChart(false);
    }

    /**
     * Draws a chart with the given configuration as a starting point.
     * <p>
     * Note that you don't need to call this method if {@link Configuration} is
     * ready before element is attached.
     * </p>
     *
     * @see #getConfiguration()
     * @param resetConfiguration
     *            defines whether the chart should be redrawn or not
     */
    public void drawChart(boolean resetConfiguration) {
        final JsonObject configurationNode = jsonFactory
                .parse(ChartSerialization.toJSON(configuration));

        getElement().callFunction("update", configurationNode,
                resetConfiguration);
    }

    /**
     * Determines if the chart is a timeline chart or a normal chart.
     *
     * @param timeline
     *            true for timeline chart
     */
    public void setTimeline(Boolean timeline) {
        getElement().setProperty("timeline", timeline);
    }

    /**
     * The series visibility is toggled by default if user clicks the series
     * item in the legend. Calling setSeriesVisibilityTogglingDisabled(
     * <code>true</code>) will disable this behaviour.
     *
     * @param disabled
     */
    public void setSeriesVisibilityTogglingDisabled(boolean disabled) {
        getElement().setProperty("_seriesVisibilityTogglingDisabled", disabled);
    }

    /**
     * @return the chart configuration that is used for this chart
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Adds a chart add series listener, which will be notified after a
     * new series is added to the chart
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
     * Adds a chart before print listener, which will be notified before the chart
     * is printed using the print menu
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
     * Adds chart drillupall listener, which will be notified after all the series
     * have been drilled up in a chart with multiple drilldown series.
     *
     * @param listener
     */
    public Registration addChartDrillupAllListener(
            ComponentEventListener<ChartDrillupAllEvent> listener) {
        return addListener(ChartDrillupAllEvent.class, listener);
    }

    /**
     * Adds a chart load listener, which will be notified after a chart
     * is loaded
     *
     * @param listener
     */
    public Registration addChartLoadListener(
            ComponentEventListener<ChartLoadEvent> listener) {
        return addListener(ChartLoadEvent.class, listener);
    }

    /**
     * Adds a chart redraw listener, which will be notified after a chart
     * is redrawn
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
     * @see DataSeries#addItemWithDrilldown(DataSeriesItem)
     *      addItemWithDrilldown to find out how to enable async drilldown
     *
     * @param listener
     */
    public Registration addDrilldownListener(
            ComponentEventListener<DrilldownEvent> listener) {
        return addListener(DrilldownEvent.class, listener);
    }

    /**
     * Adds a chart selection listener<br />
     * <br />
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
     * Adds a legend item click listener, which will be notified of clicks on
     * the legend's items
     * <p>
     * Note that by default, clicking on a legend item toggles the visibility
     * of its associated series. To disable this behavior call
     * setSeriesVisibilityTogglingDisabled(<code>true</code>)
     *
     * @param listener
     *
     * @see #setSeriesVisibilityTogglingDisabled(boolean)
     */
    public Registration addLegendItemClickListener(
            ComponentEventListener<SeriesLegendItemClickEvent> listener) {
        return addListener(SeriesLegendItemClickEvent.class, listener);
    }

    /**
     * Adds a series after animate listener, which will be notified after a series
     * is animated
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
     * Adds a point remove listener, which will be notified when a data point
     * is removed.
     *
     * @param listener
     */
    public Registration addPointRemoveListener(
            ComponentEventListener<PointRemoveEvent> listener) {
        return addListener(PointRemoveEvent.class, listener);
    }

    /**
     * Adds a point select listener, which will be notified when a data point
     * is selected.
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
     * Adds a point update listener, which will be notified when a data point
     * is updated.
     *
     * @param listener
     */
    public Registration addPointUpdateListener(
            ComponentEventListener<PointUpdateEvent> listener) {
        return addListener(PointUpdateEvent.class, listener);
    }
}
