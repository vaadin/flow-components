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
package com.vaadin.flow.component.ai.common;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

class QueryPageTest {

    @Test
    void negativeOffset_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new QueryPage(-1, 10, List.of()));
    }

    @Test
    void nonPositiveLimit_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new QueryPage(0, 0, List.of()));
    }

    @Test
    void nullSortOrders_becomeEmpty() {
        Assertions.assertEquals(List.of(),
                new QueryPage(0, 10, null).sortOrders());
    }

    @Test
    void first_startsAtZeroWithoutSort() {
        var page = QueryPage.first(5);
        Assertions.assertEquals(0, page.offset());
        Assertions.assertEquals(5, page.limit());
        Assertions.assertTrue(page.sortOrders().isEmpty());
    }

    @Test
    void apply_slicesRows() {
        var rows = rows(1, 2, 3, 4, 5);
        Assertions.assertEquals(rows(2, 3),
                new QueryPage(1, 2, List.of()).apply(rows));
        Assertions.assertEquals(rows(5),
                new QueryPage(4, 10, List.of()).apply(rows));
        Assertions.assertEquals(List.of(),
                new QueryPage(7, 10, List.of()).apply(rows));
    }

    @Test
    void apply_sortsAscendingAndDescending() {
        var rows = rows(3, 1, 2);
        Assertions.assertEquals(rows(1, 2, 3),
                new QueryPage(0, 10, List.of(ascending("v"))).apply(rows));
        Assertions.assertEquals(rows(3, 2, 1),
                new QueryPage(0, 10, List.of(descending("v"))).apply(rows));
    }

    @Test
    void apply_sortsByEveryOrderInTurn() {
        var rows = List.of(row("a", "x", "b", 2), row("a", "y", "b", 1),
                row("a", "x", "b", 1));
        var sorted = new QueryPage(0, 10,
                List.of(ascending("a"), descending("b"))).apply(rows);
        Assertions.assertEquals(List.of(row("a", "x", "b", 2),
                row("a", "x", "b", 1), row("a", "y", "b", 1)), sorted);
    }

    @Test
    void apply_putsNullsLastInEitherDirection() {
        var rows = List.of(row("v", null), row("v", 2), row("v", 1));
        Assertions.assertEquals(
                List.of(row("v", 1), row("v", 2), row("v", null)),
                new QueryPage(0, 10, List.of(ascending("v"))).apply(rows));
        Assertions.assertEquals(
                List.of(row("v", 2), row("v", 1), row("v", null)),
                new QueryPage(0, 10, List.of(descending("v"))).apply(rows));
    }

    @Test
    void apply_comparesMixedNumbersAsNumbers() {
        var rows = List.of(row("v", 10L), row("v", new BigDecimal("2.5")),
                row("v", 3), row("v", 0.5));
        var sorted = new QueryPage(0, 10, List.of(ascending("v"))).apply(rows);
        Assertions.assertEquals(List.of(0.5, new BigDecimal("2.5"), 3, 10L),
                sorted.stream().map(r -> r.get("v")).toList());
    }

    @Test
    void apply_comparesOtherValuesAsText() {
        var rows = List.of(row("v", "b"), row("v", 10), row("v", "a"));
        var sorted = new QueryPage(0, 10, List.of(ascending("v"))).apply(rows);
        Assertions.assertEquals(List.of(10, "a", "b"),
                sorted.stream().map(r -> r.get("v")).toList());
    }

    @Test
    void apply_doesNotModifyTheInput() {
        var rows = new ArrayList<>(rows(2, 1));
        new QueryPage(0, 1, List.of(ascending("v"))).apply(rows);
        Assertions.assertEquals(rows(2, 1), rows);
    }

    private static QuerySortOrder ascending(String column) {
        return new QuerySortOrder(column, SortDirection.ASCENDING);
    }

    private static QuerySortOrder descending(String column) {
        return new QuerySortOrder(column, SortDirection.DESCENDING);
    }

    private static List<Map<String, Object>> rows(int... values) {
        var rows = new ArrayList<Map<String, Object>>();
        for (var value : values) {
            rows.add(row("v", value));
        }
        return rows;
    }

    private static Map<String, Object> row(Object... keysAndValues) {
        var map = new LinkedHashMap<String, Object>();
        for (var i = 0; i < keysAndValues.length; i += 2) {
            map.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }
}
