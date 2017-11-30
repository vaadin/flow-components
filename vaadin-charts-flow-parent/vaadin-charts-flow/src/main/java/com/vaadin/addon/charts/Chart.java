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

import static com.vaadin.addon.charts.model.AxisDimension.X_AXIS;
import static com.vaadin.addon.charts.model.AxisDimension.Y_AXIS;

import com.vaadin.addon.charts.events.AbstractSeriesEvent;
import com.vaadin.addon.charts.events.AxisRescaledEvent;
import com.vaadin.addon.charts.events.ConfigurationChangeListener;
import com.vaadin.addon.charts.events.DataAddedEvent;
import com.vaadin.addon.charts.events.DataRemovedEvent;
import com.vaadin.addon.charts.events.DataUpdatedEvent;
import com.vaadin.addon.charts.events.ItemSlicedEvent;
import com.vaadin.addon.charts.events.SeriesChangedEvent;
import com.vaadin.addon.charts.events.SeriesStateEvent;
import com.vaadin.addon.charts.model.AbstractConfigurationObject;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.util.ChartSerialization;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tag;
import com.vaadin.ui.common.HtmlImport;
import com.vaadin.ui.event.AttachEvent;

import elemental.json.JsonObject;
import elemental.json.impl.JreJsonFactory;

@Tag("vaadin-chart")
@HtmlImport("frontend://bower_components/vaadin-charts/vaadin-chart.html")
public class Chart extends Component {

    private static class ProxyChangeForwarder
            implements ConfigurationChangeListener {
        private final Chart chart;
        private final JreJsonFactory jsonFactory;

        public ProxyChangeForwarder(Chart chart, JreJsonFactory jsonFactory) {
            this.chart = chart;
            this.jsonFactory = jsonFactory;
        }

        @Override
        public void dataAdded(DataAddedEvent event) {
            if (event.getItem() != null) {
                chart.getElement().callFunction("__callSeriesFunction",
                        "addPoint", getSeriesIndex(event),
                        jsonFactory.parse(
                                ChartSerialization.toJSON(event.getItem())),
                        true, event.isShift());
            }
        }

        @Override
        public void dataRemoved(DataRemovedEvent event) {
            chart.getElement().callFunction("__callPointFunction", "remove",
                    getSeriesIndex(event), event.getIndex());
        }

        @Override
        public void dataUpdated(DataUpdatedEvent event) {
            if (event.getValue() != null) {
                chart.getElement().callFunction("__callPointFunction", "update",
                        getSeriesIndex(event), event.getPointIndex(),
                        event.getValue().doubleValue());
            } else {
                chart.getElement().callFunction("__callPointFunction", "update",
                        getSeriesIndex(event), event.getPointIndex(),
                        jsonFactory.parse(
                                ChartSerialization.toJSON(event.getItem())));
            }
        }

        @Override
        public void seriesStateChanged(SeriesStateEvent event) {
            if (event.isEnabled()) {
                chart.getElement().callFunction("__callSeriesFunction", "show",
                        getSeriesIndex(event));
            } else {
                chart.getElement().callFunction("__callSeriesFunction", "hide",
                        getSeriesIndex(event));
            }
        }

        @Override
        public void axisRescaled(AxisRescaledEvent event) {
            chart.getElement().callFunction("__callAxisFunction", "setExtremes",
                    event.getAxis(), event.getAxisIndex(),
                    event.getMinimum().doubleValue(),
                    event.getMaximum().doubleValue(), event.isRedrawingNeeded(),
                    event.isAnimated());
        }

        @Override
        public void itemSliced(ItemSlicedEvent event) {
            chart.getElement().callFunction("__callPointFunction", "slice",
                    getSeriesIndex(event), event.getIndex(), event.isSliced(),
                    event.isRedraw(), event.isAnimation());
        }

        @Override
        public void seriesChanged(SeriesChangedEvent event) {
            chart.getElement().callFunction("__callSeriesFunction", "update",
                    getSeriesIndex(event),
                    jsonFactory.parse(ChartSerialization.toJSON(
                            (AbstractConfigurationObject) event.getSeries())));
        }

        @Override
        public void resetZoom(boolean redraw, boolean animate) {
            for (int i = 0; i < chart.getConfiguration()
                    .getNumberOfxAxes(); i++) {
                chart.getElement().callFunction("__callAxisFunction",
                        "setExtremes", X_AXIS.getIndex(), i, null, null, redraw,
                        animate);
            }
            for (int i = 0; i < chart.getConfiguration()
                    .getNumberOfyAxes(); i++) {
                chart.getElement().callFunction("__callAxisFunction",
                        "setExtremes", Y_AXIS.getIndex(), i, null, null, redraw,
                        animate);
            }
        }

        private int getSeriesIndex(AbstractSeriesEvent event) {
            return chart.getConfiguration().getSeries()
                    .indexOf(event.getSeries());
        }

    }

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
     * @return the chart configuration that is used for this chart
     */
    public Configuration getConfiguration() {
        return configuration;
    }

}
