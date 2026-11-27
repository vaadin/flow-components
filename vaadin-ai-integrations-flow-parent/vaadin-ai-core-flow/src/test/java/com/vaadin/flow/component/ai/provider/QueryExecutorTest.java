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
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.vaadin.flow.component.ai.common.QueryPage;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

class QueryExecutorTest {

    private static final String QUERY = "SELECT name AS \"Name\", salary AS \"Salary\" FROM employees";

    private static final QueryPage FIRST_ROW = new QueryPage(0, 1, List.of());

    /** Records the SQL it runs and answers with fixed rows. */
    private static class RecordingExecutor implements QueryExecutor {

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

    private RecordingExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new RecordingExecutor();
    }

    @Test
    void page_withoutSortOrders_appendsTheLimitToTheQuery() {
        executor.executeQuery(QUERY, new QueryPage(100, 50, List.of()));

        Assertions.assertEquals(List.of(QUERY + "\nLIMIT 50 OFFSET 100"),
                executor.statements);
    }

    @ParameterizedTest
    @ValueSource(strings = { QUERY + " LIMIT 5", QUERY + " limit 5",
            QUERY + " OFFSET 5 ROWS", QUERY + " FETCH FIRST 5 ROWS ONLY",
            "SELECT a FROM (SELECT a FROM t) AS s LIMIT 5",
            "(" + QUERY + " LIMIT 5)", "SELECT TOP 2 a FROM t" })
    void page_withoutSortOrders_wrapsAQueryWithALimit(String query) {
        executor.executeQuery(query, FIRST_ROW);

        Assertions.assertEquals(
                List.of("SELECT * FROM (" + query
                        + "\n) AS _limited LIMIT 1 OFFSET 0"),
                executor.statements);
    }

    @ParameterizedTest
    @ValueSource(strings = { "SELECT a AS \"Credit Limit\" FROM t",
            "SELECT a AS `Credit Limit` FROM t",
            "SELECT a FROM t WHERE b <> 'offset'",
            "SELECT a -- the fetch\nFROM t", "SELECT a /* no\nlimit */ FROM t",
            "SELECT credit_limit FROM t", "SELECT limit2 FROM t",
            "SELECT a AS top FROM t ORDER BY top",
            "SELECT * FROM (SELECT UPPER(a) FROM t LIMIT 5) AS s ORDER BY 1",
            "(SELECT a FROM t ORDER BY a)",
            "SELECT a FROM t WHERE b <> 'it''s a limit'",
            "SELECT a AS \"a \"\"limit\"\" b\" FROM t" })
    void page_withoutSortOrders_appendsWithoutALimitOfItsOwn(String query) {
        executor.executeQuery(query, FIRST_ROW);

        Assertions.assertEquals(List.of(query + "\nLIMIT 1 OFFSET 0"),
                executor.statements);
    }

    @Test
    void page_withSortOrders_ordersByTheResultColumnsFirst() {
        executor.executeQuery(QUERY,
                new QueryPage(0, 50, List.of(
                        new QuerySortOrder("Salary", SortDirection.DESCENDING),
                        new QuerySortOrder("Name", SortDirection.ASCENDING))));

        Assertions.assertEquals(List.of("SELECT * FROM (" + QUERY
                + "\n) AS _t ORDER BY \"Salary\" DESC, \"Name\" ASC LIMIT 50 OFFSET 0"),
                executor.statements);
    }

    @Test
    void page_quotesADoubleQuoteInAColumnName() {
        executor.executeQuery(QUERY, new QueryPage(0, 1, List.of(
                new QuerySortOrder("Say \"hi\"", SortDirection.ASCENDING))));

        Assertions.assertTrue(
                executor.statements.get(0)
                        .contains("ORDER BY \"Say \"\"hi\"\"\" ASC"),
                executor.statements.get(0));
    }

    @Test
    void page_ignoresASemicolonAtTheEndOfTheQuery() {
        executor.executeQuery(QUERY, FIRST_ROW);
        executor.executeQuery(QUERY + " ;\n", FIRST_ROW);

        Assertions.assertEquals(executor.statements.get(0),
                executor.statements.get(1));
    }

    @Test
    void page_endsALineCommentAtTheEndOfTheQuery() {
        executor.executeQuery(QUERY + " -- everyone", FIRST_ROW);

        Assertions.assertEquals(
                List.of(QUERY + " -- everyone\nLIMIT 1 OFFSET 0"),
                executor.statements);
    }

    @Test
    void page_returnsTheRowsOfThePagedQuery() {
        executor.rows = List.of(Map.of("Name", "Ann"));

        Assertions.assertEquals(executor.rows,
                executor.executeQuery(QUERY, FIRST_ROW));
    }

    @Test
    void count_wrapsTheQueryInACountAndReadsTheFirstValue() {
        executor.rows = List.of(Map.of("EXPR$0", 42L));

        Assertions.assertEquals(42, executor.count(QUERY));
        Assertions.assertEquals(
                List.of("SELECT COUNT(*) FROM (" + QUERY + "\n) AS _counted"),
                executor.statements);
    }

    @Test
    void count_ignoresASemicolonAtTheEndOfTheQuery() {
        executor.count(QUERY);
        executor.count(QUERY + " ;\n");

        Assertions.assertEquals(executor.statements.get(0),
                executor.statements.get(1));
    }

    @Test
    void count_endsALineCommentAtTheEndOfTheQuery() {
        executor.count(QUERY + " -- everyone");

        Assertions
                .assertEquals(
                        List.of("SELECT COUNT(*) FROM (" + QUERY
                                + " -- everyone\n) AS _counted"),
                        executor.statements);
    }

    @Test
    void nullArguments_throw() {
        assertNullRejected(() -> executor.executeQuery(null, FIRST_ROW));
        assertNullRejected(() -> executor.executeQuery(QUERY, null));
        assertNullRejected(() -> executor.count(null));
        Assertions.assertTrue(executor.statements.isEmpty());
    }

    @Test
    void count_isZeroWithoutANumber() {
        Assertions.assertEquals(0, executor.count(QUERY));
        executor.rows = List.of(Map.of());
        Assertions.assertEquals(0, executor.count(QUERY));
        executor.rows = List.of(Map.of("n", "many"));
        Assertions.assertEquals(0, executor.count(QUERY));
    }

    /**
     * Using a {@code null} argument throws a {@link NullPointerException} too,
     * so the message is what shows that the argument was checked.
     */
    private static void assertNullRejected(Executable call) {
        var message = Assertions.assertThrows(NullPointerException.class, call)
                .getMessage();
        Assertions.assertTrue(
                message != null && message.contains("must not be null"),
                message);
    }
}
