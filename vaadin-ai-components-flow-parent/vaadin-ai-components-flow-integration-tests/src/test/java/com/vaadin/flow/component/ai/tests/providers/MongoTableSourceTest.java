/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.ai.tests.providers;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.provider.FederatedDatabaseProvider;
import com.vaadin.flow.component.ai.provider.TableSource.Capability;
import com.vaadin.flow.component.ai.provider.TableSource.Column;
import com.vaadin.flow.component.ai.provider.TableSource.Pushdown;
import com.vaadin.flow.component.ai.tests.endpoints.SampleData;
import com.vaadin.flow.component.ai.tests.endpoints.SampleMongoServer;

class MongoTableSourceTest {

    private static final FederatedDatabaseProvider PROVIDER = FederatedSampleProvider
            .mongo();

    private static MongoTableSource source(String collection) {
        var server = SampleMongoServer.get();
        return new MongoTableSource(server.connectionString(),
                SampleMongoServer.DATABASE_NAME, collection);
    }

    private static Map<String, Class<?>> columnTypes(String collection) {
        return source(collection).getColumns().stream()
                .collect(Collectors.toMap(Column::name, Column::type));
    }

    @Test
    void discover_createsOneSourcePerCollection() {
        var server = SampleMongoServer.get();

        var sources = MongoTableSource.discover(server.connectionString(),
                SampleMongoServer.DATABASE_NAME);

        Assertions.assertEquals(SampleData.TABLES.size(), sources.size());
        Assertions.assertTrue(sources.stream().map(MongoTableSource::getName)
                .anyMatch("kpiMetrics"::equals));
        Assertions.assertEquals("MongoDB collection kpiMetrics",
                source("kpiMetrics").getDescription());
        Assertions.assertEquals(Set.of(Capability.EQUALITY_FILTER),
                source("kpiMetrics").getCapabilities());
    }

    @Test
    void columns_areInferredFromTheDocuments_withoutNestedOnes() {
        var metrics = columnTypes("kpiMetrics");
        var prices = columnTypes("stockPrices");
        var chart = columnTypes("orgChart");

        Assertions.assertEquals(Integer.class, metrics.get("id"));
        Assertions.assertEquals(String.class, metrics.get("metricName"));
        Assertions.assertEquals(Double.class, metrics.get("currentVal"));
        Assertions.assertEquals(LocalDate.class, prices.get("tradeDate"));
        Assertions.assertEquals(Integer.class, prices.get("volume"));
        Assertions.assertFalse(prices.containsKey("prices"));
        Assertions.assertFalse(chart.containsKey("manager"));
        Assertions.assertEquals(String.class, chart.get("empName"));
    }

    @Test
    void fetch_returnsTypedRows() {
        var rows = source("stockPrices").fetch(Pushdown.NONE);

        Assertions.assertEquals(SampleData.STOCK_PRICES.size(), rows.size());
        var first = SampleData.STOCK_PRICES.get(0);
        Assertions.assertEquals(first.id(), rows.get(0).get("id"));
        Assertions.assertEquals(first.ticker(), rows.get(0).get("ticker"));
        Assertions.assertEquals(first.tradeDate(),
                rows.get(0).get("tradeDate"));
        Assertions.assertEquals(first.volume(), rows.get(0).get("volume"));
    }

    @Test
    void fetch_pushesEqualityFiltersIncludingDatesAndIds() {
        var date = SampleData.STOCK_PRICES.get(0).tradeDate();

        var byDate = source("stockPrices")
                .fetch(new Pushdown(Map.of("tradeDate", date)));
        var byId = source("kpiMetrics").fetch(new Pushdown(Map.of("id", 2)));

        var expected = SampleData.STOCK_PRICES.stream()
                .filter(price -> price.tradeDate().equals(date)).count();
        Assertions.assertEquals(expected, byDate.size());
        Assertions.assertTrue(expected < SampleData.STOCK_PRICES.size());
        Assertions.assertEquals(1, byId.size());
        Assertions.assertEquals(SampleData.KPI_METRICS.get(1).metricName(),
                byId.get(0).get("metricName"));
    }

    @Test
    void schema_listsTheColumns() {
        var schema = PROVIDER.getSchema();

        Assertions.assertTrue(
                schema.contains(
                        "TABLE kpiMetrics  (MongoDB collection kpiMetrics)\n"),
                schema);
        Assertions.assertTrue(schema.contains("  currentVal DOUBLE\n"), schema);
        Assertions.assertTrue(schema.contains("  tradeDate DATE\n"), schema);
        Assertions.assertEquals(SampleData.TABLES.size(),
                schema.split("\nTABLE ").length - 1, schema);
    }

    @Test
    void queries_filterByDate_applyFunctions_andAggregate() {
        var date = SampleData.STOCK_PRICES.get(0).tradeDate();

        var byDate = PROVIDER.executeQuery(
                "SELECT ticker FROM stockPrices WHERE tradeDate = DATE '" + date
                        + "' ORDER BY ticker");
        var upper = PROVIDER.executeQuery(
                "SELECT UPPER(metricName) AS m FROM kpiMetrics ORDER BY id");
        var total = PROVIDER.executeQuery(
                "SELECT COUNT(*) AS n, SUM(amount) AS total FROM energyFlow");

        var expectedByDate = SampleData.STOCK_PRICES.stream()
                .filter(price -> price.tradeDate().equals(date)).count();
        Assertions.assertEquals(expectedByDate, byDate.size());
        Assertions.assertEquals(
                SampleData.KPI_METRICS.get(0).metricName().toUpperCase(),
                upper.get(0).get("m"));
        Assertions.assertEquals((long) SampleData.ENERGY_FLOW.size(),
                total.get(0).get("n"));
        var expectedTotal = SampleData.ENERGY_FLOW.stream()
                .mapToDouble(flow -> flow.amount().doubleValue()).sum();
        Assertions.assertEquals(expectedTotal,
                ((Number) total.get(0).get("total")).doubleValue(), 0.001);
    }

    @Test
    void gridWrappers_pageSortAndCount() {
        var query = "SELECT itemName AS \"Item\", amount AS \"Amount\" FROM budgetItems";

        var page = PROVIDER.executeQuery("SELECT * FROM (SELECT * FROM ("
                + query
                + ") AS _t ORDER BY \"Amount\" DESC) AS _limited LIMIT 2 OFFSET 1");
        var count = PROVIDER.executeQuery(
                "SELECT COUNT(*) FROM (" + query + ") AS _counted");

        List<String> byAmount = SampleData.BUDGET_ITEMS.stream()
                .sorted((a, b) -> Integer.compare(b.amount(), a.amount()))
                .map(SampleData.BudgetItem::itemName).toList();
        Assertions.assertEquals(byAmount.subList(1, 3),
                page.stream().map(row -> row.get("Item")).toList());
        Assertions.assertEquals((long) SampleData.BUDGET_ITEMS.size(),
                count.get(0).values().iterator().next());
    }
}
