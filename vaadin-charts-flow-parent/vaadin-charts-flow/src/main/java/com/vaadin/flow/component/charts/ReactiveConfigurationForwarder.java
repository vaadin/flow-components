/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import com.vaadin.flow.component.charts.events.internal.DataAddedEvent;
import com.vaadin.flow.component.charts.events.internal.DataRemovedEvent;
import com.vaadin.flow.component.charts.events.internal.DataUpdatedEvent;
import com.vaadin.flow.component.charts.events.internal.ItemSlicedEvent;
import com.vaadin.flow.component.charts.events.internal.SeriesAddedEvent;
import com.vaadin.flow.component.charts.events.internal.SeriesChangedEvent;
import com.vaadin.flow.component.charts.events.internal.SeriesRemovedEvent;
import com.vaadin.flow.component.charts.events.internal.SeriesStateEvent;
import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;
import com.vaadin.flow.component.charts.model.Series;
import com.vaadin.flow.component.charts.util.ChartSerialization;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ObjectNode;

/**
 * {@link com.vaadin.flow.component.charts.events.internal.ConfigurationChangeListener}
 * used while the experimental {@code reactiveCharts} feature flag is enabled.
 * <p>
 * Series and data mutations are translated into id-keyed operations and
 * buffered on the {@link Chart} for a single coalesced sync (applied via the
 * client {@code $connector.syncSeries}), instead of the eager, index-based JS
 * calls used by {@link ProxyChangeForwarder}. Because the operations are keyed
 * by a stable internal id rather than positional index, a coalesced batch
 * mixing add/remove/update reconciles correctly regardless of intervening
 * structural changes. Axis and zoom events keep the inherited eager behavior.
 */
class ReactiveConfigurationForwarder extends ProxyChangeForwarder {

    ReactiveConfigurationForwarder(Chart chart) {
        super(chart);
    }

    private static ObjectNode op(String op) {
        ObjectNode node = JacksonUtils.getMapper().createObjectNode();
        node.put("op", op);
        return node;
    }

    private String vid(Series series) {
        return chart.getConfiguration().ensureSeriesReactiveId(series);
    }

    private static ObjectNode toJson(Object configurationObject) {
        return (ObjectNode) JacksonUtils.readTree(ChartSerialization
                .toJSON((AbstractConfigurationObject) configurationObject));
    }

    @Override
    public void seriesAdded(SeriesAddedEvent event) {
        ObjectNode op = op("addSeries");
        vid(event.getSeries()); // assign id before serializing the config
        op.set("config", toJson(event.getSeries()));
        chart.enqueueSeriesOp(op);
    }

    @Override
    public void seriesChanged(SeriesChangedEvent event) {
        ObjectNode op = op("updateSeries");
        op.put("vid", vid(event.getSeries()));
        op.set("config", toJson(event.getSeries()));
        chart.enqueueSeriesOp(op);
    }

    @Override
    public void seriesRemoved(SeriesRemovedEvent event) {
        String vid = chart.getConfiguration()
                .getSeriesReactiveId(event.getSeries());
        if (vid == null) {
            return;
        }
        ObjectNode op = op("removeSeries");
        op.put("vid", vid);
        chart.enqueueSeriesOp(op);
    }

    @Override
    public void seriesStateChanged(SeriesStateEvent event) {
        ObjectNode op = op("setSeriesVisible");
        op.put("vid", vid(event.getSeries()));
        op.put("visible", event.isEnabled());
        chart.enqueueSeriesOp(op);
    }

    @Override
    public void dataAdded(DataAddedEvent event) {
        if (event.getItem() == null) {
            return;
        }
        ObjectNode op = op("addPoint");
        op.put("vid", vid(event.getSeries()));
        op.set("point", toJson(event.getItem()));
        op.put("shift", event.isShift());
        chart.enqueueSeriesOp(op);
    }

    @Override
    public void dataRemoved(DataRemovedEvent event) {
        ObjectNode op = op("removePoint");
        op.put("vid", vid(event.getSeries()));
        op.put("index", event.getIndex());
        chart.enqueueSeriesOp(op);
    }

    @Override
    public void dataUpdated(DataUpdatedEvent event) {
        ObjectNode op = op("updatePoint");
        op.put("vid", vid(event.getSeries()));
        op.put("index", event.getPointIndex());
        if (event.getValue() != null) {
            op.put("value", event.getValue().doubleValue());
        } else {
            op.set("value", toJson(event.getItem()));
        }
        chart.enqueueSeriesOp(op);
    }

    @Override
    public void itemSliced(ItemSlicedEvent event) {
        ObjectNode op = op("slicePoint");
        op.put("vid", vid(event.getSeries()));
        op.put("index", event.getIndex());
        op.put("sliced", event.isSliced());
        op.put("animation", event.isAnimation());
        chart.enqueueSeriesOp(op);
    }
}
