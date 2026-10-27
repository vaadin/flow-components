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

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.provider.FederatedDatabaseProvider;
import com.vaadin.flow.component.ai.tests.endpoints.SampleData;
import com.vaadin.flow.component.ai.tests.endpoints.jpa.SampleJpaDatabase;

class FederatedJpaProviderTest {

    private static final FederatedDatabaseProvider PROVIDER = FederatedSampleProvider
            .dataProviders();

    @Test
    void schema_listsEveryEntityWithItsProperties() {
        var schema = PROVIDER.getSchema();

        Assertions.assertEquals(SampleJpaDatabase.ENTITY_CLASSES.size(),
                schema.split("\nTABLE ").length - 1, schema);
        Assertions.assertTrue(
                schema.contains(
                        "TABLE Employee  (Flow data provider of Employee)\n"),
                schema);
        Assertions.assertTrue(schema.contains("  salary INTEGER\n"), schema);
        Assertions.assertTrue(schema.contains("  tradeDate DATE\n"), schema);
        Assertions.assertTrue(schema.contains("  openPrice DECIMAL\n"), schema);
    }

    @Test
    void aggregation_andDates_work() {
        var byRegion = PROVIDER.executeQuery(
                "SELECT region, SUM(revenue) AS total FROM Sale GROUP BY region ORDER BY region");
        var date = SampleData.STOCK_PRICES.get(0).tradeDate();
        var byDate = PROVIDER.executeQuery(
                "SELECT ticker, openPrice FROM StockPrice WHERE tradeDate = DATE '"
                        + date + "' ORDER BY ticker");

        var expected = SampleData.SALES.stream()
                .collect(Collectors.groupingBy(SampleData.Sale::region,
                        Collectors.summingLong(SampleData.Sale::revenue)));
        Assertions.assertEquals(expected.size(), byRegion.size());
        for (var row : byRegion) {
            Assertions.assertEquals(expected.get(row.get("region")),
                    ((Number) row.get("total")).longValue());
        }
        var expectedPrices = SampleData.STOCK_PRICES.stream()
                .filter(price -> price.tradeDate().equals(date)).toList();
        Assertions.assertEquals(expectedPrices.size(), byDate.size());
        Assertions.assertEquals(0, expectedPrices.get(0).openPrice()
                .compareTo((BigDecimal) byDate.get(0).get("openPrice")));
    }

    @Test
    void gridWrappers_pageSortAndCount() {
        var query = "SELECT name AS \"Name\", salary AS \"Salary\" FROM Employee";

        var page = PROVIDER.executeQuery("SELECT * FROM (SELECT * FROM ("
                + query
                + ") AS _t ORDER BY \"Salary\" DESC) AS _limited LIMIT 2 OFFSET 1");
        var count = PROVIDER.executeQuery(
                "SELECT COUNT(*) FROM (" + query + ") AS _counted");

        List<String> bySalary = SampleData.EMPLOYEES.stream()
                .sorted((a, b) -> Integer.compare(b.salary(), a.salary()))
                .map(SampleData.Employee::name).toList();
        Assertions.assertEquals(bySalary.subList(1, 3),
                page.stream().map(row -> row.get("Name")).toList());
        Assertions.assertEquals((long) SampleData.EMPLOYEES.size(),
                count.get(0).values().iterator().next());
    }
}
