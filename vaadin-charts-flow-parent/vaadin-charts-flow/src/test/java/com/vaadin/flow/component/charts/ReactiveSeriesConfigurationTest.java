/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.util.ChartSerialization;
import com.vaadin.flow.dom.Element;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

/**
 * Unit coverage for the reactive series/point op path (Track 2). Drives real
 * {@link Configuration} mutations through
 * {@link ReactiveConfigurationForwarder} and asserts the id-keyed operation
 * list emitted to the client {@code $connector.syncSeries}, without depending
 * on Flow's attach lifecycle.
 */
class ReactiveSeriesConfigurationTest {

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

        // The tests drive synchronizeReactiveConfiguration() directly;
        // returning
        // no UI keeps requestReactiveSync() a no-op without an attach
        // lifecycle.
        @Override
        public Optional<UI> getUI() {
            return Optional.empty();
        }
    }

    /**
     * Mirrors Chart.onAttach for the reactive path: assign internal ids to the
     * series already present, install the reactive forwarder + trigger, and
     * seed the snapshot. Mutations performed after this are the ones under
     * test.
     */
    private static void startReactive(TestChart chart) {
        Configuration conf = chart.getConfiguration();
        conf.getSeries().forEach(conf::ensureSeriesReactiveId);
        conf.addChangeListener(new ReactiveConfigurationForwarder(chart));
        conf.setReactiveSyncTrigger(() -> {
        });
        chart.seedReactiveSnapshot();
    }

    private static List<String> opTypes(ArrayNode ops) {
        List<String> types = new ArrayList<>();
        ops.forEach(op -> types.add(op.get("op").asString()));
        return types;
    }

    private static ArrayNode captureSyncSeries(TestChart chart) {
        ArgumentCaptor<ArrayNode> captor = ArgumentCaptor
                .forClass(ArrayNode.class);
        verify(chart.getElement()).executeJs(
                eq("this.$connector.syncSeries($0)"), captor.capture());
        return captor.getValue();
    }

    @Test
    void addSeries_emitsAddSeriesOp_withInternalReactiveId() {
        TestChart chart = new TestChart();
        Configuration conf = chart.getConfiguration();
        startReactive(chart);

        conf.addSeries(new DataSeries("A"));

        chart.synchronizeReactiveConfiguration();

        ArrayNode ops = captureSyncSeries(chart);
        assertEquals(1, ops.size());
        JsonNode op = ops.get(0);
        assertEquals("addSeries", op.get("op").asString());
        assertTrue(op.get("config").has("vaadinReactiveId"),
                "addSeries config must carry the internal reactive id");
    }

    @Test
    void pointMutations_emitIdKeyedPointOps_inOrder() {
        TestChart chart = new TestChart();
        Configuration conf = chart.getConfiguration();
        DataSeries ds = new DataSeries("S");
        ds.add(new DataSeriesItem("p0", 1));
        conf.addSeries(ds);
        startReactive(chart);
        String vid = conf.getSeriesReactiveId(ds);

        ds.add(new DataSeriesItem("p1", 2)); // addPoint
        ds.remove(ds.get(0)); // removePoint @ index 0

        chart.synchronizeReactiveConfiguration();

        ArrayNode ops = captureSyncSeries(chart);
        assertEquals(List.of("addPoint", "removePoint"), opTypes(ops));
        ops.forEach(op -> assertEquals(vid, op.get("vid").asString(),
                "every point op must address the series by its internal id"));
        assertEquals(0, ops.get(1).get("index").asInt());
    }

    @Test
    void setSeriesList_reconcilesRemoveRetainAdd_byId() {
        TestChart chart = new TestChart();
        Configuration conf = chart.getConfiguration();
        DataSeries s1 = new DataSeries("A");
        DataSeries s2 = new DataSeries("B");
        conf.addSeries(s1);
        conf.addSeries(s2);
        startReactive(chart);
        String s2Id = conf.getSeriesReactiveId(s2);

        DataSeries s3 = new DataSeries("C");
        conf.setSeries(List.of(s1, s3)); // keep s1, drop s2, add s3

        chart.synchronizeReactiveConfiguration();

        ArrayNode ops = captureSyncSeries(chart);
        // removes first, then retained (update), then new (add)
        assertEquals(List.of("removeSeries", "updateSeries", "addSeries"),
                opTypes(ops));
        assertEquals(s2Id, ops.get(0).get("vid").asString(),
                "the dropped series must be removed by its internal id");
    }

    @Test
    void seriesVisibility_emitsSetSeriesVisibleOp() {
        TestChart chart = new TestChart();
        Configuration conf = chart.getConfiguration();
        DataSeries ds = new DataSeries("S");
        conf.addSeries(ds);
        startReactive(chart);

        ds.setVisible(false);

        chart.synchronizeReactiveConfiguration();

        ArrayNode ops = captureSyncSeries(chart);
        assertEquals(List.of("setSeriesVisible"), opTypes(ops));
        assertFalse(ops.get(0).get("visible").asBoolean());
    }

    // -------------------------------------------------- flag-off serialization

    @Test
    void seriesWithoutReactiveId_serializesUnchanged() {
        String json = ChartSerialization.toJSON(new DataSeries("S"));
        assertFalse(json.contains("vaadinReactiveId"),
                "no reactive id must be serialized until one is assigned");
    }

    @Test
    void ensureReactiveId_addsReactiveIdToSerialization() {
        Configuration conf = new Configuration();
        DataSeries ds = new DataSeries("S");
        conf.addSeries(ds);
        String assigned = conf.ensureSeriesReactiveId(ds);
        assertNotNull(assigned);
        assertTrue(ChartSerialization.toJSON(ds).contains(assigned));
    }
}
