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
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.provider.TableSource.Column;
import com.vaadin.flow.component.ai.provider.TableSource.Pushdown;
import com.vaadin.flow.component.ai.provider.TableSource.SortKey;
import com.vaadin.flow.component.ai.provider.ToolException;
import com.vaadin.flow.component.ai.tests.endpoints.SampleApiServer;
import com.vaadin.flow.component.ai.tests.endpoints.SampleData;

class RestTableSourceTest {

    private final RestTableSource sales = RestTableSource
            .of(SampleApiServer.get().restUri(), "sales");

    @Test
    void discover_createsOneSourcePerCollection() {
        var sources = RestTableSource.discover(SampleApiServer.get().restUri());

        Assertions.assertEquals(SampleData.TABLES.size(), sources.size());
        Assertions.assertEquals("sales", sources.get(0).getName());
        Assertions.assertEquals("REST collection /api/sales",
                sources.get(0).getDescription());
    }

    @Test
    void columns_areInferredFromTheValues() {
        Assertions.assertEquals(
                List.of(new Column("id", Integer.class),
                        new Column("month", String.class),
                        new Column("monthOrder", Integer.class),
                        new Column("revenue", Integer.class),
                        new Column("region", String.class)),
                sales.getColumns());

        var stockPrices = RestTableSource.of(SampleApiServer.get().restUri(),
                "stockPrices");
        var columns = stockPrices.getColumns().stream()
                .collect(Collectors.toMap(Column::name, Column::type));
        Assertions.assertEquals(LocalDate.class, columns.get("tradeDate"));
        Assertions.assertEquals(BigDecimal.class, columns.get("openPrice"));
        Assertions.assertEquals(Integer.class, columns.get("volume"));
    }

    @Test
    void fetch_returnsEveryRowAcrossPagesAsTypedValues() {
        var rows = sales.fetch(Pushdown.NONE);

        Assertions.assertEquals(SampleData.SALES.size(), rows.size());
        var first = SampleData.SALES.get(0);
        Assertions.assertEquals(first.month(), rows.get(0).get("month"));
        Assertions.assertEquals(first.revenue(), rows.get(0).get("revenue"));
        Assertions.assertEquals(first.region(), rows.get(0).get("region"));

        var stockPrices = RestTableSource
                .of(SampleApiServer.get().restUri(), "stockPrices")
                .fetch(Pushdown.NONE);
        Assertions.assertEquals(SampleData.STOCK_PRICES.size(),
                stockPrices.size());
        Assertions.assertEquals(SampleData.STOCK_PRICES.get(0).tradeDate(),
                stockPrices.get(0).get("tradeDate"));
        Assertions.assertEquals(0, SampleData.STOCK_PRICES.get(0).openPrice()
                .compareTo((BigDecimal) stockPrices.get(0).get("openPrice")));
    }

    @Test
    void fetch_pushesEqualityFiltersToTheApi() {
        var rows = sales.fetch(new Pushdown(Map.of("region", "North")));

        var expected = SampleData.SALES.stream()
                .filter(sale -> sale.region().equals("North")).count();
        Assertions.assertEquals(expected, rows.size());
        Assertions.assertTrue(expected < SampleData.SALES.size());
        Assertions.assertTrue(rows.stream()
                .allMatch(row -> "North".equals(row.get("region"))));
    }

    @Test
    void fetch_readsASortedPageThatSpansTwoPagesOfTheApi() {
        var rows = sales.fetch(new Pushdown(Map.of(),
                List.of(new SortKey("revenue", true)), 3, 4L));

        var expected = SampleData.SALES.stream()
                .sorted(Comparator.comparing(SampleData.Sale::revenue)
                        .reversed())
                .skip(3).limit(4).map(SampleData.Sale::revenue).toList();
        Assertions.assertEquals(expected,
                rows.stream().map(row -> row.get("revenue")).toList());
    }

    @Test
    void fetch_appliesFiltersBeforeThePage() {
        var rows = sales.fetch(new Pushdown(Map.of("region", "South"),
                List.of(new SortKey("monthOrder", false)), 0, 2L));

        Assertions.assertEquals(List.of(1, 2),
                rows.stream().map(row -> row.get("monthOrder")).toList());
        Assertions.assertTrue(rows.stream()
                .allMatch(row -> "South".equals(row.get("region"))));
    }

    @Test
    void count_readsTheTotalOfTheApi() {
        Assertions.assertEquals(SampleData.SALES.size(),
                sales.count(Pushdown.NONE));
        Assertions.assertEquals(
                SampleData.SALES.stream()
                        .filter(sale -> sale.region().equals("North")).count(),
                sales.count(new Pushdown(Map.of("region", "North"))));
    }

    @Test
    void fetch_relaysTheApisRejectionOfAnUnknownField() {
        var exception = Assertions.assertThrows(ToolException.class,
                () -> sales.fetch(new Pushdown(Map.of("colour", "red"))));

        Assertions.assertTrue(exception.getMessage().contains("colour"),
                exception.getMessage());
    }
}
