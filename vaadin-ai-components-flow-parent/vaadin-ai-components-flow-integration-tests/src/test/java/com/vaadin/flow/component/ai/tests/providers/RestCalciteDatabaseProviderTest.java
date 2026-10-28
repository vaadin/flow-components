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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.provider.ToolException;
import com.vaadin.flow.component.ai.tests.endpoints.SampleApiServer;
import com.vaadin.flow.component.ai.tests.endpoints.SampleData;

class RestCalciteDatabaseProviderTest {

    private static final RestCalciteDatabaseProvider PROVIDER = new RestCalciteDatabaseProvider(
            SampleApiServer.get().restUri(), "- custom note\n");

    @Test
    void schema_listsEveryCollectionWithTypesAndQuotedReservedWords() {
        var schema = PROVIDER.getSchema();

        Assertions.assertEquals(SampleData.TABLES.size(),
                schema.split("\nTABLE ").length - 1, schema);
        Assertions.assertTrue(
                schema.contains("TABLE sales  (REST collection /api/sales)\n"),
                schema);
        Assertions.assertTrue(schema.contains("  \"month\" VARCHAR\n"), schema);
        Assertions.assertTrue(schema.contains("  monthOrder INTEGER\n"),
                schema);
        Assertions.assertTrue(schema.contains("  tradeDate DATE\n"), schema);
        Assertions.assertTrue(schema.contains("  openPrice DECIMAL\n"), schema);
        Assertions.assertTrue(schema.endsWith("- custom note\n"), schema);
    }

    @Test
    void select_returnsRowsWithLabelsAsWritten() {
        var rows = PROVIDER.executeQuery(
                "SELECT productName AS \"Product\", price FROM products ORDER BY id");

        Assertions.assertEquals(SampleData.PRODUCTS.size(), rows.size());
        Assertions.assertEquals(List.of("Product", "price"),
                new ArrayList<>(rows.get(0).keySet()));
        Assertions.assertEquals(SampleData.PRODUCTS.get(0).productName(),
                rows.get(0).get("Product"));
        Assertions.assertEquals(0, SampleData.PRODUCTS.get(0).price()
                .compareTo((BigDecimal) rows.get(0).get("price")));
    }

    @Test
    void filter_aggregation_andDates_work() {
        var north = PROVIDER.executeQuery(
                "SELECT SUM(revenue) AS total FROM sales WHERE region = 'North'");
        var byRegion = PROVIDER.executeQuery(
                "SELECT region, COUNT(*) AS n FROM sales GROUP BY region ORDER BY region");
        var date = SampleData.STOCK_PRICES.get(0).tradeDate();
        var byDate = PROVIDER.executeQuery(
                "SELECT ticker, tradeDate FROM stockPrices WHERE tradeDate = DATE '"
                        + date + "' ORDER BY ticker");

        var expectedNorth = SampleData.SALES.stream()
                .filter(sale -> sale.region().equals("North"))
                .mapToLong(SampleData.Sale::revenue).sum();
        Assertions.assertEquals(expectedNorth,
                ((Number) north.get(0).get("total")).longValue());
        var expectedByRegion = SampleData.SALES.stream().collect(Collectors
                .groupingBy(SampleData.Sale::region, Collectors.counting()));
        Assertions.assertEquals(expectedByRegion.size(), byRegion.size());
        for (var row : byRegion) {
            Assertions.assertEquals(expectedByRegion.get(row.get("region")),
                    row.get("n"));
        }
        Assertions.assertEquals(SampleData.STOCK_PRICES.stream()
                .filter(price -> price.tradeDate().equals(date)).count(),
                byDate.size());
        Assertions.assertEquals(date, byDate.get(0).get("tradeDate"));
    }

    @Test
    void stringLiteralsOfAUnion_joinWithTableValues() {
        var rows = PROVIDER.executeQuery(
                "SELECT COUNT(*) AS n FROM sales s JOIN (SELECT 'North' AS r UNION ALL SELECT 'Central' AS r) t ON t.r = s.region");

        Assertions.assertEquals(
                SampleData.SALES.stream()
                        .filter(sale -> sale.region().equals("North")).count(),
                ((Number) rows.get(0).get("n")).longValue());
    }

    @Test
    void gridWrappers_pageSortAndCount() {
        var query = "SELECT name AS \"Name\", salary AS \"Salary\" FROM employees";

        var sample = PROVIDER
                .executeQuery("SELECT * FROM (" + query + ") AS _v LIMIT 1");
        var page = PROVIDER.executeQuery("SELECT * FROM (SELECT * FROM ("
                + query
                + ") AS _t ORDER BY \"Salary\" DESC) AS _limited LIMIT 2 OFFSET 1");
        var count = PROVIDER.executeQuery(
                "SELECT COUNT(*) FROM (" + query + ") AS _counted");

        Assertions.assertEquals(1, sample.size());
        var bySalary = SampleData.EMPLOYEES.stream()
                .sorted((a, b) -> Integer.compare(b.salary(), a.salary()))
                .map(SampleData.Employee::name).toList();
        Assertions.assertEquals(bySalary.subList(1, 3),
                page.stream().map(row -> row.get("Name")).toList());
        Assertions.assertEquals((long) SampleData.EMPLOYEES.size(),
                count.get(0).values().iterator().next());
    }

    @Test
    void errors_reachTheLlmOnlyWhenSafe() {
        var unknownColumn = Assertions.assertThrows(ToolException.class,
                () -> PROVIDER.executeQuery("SELECT foo FROM sales"));
        var syntax = Assertions.assertThrows(ToolException.class,
                () -> PROVIDER.executeQuery("SELEC region FROM sales"));
        var write = Assertions.assertThrows(ToolException.class,
                () -> PROVIDER.executeQuery("DELETE FROM sales"));

        Assertions.assertTrue(unknownColumn.getMessage().contains("foo"),
                unknownColumn.getMessage());
        Assertions.assertFalse(syntax.getMessage().contains("Was expecting"),
                syntax.getMessage());
        Assertions.assertEquals("Only SELECT queries are allowed.",
                write.getMessage());
    }

    @Test
    void provider_worksAfterSerialization()
            throws IOException, ClassNotFoundException {
        var bytes = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(bytes)) {
            out.writeObject(PROVIDER);
        }
        RestCalciteDatabaseProvider copy;
        try (var in = new ObjectInputStream(
                new ByteArrayInputStream(bytes.toByteArray()))) {
            copy = (RestCalciteDatabaseProvider) in.readObject();
        }

        var rows = copy.executeQuery("SELECT COUNT(*) AS n FROM sales");

        Assertions.assertEquals((long) SampleData.SALES.size(),
                rows.get(0).get("n"));
    }
}
