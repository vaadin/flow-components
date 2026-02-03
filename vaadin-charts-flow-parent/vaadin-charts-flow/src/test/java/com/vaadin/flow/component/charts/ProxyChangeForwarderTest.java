/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import com.vaadin.flow.component.charts.events.internal.AxisRescaledEvent;
import com.vaadin.flow.component.charts.events.internal.DataAddedEvent;
import com.vaadin.flow.component.charts.events.internal.DataRemovedEvent;
import com.vaadin.flow.component.charts.events.internal.DataUpdatedEvent;
import com.vaadin.flow.component.charts.events.internal.ItemSlicedEvent;
import com.vaadin.flow.component.charts.events.internal.SeriesAddedEvent;
import com.vaadin.flow.component.charts.events.internal.SeriesChangedEvent;
import com.vaadin.flow.component.charts.events.internal.SeriesStateEvent;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;

/**
 * Unit tests for {@link ProxyChangeForwarder} verifying that calls to
 * {@code callJsFunction} are forwarded with correct function names.
 */
public class ProxyChangeForwarderTest {

    private static class TestChart extends Chart {
        private final Element element;

        TestChart() {
            super(ChartType.LINE);
            this.element = mock(Element.class);
        }

        @Override
        public Element getElement() {
            return element;
        }
    }

    @Test
    public void dataAdded_itemSerializedAsJson() {
        TestChart chart = new TestChart();
        Configuration conf = chart.getConfiguration();
        DataSeries series = new DataSeries();
        conf.addSeries(series);

        ProxyChangeForwarder forwarder = new ProxyChangeForwarder(chart);

        DataSeriesItem item = new DataSeriesItem();
        item.setX(10);
        item.setY(5);
        DataAddedEvent event = new DataAddedEvent(series, item, true);
        forwarder.dataAdded(event);

        var expectedData = JacksonUtils.createArrayNode();
        expectedData.add(10);
        expectedData.add(5);
        verify(chart.getElement()).callJsFunction("__callSeriesFunction",
                "addPoint", 0, expectedData, true, true);
    }

    @Test
    public void dataRemoved_indexPassedToPointFunction() {
        TestChart chart = new TestChart();
        Configuration conf = chart.getConfiguration();
        DataSeries series = new DataSeries();
        conf.addSeries(series);

        ProxyChangeForwarder forwarder = new ProxyChangeForwarder(chart);

        DataRemovedEvent event = new DataRemovedEvent(series, 3);
        forwarder.dataRemoved(event);
        verify(chart.getElement()).callJsFunction("__callPointFunction",
                "remove", 0, 3);
    }

    @Test
    public void dataUpdated_valueNumberSerialized() {
        TestChart chart = new TestChart();
        Configuration conf = chart.getConfiguration();
        DataSeries series = new DataSeries();
        conf.addSeries(series);

        ProxyChangeForwarder forwarder = new ProxyChangeForwarder(chart);

        DataUpdatedEvent event = new DataUpdatedEvent(series, 10.5, 1);
        forwarder.dataUpdated(event);
        verify(chart.getElement()).callJsFunction("__callPointFunction",
                "update", 0, 1, 10.5);
    }

    @Test
    public void dataUpdated_itemSerializedWhenValueNull() {
        TestChart chart = new TestChart();
        Configuration conf = chart.getConfiguration();
        DataSeries series = new DataSeries();
        conf.addSeries(series);

        ProxyChangeForwarder forwarder = new ProxyChangeForwarder(chart);

        DataSeriesItem item = new DataSeriesItem();
        item.setY(7);
        DataUpdatedEvent event = new DataUpdatedEvent(series, item, 2);
        forwarder.dataUpdated(event);
        verify(chart.getElement()).callJsFunction("__callPointFunction",
                "update", 0, 2, JacksonUtils.createNode(7));
    }

