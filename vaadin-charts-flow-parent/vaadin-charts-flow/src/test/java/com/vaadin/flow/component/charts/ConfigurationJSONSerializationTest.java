package com.vaadin.flow.component.charts;

import static com.vaadin.flow.component.charts.util.ChartSerialization.toJSON;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.vaadin.flow.component.charts.events.internal.AxisRescaledEvent;
import com.vaadin.flow.component.charts.events.internal.ConfigurationChangeListener;
import com.vaadin.flow.component.charts.events.internal.DataAddedEvent;
import com.vaadin.flow.component.charts.events.internal.DataRemovedEvent;
import com.vaadin.flow.component.charts.events.internal.DataUpdatedEvent;
import com.vaadin.flow.component.charts.events.internal.ItemSlicedEvent;
import com.vaadin.flow.component.charts.events.internal.SeriesAddedEvent;
import com.vaadin.flow.component.charts.events.internal.SeriesChangedEvent;
import com.vaadin.flow.component.charts.events.internal.SeriesStateEvent;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.YAxis;

/**
 * Tests for the JSON serialization in {@link Configuration}
 *
 */
public class ConfigurationJSONSerializationTest {

    @Test
    public void configurationJSONSerialization_configurationSerializedWithChangeListener_changeListenerNotSerialized() {
        Configuration conf = new Configuration();
        conf.addChangeListener(new ConfigurationChangeListener() {

            @Override
            public void dataAdded(DataAddedEvent event) {
                // TODO Auto-generated method stub

            }

            @Override
            public void dataRemoved(DataRemovedEvent event) {
                // TODO Auto-generated method stub

            }

            @Override
            public void dataUpdated(DataUpdatedEvent event) {
                // TODO Auto-generated method stub

            }

            @Override
            public void itemSliced(ItemSlicedEvent event) {
                // TODO Auto-generated method stub

            }

            @Override
            public void seriesStateChanged(SeriesStateEvent event) {
                // TODO Auto-generated method stub

            }

            @Override
            public void seriesAdded(SeriesAddedEvent event) {
                // TODO Auto-generated method stub

            }

            @Override
            public void seriesChanged(SeriesChangedEvent event) {
                // TODO Auto-generated method stub

            }

            @Override
            public void axisRescaled(AxisRescaledEvent event) {
                // TODO Auto-generated method stub

            }

            @Override
            public void resetZoom(boolean redraw, boolean animate) {
                // TODO Auto-generated method stub

            }
        });
        assertEquals(
                "{\"chart\":{\"styledMode\":false},\"plotOptions\":{},\"series\":[],\"exporting\":{\"enabled\":false}}",
                toJSON(conf));
    }

    @Test
    public void configurationJSONSerialization_configurationSerializedWithYAxis_yAxisConfigurationNotSerialized() {
        Configuration conf = new Configuration();
        YAxis axis = new YAxis();
        conf.addyAxis(axis);
        assertEquals(
                "{\"chart\":{\"styledMode\":false},\"yAxis\":{\"axisIndex\":0},\"plotOptions\":{},\"series\":[],\"exporting\":{\"enabled\":false}}",
                toJSON(conf));
    }

    @Test
    public void configurationJSONSerialization_configurationSerializedStyledMode_styledModeSerialized() {
        Configuration conf = new Configuration();
        conf.getChart().setStyledMode(true);
        assertEquals(
                "{\"chart\":{\"styledMode\":true},\"plotOptions\":{},\"series\":[],\"exporting\":{\"enabled\":false}}",
                toJSON(conf));
    }
}
