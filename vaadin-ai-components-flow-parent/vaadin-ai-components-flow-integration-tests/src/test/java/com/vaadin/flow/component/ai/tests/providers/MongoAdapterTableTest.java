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
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.provider.FederatedDatabaseProvider;
import com.vaadin.flow.component.ai.provider.TableSource.Column;
import com.vaadin.flow.component.ai.provider.ToolException;
import com.vaadin.flow.component.ai.tests.endpoints.SampleData;
import com.vaadin.flow.component.ai.tests.endpoints.SampleMongoServer;

class MongoAdapterTableTest {

    private static final FederatedDatabaseProvider PROVIDER = FederatedSampleProvider
            .mongo();

    private static Map<String, Class<?>> columnTypes(String collection) {
        var server = SampleMongoServer.get();
        return new MongoAdapterTable(server.connectionString(),
                SampleMongoServer.DATABASE_NAME, collection).getColumns()
                .stream().collect(Collectors.toMap(Column::name, Column::type));
    }

    @Test
    void discover_createsOneTablePerCollection() {
        var server = SampleMongoServer.get();

        var tables = MongoAdapterTable.discover(server.connectionString(),
                SampleMongoServer.DATABASE_NAME);

        Assertions.assertEquals(SampleData.TABLES.size(), tables.size());
        Assertions.assertTrue(tables.stream().map(MongoAdapterTable::getName)
                .anyMatch("kpiMetrics"::equals));
        Assertions.assertEquals("MongoDB collection kpiMetrics",
                tables.stream()
                        .filter(table -> table.getName().equals("kpiMetrics"))
                        .findFirst().orElseThrow().getDescription());
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
    void schema_listsTheViewColumns() {
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
    void rows_comeThroughTheAdapterTyped() {
        var rows = PROVIDER.executeQuery(
                "SELECT metricName, currentVal FROM kpiMetrics ORDER BY id");

        Assertions.assertEquals(SampleData.KPI_METRICS.size(), rows.size());
        var first = SampleData.KPI_METRICS.get(0);
        Assertions.assertEquals(first.metricName(),
                rows.get(0).get("metricName"));
        Assertions.assertEquals(first.currentVal().doubleValue(),
                rows.get(0).get("currentVal"));
    }

    @Test
    void filter_isPushedIntoThePipeline() {
        var metric = SampleData.KPI_METRICS.get(1);
        var sql = "SELECT metricName, targetVal FROM kpiMetrics WHERE metricName = '"
                + metric.metricName() + "'";

        var rows = PROVIDER.executeQuery(sql);
        var plan = PROVIDER.explain(sql);

        Assertions.assertEquals(1, rows.size());
        Assertions.assertEquals(metric.targetVal().doubleValue(),
                rows.get(0).get("targetVal"));
        Assertions.assertTrue(plan.contains("MongoFilter"), plan);
        Assertions.assertTrue(plan.contains("MongoToEnumerableConverter"),
                plan);
    }

    @Test
    void dateValues_andAggregates_work() {
        var acme = PROVIDER.executeQuery(
                "SELECT ticker, tradeDate FROM stockPrices WHERE ticker = 'ACME' ORDER BY tradeDate");
        var total = PROVIDER.executeQuery(
                "SELECT COUNT(*) AS n, SUM(amount) AS total FROM energyFlow");

        var expectedFirst = SampleData.STOCK_PRICES.stream()
                .filter(price -> price.ticker().equals("ACME"))
                .map(SampleData.StockPrice::tradeDate).sorted().findFirst()
                .orElseThrow();
        Assertions.assertEquals(expectedFirst, acme.get(0).get("tradeDate"));
        Assertions.assertEquals((long) SampleData.ENERGY_FLOW.size(),
                total.get(0).get("n"));
        var expectedTotal = SampleData.ENERGY_FLOW.stream()
                .mapToDouble(flow -> flow.amount().doubleValue()).sum();
        Assertions.assertEquals(expectedTotal,
                ((Number) total.get(0).get("total")).doubleValue(), 0.001);
    }

    @Test
    void dateFilter_isPushedButMatchesNothing_adapterLimitation() {
        var date = SampleData.STOCK_PRICES.get(0).tradeDate();
        var sql = "SELECT ticker FROM stockPrices WHERE tradeDate = DATE '"
                + date + "'";

        var plan = PROVIDER.explain(sql);
        var rows = PROVIDER.executeQuery(sql);

        Assertions.assertTrue(plan.contains("MongoFilter"), plan);
        Assertions.assertEquals(List.of(), rows);
    }

    @Test
    void functionTheAdapterCannotPush_isReportedToTheLlm() {
        var exception = Assertions.assertThrows(ToolException.class,
                () -> PROVIDER.executeQuery(
                        "SELECT UPPER(metricName) AS m FROM kpiMetrics"));

        Assertions.assertTrue(
                exception.getMessage().contains("cannot evaluate"),
                exception.getMessage());
    }

    @Test
    void gridWrappers_pageSortAndCount() {
        var query = "SELECT itemName AS \"Item\", amount AS \"Amount\" FROM budgetItems";

        var wrapped = "SELECT * FROM (SELECT * FROM (" + query
                + ") AS _t ORDER BY \"Amount\" DESC) AS _limited LIMIT 2 OFFSET 1";
        var page = PROVIDER.executeQuery(wrapped);
        var plan = PROVIDER.explain(wrapped);
        var count = PROVIDER.executeQuery(
                "SELECT COUNT(*) FROM (" + query + ") AS _counted");

        List<String> byAmount = SampleData.BUDGET_ITEMS.stream()
                .sorted((a, b) -> Integer.compare(b.amount(), a.amount()))
                .map(SampleData.BudgetItem::itemName).toList();
        Assertions.assertEquals(byAmount.subList(1, 3),
                page.stream().map(row -> row.get("Item")).toList());
        Assertions.assertTrue(
                plan.contains("dir0=[DESC], offset=[1], fetch=[2]"), plan);
        Assertions.assertEquals((long) SampleData.BUDGET_ITEMS.size(),
                count.get(0).values().iterator().next());
    }
}
