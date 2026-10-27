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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.provider.FederatedDatabaseProvider;
import com.vaadin.flow.component.ai.tests.endpoints.SampleData;

class FederatedSampleProviderTest {

    private static final FederatedDatabaseProvider PROVIDER = FederatedSampleProvider
            .federated();

    @Test
    void schema_listsEveryTableOnce_fromItsEndpoint() {
        var schema = PROVIDER.getSchema();

        Assertions.assertTrue(
                schema.contains("TABLE sales  (REST collection /api/sales)"),
                schema);
        Assertions.assertTrue(
                schema.contains("TABLE employees  (GraphQL field employees)"),
                schema);
        Assertions.assertTrue(
                schema.contains(
                        "TABLE kpiMetrics  (MongoDB collection kpiMetrics)"),
                schema);
        Assertions.assertTrue(
                schema.contains("TABLE org_chart  (CSV file org_chart.csv)"),
                schema);
        Assertions.assertEquals(SampleData.TABLES.size(),
                schema.split("\nTABLE ").length - 1, schema);
        Assertions.assertTrue(schema.contains("  \"month\" VARCHAR"), schema);
    }

    @Test
    void everyEndpointAlone_servesTheWholeDataSet() {
        for (var provider : List.of(FederatedSampleProvider.rest(),
                FederatedSampleProvider.csv(),
                FederatedSampleProvider.graphql(),
                FederatedSampleProvider.mongo())) {
            var schema = provider.getSchema();
            Assertions.assertEquals(SampleData.TABLES.size(),
                    schema.split("\nTABLE ").length - 1, schema);
        }
    }

    @Test
    void aggregation_overTheRestCollection() {
        var rows = PROVIDER.executeQuery(
                "SELECT region, SUM(revenue) AS total FROM sales GROUP BY region ORDER BY region");

        var expected = SampleData.SALES.stream()
                .collect(Collectors.groupingBy(SampleData.Sale::region,
                        Collectors.summingLong(SampleData.Sale::revenue)));
        Assertions.assertEquals(expected.size(), rows.size());
        for (var row : rows) {
            Assertions.assertEquals(expected.get(row.get("region")),
                    ((Number) row.get("total")).longValue());
        }
    }

    @Test
    void join_acrossTheRestAndMongoEndpoints() {
        var rows = PROVIDER.executeQuery("""
                SELECT p.productName AS "Product", k.metricName AS "Metric"
                FROM products p JOIN kpiMetrics k ON k.id = p.id
                ORDER BY p.id""");

        Assertions.assertEquals(Math.min(SampleData.PRODUCTS.size(),
                SampleData.KPI_METRICS.size()), rows.size());
        Assertions.assertEquals(SampleData.PRODUCTS.get(0).productName(),
                rows.get(0).get("Product"));
        Assertions.assertEquals(SampleData.KPI_METRICS.get(0).metricName(),
                rows.get(0).get("Metric"));
    }

    @Test
    void join_acrossTheGraphQLAndCsvEndpoints() {
        var rows = PROVIDER.executeQuery("""
                SELECT e.name AS "Employee", s.stage AS "Stage"
                FROM employees e JOIN sales_pipeline s ON s.id = e.id
                ORDER BY e.id""");

        Assertions.assertEquals(Math.min(SampleData.EMPLOYEES.size(),
                SampleData.SALES_PIPELINE.size()), rows.size());
        Assertions.assertEquals(SampleData.EMPLOYEES.get(0).name(),
                rows.get(0).get("Employee"));
        Assertions.assertEquals(SampleData.SALES_PIPELINE.get(0).stage(),
                rows.get(0).get("Stage"));
    }

    @Test
    void gridWrappers_pageAndCountTheGraphQLTable() {
        var query = "SELECT name AS \"Name\", salary AS \"Salary\" FROM employees";
        var page = PROVIDER.executeQuery("SELECT * FROM (SELECT * FROM ("
                + query
                + ") AS _t ORDER BY \"Salary\" DESC) AS _limited LIMIT 2 OFFSET 1");
        var count = PROVIDER.executeQuery(
                "SELECT COUNT(*) FROM (" + query + ") AS _counted");

        var bySalary = SampleData.EMPLOYEES.stream()
                .sorted((a, b) -> Integer.compare(b.salary(), a.salary()))
                .map(SampleData.Employee::name).toList();
        Assertions.assertEquals(bySalary.subList(1, 3),
                page.stream().map(row -> row.get("Name")).toList());
        Assertions.assertEquals((long) SampleData.EMPLOYEES.size(),
                count.get(0).values().iterator().next());
    }

    @Test
    void dateColumns_compareWithDateLiterals() {
        var rows = PROVIDER.executeQuery(
                "SELECT ticker, tradeDate FROM stockPrices WHERE tradeDate = DATE '"
                        + SampleData.STOCK_PRICES.get(0).tradeDate()
                        + "' ORDER BY ticker");

        var expected = SampleData.STOCK_PRICES.stream()
                .filter(price -> price.tradeDate()
                        .equals(SampleData.STOCK_PRICES.get(0).tradeDate()))
                .count();
        Assertions.assertEquals(expected, rows.size());
        Assertions.assertEquals(SampleData.STOCK_PRICES.get(0).tradeDate(),
                rows.get(0).get("tradeDate"));
    }

    @Test
    void reservedWordColumn_worksQuoted() {
        List<Map<String, Object>> rows = PROVIDER.executeQuery(
                "SELECT \"month\", revenue FROM sales WHERE monthOrder = 1");

        Assertions.assertFalse(rows.isEmpty());
        Assertions.assertEquals("January", rows.get(0).get("month"));
    }
}
