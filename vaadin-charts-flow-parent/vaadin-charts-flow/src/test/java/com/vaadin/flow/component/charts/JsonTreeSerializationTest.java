/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataProviderSeries;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.OhlcItem;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.charts.util.ChartSerialization;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;

/**
 * Tests that {@link ChartSerialization#toJsonWithFunctions} gives the same JSON
 * as {@link ChartSerialization#toJSON}, without POJO nodes, which a mapper
 * without the chart serializers would write.
 */
class JsonTreeSerializationTest {

    @Test
    void dataSeriesItemArrays_toJsonTree_sameAsToJSON() {
        DataSeriesItem yOnly = new DataSeriesItem();
        yOnly.setY(5);
        DataSeriesItem lowHighOnly = new DataSeriesItem();
        lowHighOnly.setLow(1);
        lowHighOnly.setHigh(2);
        DataSeriesItem xOnly = new DataSeriesItem();
        xOnly.setX(3);

        DataSeries series = new DataSeries();
        series.add(new OhlcItem(1, 2, 3, 4, 5));
        series.add(new DataSeriesItem(1, 2));
        series.add(new DataSeriesItem(1, 2, 3));
        series.add(xOnly);
        series.add(yOnly);
        series.add(lowHighOnly);

        JsonNode tree = assertSameAsToJSON(series);
        Assertions.assertEquals(
                "[[1,2,3,4,5],[1,2],[1,2,3],[3,null,null],5,[1,2]]",
                tree.get("data").toString());
    }

    @Test
    void configurationWithAxesAndDataProviderSeries_toJsonTree_sameAsToJSON() {
        DataProviderSeries<Integer> dataProviderSeries = new DataProviderSeries<>(
                DataProvider.ofCollection(List.of(1, 2, 3)), value -> value);

        Configuration configuration = new Configuration();
        configuration.addxAxis(new XAxis());
        configuration.addxAxis(new XAxis());
        configuration.addyAxis(new YAxis());
        configuration.addSeries(dataProviderSeries);
        configuration.addSeries(new DataSeries(List.of(new DataSeriesItem(1, 2),
                new OhlcItem(1, 2, 3, 4, 5))));

        assertSameAsToJSON(configuration);
    }

    private static JsonNode assertSameAsToJSON(
            AbstractConfigurationObject object) {
        JsonNode tree = ChartSerialization.toJsonWithFunctions(object).json();
        assertNoPojoNode(tree);
        Assertions.assertEquals(
                JacksonUtils.readTree(ChartSerialization.toJSON(object)), tree);
        return tree;
    }

    private static void assertNoPojoNode(JsonNode node) {
        Assertions.assertFalse(node.isPojo(), () -> "POJO node: " + node);
        for (JsonNode child : node) {
            assertNoPojoNode(child);
        }
    }
}
