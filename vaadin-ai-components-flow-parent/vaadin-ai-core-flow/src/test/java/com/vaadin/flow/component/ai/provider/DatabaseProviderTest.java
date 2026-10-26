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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.common.QueryLanguage;
import com.vaadin.flow.component.ai.common.QueryPage;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

class DatabaseProviderTest {

    private final List<String> executed = new ArrayList<>();
    private List<Map<String, Object>> results = List.of();
    private RuntimeException failure;

    private final DatabaseProvider provider = new DatabaseProvider() {
        @Override
        public String getSchema() {
            return "schema";
        }

        @Override
        public List<Map<String, Object>> executeQuery(String sql) {
            executed.add(sql);
            if (failure != null) {
                throw failure;
            }
            return results;
        }
    };

    @Test
    void queryLanguage_isSql() {
        Assertions.assertSame(QueryLanguage.SQL, provider.getQueryLanguage());
    }

    @Test
    void pagedExecute_firstRow_wrapsWithLimitOne() {
        provider.executeQuery("SELECT a FROM t", QueryPage.first(1));
        Assertions.assertEquals(List.of(
                "SELECT * FROM (SELECT a FROM t) AS _limited LIMIT 1 OFFSET 0"),
                executed);
    }

    @Test
    void pagedExecute_wrapsWithLimitAndOffset() {
        provider.executeQuery("SELECT a FROM t",
                new QueryPage(20, 10, List.of()));
        Assertions.assertEquals(List.of(
                "SELECT * FROM (SELECT a FROM t) AS _limited LIMIT 10 OFFSET 20"),
                executed);
    }

    @Test
    void pagedExecute_withSortOrder_wrapsWithOrderBy() {
        provider.executeQuery("SELECT a FROM t", new QueryPage(0, 50,
                List.of(new QuerySortOrder("a", SortDirection.DESCENDING))));
        Assertions.assertEquals(List.of(
                "SELECT * FROM (SELECT * FROM (SELECT a FROM t) AS _t ORDER BY \"a\" DESC) AS _limited LIMIT 50 OFFSET 0"),
                executed);
    }

    @Test
    void pagedExecute_withSeveralSortOrders_ordersByAll() {
        provider.executeQuery("SELECT a FROM t",
                new QueryPage(0, 10, List.of(
                        new QuerySortOrder("a", SortDirection.ASCENDING),
                        new QuerySortOrder("b", SortDirection.DESCENDING))));
        Assertions.assertTrue(
                executed.getFirst().contains("ORDER BY \"a\" ASC, \"b\" DESC"),
                executed.getFirst());
    }

    @Test
    void pagedExecute_returnsTheRowsOfTheWrappedQuery() {
        results = List.of(Map.of("a", 10), Map.of("a", 20));
        Assertions.assertEquals(results,
                provider.executeQuery("SELECT a FROM t", QueryPage.first(5)));
    }

    @Test
    void pagedExecute_relaysToolException() {
        failure = new ToolException("Unknown column");
        var e = Assertions.assertThrows(ToolException.class, () -> provider
                .executeQuery("SELECT a FROM t", QueryPage.first(1)));
        Assertions.assertEquals("Unknown column", e.getMessage());
    }

    @Test
    void count_wrapsQueryWithCount() {
        results = List.of(Map.of("COUNT(*)", 42));
        Assertions.assertEquals(OptionalInt.of(42),
                provider.count("SELECT a FROM t"));
        Assertions.assertEquals(
                List.of("SELECT COUNT(*) FROM (SELECT a FROM t) AS _counted"),
                executed);
    }

    @Test
    void count_emptyResult_isZero() {
        results = List.of();
        Assertions.assertEquals(OptionalInt.of(0),
                provider.count("SELECT a FROM t"));
    }

    @Test
    void count_nonNumericValue_isZero() {
        results = List.of(Map.of("COUNT(*)", "not a number"));
        Assertions.assertEquals(OptionalInt.of(0),
                provider.count("SELECT a FROM t"));
    }

    @Test
    void count_rowWithoutColumns_isZero() {
        results = List.of(new LinkedHashMap<>());
        Assertions.assertEquals(OptionalInt.of(0),
                provider.count("SELECT a FROM t"));
    }
}