    @Test
    public void seriesStateChanged_showHideBasedOnEnabledFlag() {
        TestChart chart = new TestChart();
        Configuration conf = chart.getConfiguration();
        DataSeries series = new DataSeries();
        conf.addSeries(series);

        ProxyChangeForwarder forwarder = new ProxyChangeForwarder(chart);

        SeriesStateEvent showEvent = new SeriesStateEvent(series, true);
        forwarder.seriesStateChanged(showEvent);
        verify(chart.getElement()).callJsFunction("__callSeriesFunction",
                "show", 0);

        SeriesStateEvent hideEvent = new SeriesStateEvent(series, false);
        forwarder.seriesStateChanged(hideEvent);
        verify(chart.getElement()).callJsFunction("__callSeriesFunction",
                "hide", 0);
    }

    @Test
    public void axisRescaled_serializesMinMaxAndFlags() {
        TestChart chart = new TestChart();
        Configuration conf = chart.getConfiguration();
        conf.addxAxis(new XAxis());

        ProxyChangeForwarder forwarder = new ProxyChangeForwarder(chart);

        AxisRescaledEvent event = new AxisRescaledEvent(0, 0, 1d, 10d, true,
                false);
        forwarder.axisRescaled(event);
        verify(chart.getElement()).callJsFunction("__callAxisFunction",
                "setExtremes", 0, 0, 1d, 10d, true, false);
    }

    @Test
    public void itemSliced_argumentsForwardedCorrectly() {
        TestChart chart = new TestChart();
        Configuration conf = chart.getConfiguration();
        DataSeries series = new DataSeries();
        conf.addSeries(series);

        ProxyChangeForwarder forwarder = new ProxyChangeForwarder(chart);

        ItemSlicedEvent event = new ItemSlicedEvent(series, 4, true, false,
                true);
        forwarder.itemSliced(event);
        verify(chart.getElement()).callJsFunction("__callPointFunction",
                "slice", 0, 4, true, false, true);
    }

    @Test
    public void seriesAdded_seriesSerializedToJson() {
        TestChart chart = new TestChart();
        Configuration conf = chart.getConfiguration();
        ListSeries series = new ListSeries("name", 1, 2, 3);
        conf.addSeries(series);

        ProxyChangeForwarder forwarder = new ProxyChangeForwarder(chart);

        SeriesAddedEvent event = new SeriesAddedEvent(series);
        forwarder.seriesAdded(event);

        var expectedData = JacksonUtils
                .readTree("{\"data\": [1,2,3],\"name\":\"name\"}");
        verify(chart.getElement()).callJsFunction("__callChartFunction",
                "addSeries", expectedData);
    }

    @Test
    public void seriesChanged_seriesSerializedToJson() {
        TestChart chart = new TestChart();
        Configuration conf = chart.getConfiguration();
        ListSeries series = new ListSeries("name", 1, 2, 3);
        conf.addSeries(series);

        ProxyChangeForwarder forwarder = new ProxyChangeForwarder(chart);

        series.setName("updated");
        SeriesChangedEvent event = new SeriesChangedEvent(series);
        forwarder.seriesChanged(event);
        var expectedData = JacksonUtils
                .readTree("{\"data\": [1,2,3],\"name\":\"updated\"}");
        verify(chart.getElement()).callJsFunction("__callSeriesFunction",
                "update", 0, expectedData);
    }

    @Test
    public void resetZoom_clearsAllAxes() {
        TestChart chart = new TestChart();
        Configuration conf = chart.getConfiguration();
        conf.addxAxis(new XAxis());
        conf.addxAxis(new XAxis());
        conf.addyAxis(new YAxis());

        ProxyChangeForwarder forwarder = new ProxyChangeForwarder(chart);

        forwarder.resetZoom(true, false);
        // Last call corresponds to the Y-axis index 0
        verify(chart.getElement()).callJsFunction("__callAxisFunction",
                "setExtremes", 1, 0, null, null, true, false);
    }
}
