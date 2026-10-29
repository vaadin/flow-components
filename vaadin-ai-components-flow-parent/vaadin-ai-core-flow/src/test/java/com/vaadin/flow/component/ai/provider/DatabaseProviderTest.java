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
package com.vaadin.flow.component.ai.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.common.QueryPage;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

class DatabaseProviderTest {

    private static final String QUERY = "SELECT name AS \"Name\", salary AS \"Salary\" FROM employees";

    /** Records the SQL it runs and answers with fixed rows. */
    private static class RecordingProvider implements DatabaseProvider {

        final List<String> statements = new ArrayList<>();
        List<Map<String, Object>> rows = List.of();

        @Override
        public String getSchema() {
            return "";
        }

        @Override
        public List<Map<String, Object>> executeQuery(String sql) {
            statements.add(sql);
            return rows;
        }
    }

    private RecordingProvider provider;

    @BeforeEach
    void setUp() {
        provider = new RecordingProvider();
    }

    @Test
    void page_withoutSortOrders_wrapsTheQueryInALimit() {
        provider.executeQuery(QUERY, new QueryPage(100, 50, List.of()));

        Assertions.assertEquals(
                List.of("SELECT * FROM (" + QUERY
                        + ") AS _limited LIMIT 50 OFFSET 100"),
                provider.statements);
    }

    @Test
    void page_withSortOrders_ordersByTheResultColumnsFirst() {
        provider.executeQuery(QUERY,
                new QueryPage(0, 50, List.of(
                        new QuerySortOrder("Salary", SortDirection.DESCENDING),
                        new QuerySortOrder("Name", SortDirection.ASCENDING))));

        Assertions.assertEquals(List.of("SELECT * FROM (SELECT * FROM (" + QUERY
                + ") AS _t ORDER BY \"Salary\" DESC, \"Name\" ASC) AS _limited LIMIT 50 OFFSET 0"),
                provider.statements);
    }

    @Test
    void page_quotesADoubleQuoteInAColumnName() {
        provider.executeQuery(QUERY, new QueryPage(0, 1, List.of(
                new QuerySortOrder("Say \"hi\"", SortDirection.ASCENDING))));

        Assertions.assertTrue(
                provider.statements.get(0)
                        .contains("ORDER BY \"Say \"\"hi\"\"\" ASC"),
                provider.statements.get(0));
    }

    @Test
    void page_returnsTheRowsOfTheWrappedQuery() {
        provider.rows = List.of(Map.of("Name", "Ann"));

        Assertions.assertEquals(provider.rows,
                provider.executeQuery(QUERY, QueryPage.first(1)));
    }

    @Test
    void count_wrapsTheQueryInACountAndReadsTheFirstValue() {
        provider.rows = List.of(Map.of("EXPR$0", 42L));

        Assertions.assertEquals(42, provider.count(QUERY));
        Assertions.assertEquals(
                List.of("SELECT COUNT(*) FROM (" + QUERY + ") AS _counted"),
                provider.statements);
    }

    @Test
    void count_isZeroWithoutANumber() {
        Assertions.assertEquals(0, provider.count(QUERY));
        provider.rows = List.of(Map.of());
        Assertions.assertEquals(0, provider.count(QUERY));
        provider.rows = List.of(Map.of("n", "many"));
        Assertions.assertEquals(0, provider.count(QUERY));
    }

    @Test
    void queryPage_rejectsANegativeOffsetAndANonPositiveLimit() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new QueryPage(-1, 1, List.of()));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new QueryPage(0, 0, List.of()));
        Assertions.assertEquals(List.of(),
                new QueryPage(0, 1, null).sortOrders());
    }
}
