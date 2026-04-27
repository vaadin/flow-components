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
package com.vaadin.flow.component.ai.grid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;

class GridAIControllerTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Grid<AIDataRow> grid;
    private StubDatabaseProvider dbProvider;
    private GridAIController controller;

    @BeforeEach
    void setUp() {
        grid = new Grid<>();
        ui.add(grid);
        dbProvider = new StubDatabaseProvider();
        controller = new GridAIController(grid, dbProvider);
    }

    // --- Constructor ---

    @Test
    void constructor_nullGrid_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new GridAIController(null, dbProvider));
    }

    @Test
    void constructor_nullDatabaseProvider_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new GridAIController(grid, null));
    }

    // --- Tools ---

    @Test
    void getTools_returnsFourTools() {
        var tools = controller.getTools();
        Assertions.assertEquals(4, tools.size());
    }

    @Test
    void getTools_containsSchemaStatAndUpdateTools() {
        var tools = controller.getTools();
        var names = tools.stream().map(LLMProvider.ToolSpec::getName).toList();
        Assertions.assertTrue(names.contains("get_grid_instructions"));
        Assertions.assertTrue(names.contains("get_database_schema"));
        Assertions.assertTrue(names.contains("get_grid_state"));
        Assertions.assertTrue(names.contains("update_grid_data"));
    }

    @Test
    void getTools_instructionsToolIsFirst() {
        Assertions.assertEquals("get_grid_instructions",
                controller.getTools().get(0).getName());
    }

    @Test
    void schemaTool_returnsSchema() {
        dbProvider.schema = "test schema";
        var tool = controller.getTools().stream()
                .filter(t -> t.getName().equals("get_database_schema"))
                .findFirst().orElseThrow();
        Assertions.assertEquals("test schema", tool.execute(json("{}")));
    }

    @Test
    void currentStateTool_noQuery_returnsEmpty() {
        var tool = controller.getTools().stream()
                .filter(t -> t.getName().equals("get_grid_state")).findFirst()
                .orElseThrow();
        var result = tool.execute(json("{}"));
        Assertions.assertTrue(result.contains("empty"));
    }

    @Test
    void updateDataTool_validQuery_returnSuccess() {
        dbProvider.queryResults = List.of(row("a", 1));
        var tool = controller.getTools().stream()
                .filter(t -> t.getName().equals("update_grid_data")).findFirst()
                .orElseThrow();
        var result = tool.execute(json("{\"query\": \"SELECT 1\"}"));
        Assertions.assertTrue(result.contains("queued successfully"));
    }

    @Test
    void updateDataTool_invalidQuery_returnsError() {
        dbProvider.throwOnExecute = true;
        var tool = controller.getTools().stream()
                .filter(t -> t.getName().equals("update_grid_data")).findFirst()
                .orElseThrow();
        var result = tool.execute(json("{\"query\": \"INVALID\"}"));
        Assertions.assertTrue(result.contains("Error"));
    }

    @Test
    void updateDataTool_missingQuery_returnsError() {
        var tool = controller.getTools().stream()
                .filter(t -> t.getName().equals("update_grid_data")).findFirst()
                .orElseThrow();
        var result = tool.execute(json("{}"));
        Assertions.assertTrue(result.contains("Error"));
    }

    @Test
    void updateDataTool_hasParameterSchema() {
        var tool = controller.getTools().stream()
                .filter(t -> t.getName().equals("update_grid_data")).findFirst()
                .orElseThrow();
        Assertions.assertNotNull(tool.getParametersSchema());
        Assertions.assertTrue(tool.getParametersSchema().contains("query"));
    }

    // --- getState ---

    @Test
    void getState_beforeAnyUpdate_returnsNull() {
        Assertions.assertNull(controller.getState());
    }

    @Test
    void getState_afterUpdate_returnsQuery() {
        dbProvider.queryResults = List.of(row("a", 1));
        simulateUpdate("SELECT a FROM t");

        var state = controller.getState();
        Assertions.assertNotNull(state);
        Assertions.assertEquals("SELECT a FROM t", state.query());
    }

    @Test
    void getState_afterFailedRender_returnsNull() {
        dbProvider.queryResults = List.of(row("a", 1));
        findTool("update_grid_data")
                .execute(json("{\"query\": \"SELECT a FROM t\"}"));

        dbProvider.throwOnExecute = true;
        Assertions.assertThrows(RuntimeException.class,
                () -> controller.onResponseComplete());

        Assertions.assertNull(controller.getState());
    }

    // --- onResponseComplete ---

    @Test
    void currentStateTool_afterUpdate_returnsQuery() {
        dbProvider.queryResults = List.of(row("a", 1));
        simulateUpdate("SELECT a FROM t");

        var stateTool = findTool("get_grid_state");
        var result = stateTool.execute(json("{}"));
        Assertions.assertTrue(result.contains("SELECT a FROM t"));
    }

    @Test
    void onResponseComplete_noPending_doesNotChangeGrid() {
        // No pending query — should be a no-op
        var columnsBefore = grid.getColumns().size();
        controller.onResponseComplete();
        Assertions.assertEquals(columnsBefore, grid.getColumns().size());
        Assertions.assertNull(controller.getState());
    }

    @Test
    void onResponseComplete_clearsPending_secondCallIsNoOp() {
        dbProvider.queryResults = List.of(row("a", 1));
        simulateUpdate("SELECT a FROM t");

        // Second call — no pending query
        controller.onResponseComplete();

        // State should still reflect the first update
        var stateTool = findTool("get_grid_state");
        Assertions.assertTrue(
                stateTool.execute(json("{}")).contains("SELECT a FROM t"));
    }

    // --- GridState serialization ---

    @Test
    void gridState_isSerializable() throws Exception {
        var state = new GridState("SELECT * FROM t");
        var baos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(baos)) {
            oos.writeObject(state);
        }
        try (var ois = new ObjectInputStream(
                new ByteArrayInputStream(baos.toByteArray()))) {
            var deserialized = (GridState) ois.readObject();
            Assertions.assertEquals("SELECT * FROM t", deserialized.query());
        }
    }

    // --- restoreState ---

    @Test
    void restoreState_rendersGridAndSetsState() {
        dbProvider.queryResults = List.of(row("col1", "v1", "col2", 42));
        controller.restoreState(new GridState("SELECT col1, col2 FROM t"));

        Assertions.assertEquals(2, grid.getColumns().size());
        var state = controller.getState();
        Assertions.assertNotNull(state);
        Assertions.assertEquals("SELECT col1, col2 FROM t", state.query());
    }

    @Test
    void restoreState_nullState_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> controller.restoreState(null));
    }

    @Test
    void restoreState_failedQuery_doesNotUpdateState() {
        dbProvider.throwOnExecute = true;
        controller.restoreState(new GridState("SELECT bad"));

        Assertions.assertNull(controller.getState());
    }

    @Test
    void restoreState_thenLlmUpdate_overridesState() {
        dbProvider.queryResults = List.of(row("a", 1));
        controller.restoreState(new GridState("SELECT a FROM old"));
        Assertions.assertEquals("SELECT a FROM old",
                controller.getState().query());

        // LLM updates with a new query
        simulateUpdate("SELECT a FROM new_table");
        Assertions.assertEquals("SELECT a FROM new_table",
                controller.getState().query());
    }

    @Test
    void restoreState_nullQuery_isNoOp() {
        controller.restoreState(new GridState(null));

        Assertions.assertNull(controller.getState());
        Assertions.assertEquals(0, grid.getColumns().size());
    }

    @Test
    void restoreState_calledTwice_secondOverwritesFirst() {
        dbProvider.queryResults = List.of(row("a", 1));
        controller.restoreState(new GridState("SELECT a FROM first"));
        Assertions.assertEquals("SELECT a FROM first",
                controller.getState().query());

        controller.restoreState(new GridState("SELECT a FROM second"));
        Assertions.assertEquals("SELECT a FROM second",
                controller.getState().query());
    }

    @Test
    void restoreState_emptyResult_stillSetsQuery() {
        dbProvider.queryResults = List.of();
        controller.restoreState(new GridState("SELECT a FROM empty_table"));

        var state = controller.getState();
        Assertions.assertNotNull(state);
        Assertions.assertEquals("SELECT a FROM empty_table", state.query());
    }

    @Test
    void getState_afterSuccessfulRestore_thenFailedRender_retainsPreviousQuery() {
        dbProvider.queryResults = List.of(row("a", 1));
        controller.restoreState(new GridState("SELECT a FROM good"));
        Assertions.assertEquals("SELECT a FROM good",
                controller.getState().query());

        // LLM tries a bad query
        dbProvider.queryResults = List.of(row("a", 1));
        findTool("update_grid_data")
                .execute(json("{\"query\": \"SELECT a FROM bad\"}"));
        dbProvider.throwOnExecute = true;
        Assertions.assertThrows(RuntimeException.class,
                () -> controller.onResponseComplete());

        // Previous successful query should be retained
        Assertions.assertEquals("SELECT a FROM good",
                controller.getState().query());
    }

    @Test
    void restoreState_thenGetStateTool_returnsRestoredQuery() {
        dbProvider.queryResults = List.of(row("a", 1));
        controller.restoreState(new GridState("SELECT a FROM t"));

        var result = findTool("get_grid_state").execute(json("{}"));
        Assertions.assertTrue(result.contains("SELECT a FROM t"));
    }

    // --- State change listeners ---

    @Test
    void stateChangeListener_firesAfterOnResponseComplete() {
        var captured = new AtomicReference<GridState>();
        controller.addStateChangeListener(captured::set);

        dbProvider.queryResults = List.of(row("a", 1));
        simulateUpdate("SELECT a FROM t");

        Assertions.assertNotNull(captured.get());
        Assertions.assertEquals("SELECT a FROM t", captured.get().query());
    }

    @Test
    void stateChangeListener_doesNotFireOnRenderFailure() {
        var captured = new AtomicReference<GridState>();
        controller.addStateChangeListener(captured::set);

        dbProvider.queryResults = List.of(row("a", 1));
        findTool("update_grid_data")
                .execute(json("{\"query\": \"SELECT a FROM t\"}"));

        dbProvider.throwOnExecute = true;
        Assertions.assertThrows(RuntimeException.class,
                () -> controller.onResponseComplete());

        Assertions.assertNull(captured.get());
    }

    @Test
    void stateChangeListener_doesNotFireOnRestoreState() {
        var captured = new AtomicReference<GridState>();
        controller.addStateChangeListener(captured::set);

        dbProvider.queryResults = List.of(row("a", 1));
        controller.restoreState(new GridState("SELECT a FROM t"));

        Assertions.assertNull(captured.get());
    }

    @Test
    void stateChangeListener_registration_removesListener() {
        var captured = new AtomicReference<GridState>();
        var registration = controller.addStateChangeListener(captured::set);
        registration.remove();

        dbProvider.queryResults = List.of(row("a", 1));
        simulateUpdate("SELECT a FROM t");

        Assertions.assertNull(captured.get());
    }

    @Test
    void stateChangeListener_doesNotFireOnSecondCall() {
        dbProvider.queryResults = List.of(row("a", 1));
        simulateUpdate("SELECT a FROM t");

        var states = new ArrayList<GridState>();
        controller.addStateChangeListener(states::add);

        controller.onResponseComplete();

        Assertions.assertTrue(states.isEmpty(),
                "Second onResponseComplete should not fire listeners");
    }

    @Test
    void stateChangeListener_throwingListenerDoesNotPreventOthers() {
        var capture = new AtomicReference<GridState>();

        controller.addStateChangeListener(state -> {
            throw new RuntimeException("Listener failure");
        });
        controller.addStateChangeListener(capture::set);

        dbProvider.queryResults = List.of(row("a", 1));
        simulateUpdate("SELECT a FROM t");

        Assertions.assertNotNull(capture.get(),
                "Second listener should fire despite first throwing");
    }

    @Test
    void stateChangeListener_firesAfterRestoreThenLlmUpdate() {
        dbProvider.queryResults = List.of(row("a", 1));
        controller.restoreState(new GridState("SELECT a FROM old"));

        var captured = new AtomicReference<GridState>();
        controller.addStateChangeListener(captured::set);

        simulateUpdate("SELECT a FROM new_table");

        Assertions.assertNotNull(captured.get());
        Assertions.assertEquals("SELECT a FROM new_table",
                captured.get().query());
    }

    @Test
    void stateChangeListener_nullListener_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> controller.addStateChangeListener(null));
    }

    // --- Empty state ---

    @Test
    void emptyResult_setsDefaultEmptyStateText() {
        dbProvider.queryResults = List.of();
        simulateUpdate("SELECT * FROM empty_table");

        Assertions.assertEquals("No results", grid.getEmptyStateText());
    }

    @Test
    void emptyResult_doesNotOverrideCustomEmptyStateText() {
        grid.setEmptyStateText("Custom message");
        dbProvider.queryResults = List.of();
        simulateUpdate("SELECT * FROM empty_table");

        Assertions.assertEquals("Custom message", grid.getEmptyStateText());
    }

    @Test
    void emptyResult_doesNotOverrideCustomEmptyStateComponent() {
        grid.setEmptyStateComponent(
                new com.vaadin.flow.component.html.Span("Custom"));
        dbProvider.queryResults = List.of();
        simulateUpdate("SELECT * FROM empty_table");

        Assertions.assertNotNull(grid.getEmptyStateComponent());
        Assertions.assertNull(grid.getEmptyStateText());
    }

    @Test
    void nonEmptyResult_doesNotSetEmptyStateText() {
        dbProvider.queryResults = List.of(row("a", 1));
        simulateUpdate("SELECT a FROM t");

        Assertions.assertNull(grid.getEmptyStateText());
    }

    // --- Instructions tool ---

    @Test
    void instructionsTool_descriptionContainsWorkflow() {
        var tool = findTool("get_grid_instructions");
        Assertions.assertTrue(tool.getDescription().contains("get_grid_state"));
        Assertions.assertTrue(
                tool.getDescription().contains("get_database_schema"));
        Assertions
                .assertTrue(tool.getDescription().contains("update_grid_data"));
    }

    @Test
    void instructionsTool_executeReturnsWorkflow() {
        var result = findTool("get_grid_instructions").execute(null);
        Assertions.assertFalse(result.isBlank());
        Assertions.assertTrue(result.contains("get_grid_state"));
    }

    // --- stripGroupPrefix ---

    @Nested
    class StripGroupPrefixTests {

        @Test
        void withDot_returnsAfterDot() {
            Assertions.assertEquals("Email",
                    GridFormatting.stripGroupPrefix("Contact.Email"));
        }

        @Test
        void withoutDot_returnsFullName() {
            Assertions.assertEquals("salary",
                    GridFormatting.stripGroupPrefix("salary"));
        }

        @Test
        void leadingDot_returnsFullName() {
            // Dot at position 0 — no prefix to strip
            Assertions.assertEquals(".hidden",
                    GridFormatting.stripGroupPrefix(".hidden"));
        }

        @Test
        void multipleDots_stripsFirstOnly() {
            Assertions.assertEquals("Street.Number",
                    GridFormatting.stripGroupPrefix("Address.Street.Number"));
        }
    }

    // --- groupPrefix ---

    @Nested
    class GroupPrefixTests {

        @Test
        void withDot_returnsPrefix() {
            Assertions.assertEquals("Contact",
                    GridFormatting.groupPrefix("Contact.Email"));
        }

        @Test
        void withoutDot_returnsEmpty() {
            Assertions.assertEquals("", GridFormatting.groupPrefix("salary"));
        }

        @Test
        void leadingDot_returnsEmpty() {
            Assertions.assertEquals("", GridFormatting.groupPrefix(".hidden"));
        }

        @Test
        void multipleDots_returnsFirstPrefix() {
            Assertions.assertEquals("Address",
                    GridFormatting.groupPrefix("Address.Street.Number"));
        }
    }

    // --- Column grouping ordering ---

    @Nested
    class ColumnGroupingOrderTests {

        @Test
        void groupedColumnsAreSortedAdjacent() {
            // Columns: Person.Name, Product.Name, Product.Category,
            // Order.Quantity — non-adjacent groups in original order
            dbProvider.queryResults = List.of(row("Person.Name", "Alice",
                    "Product.Name", "Laptop", "Product.Category", "Electronics",
                    "Order.Quantity", 1));
            simulateUpdate("SELECT 1");

            var keys = grid.getColumns().stream().map(Grid.Column::getKey)
                    .toList();
            // Ungrouped columns first (none here), then grouped sorted
            // by prefix: Order, Person, Product
            var orderIdx = keys.indexOf("Order.Quantity");
            var personIdx = keys.indexOf("Person.Name");
            var productNameIdx = keys.indexOf("Product.Name");
            var productCatIdx = keys.indexOf("Product.Category");

            // Order < Person < Product (alphabetical)
            Assertions.assertTrue(orderIdx < personIdx);
            Assertions.assertTrue(personIdx < productNameIdx);
            // Product columns must be adjacent
            Assertions.assertEquals(productNameIdx + 1, productCatIdx);
        }

        @Test
        void ungroupedColumnsComFirst() {
            dbProvider.queryResults = List
                    .of(row("plain", "val", "Group.A", 1, "Group.B", 2));
            simulateUpdate("SELECT 1");

            var keys = grid.getColumns().stream().map(Grid.Column::getKey)
                    .toList();
            // "plain" has empty prefix → sorts first
            Assertions.assertEquals("plain", keys.getFirst());
        }

        @Test
        void singleColumnGroup_noGroupHeader() {
            // Only one column with a dot — not enough for a group
            dbProvider.queryResults = List.of(row("Group.Only", 1, "other", 2));
            simulateUpdate("SELECT 1");

            // Should not throw — single-column groups are skipped
            Assertions.assertEquals(2, grid.getColumns().size());
        }

        @Test
        void groupHeadersClearedOnSubsequentUpdate() {
            // First update: grouped columns
            dbProvider.queryResults = List.of(row("G.A", 1, "G.B", 2));
            simulateUpdate("SELECT 1");
            Assertions.assertEquals(2, grid.getHeaderRows().size(),
                    "Should have default + group header row");

            // Second update: no grouped columns
            dbProvider.queryResults = List.of(row("plain", 1));
            simulateUpdate("SELECT 2");
            Assertions.assertEquals(1, grid.getHeaderRows().size(),
                    "Group header row should be removed");
        }

        @Test
        void multipleUpdatesWithGroups_noAccumulation() {
            // Three successive updates with groups — should always
            // have exactly 2 header rows (default + one group row)
            for (int i = 0; i < 3; i++) {
                dbProvider.queryResults = List.of(row("X.A", 1, "X.B", 2));
                simulateUpdate("SELECT " + i);
            }
            Assertions.assertEquals(2, grid.getHeaderRows().size(),
                    "Should not accumulate group header rows");
        }
    }

    // --- formatHeader ---

    @Nested
    class FormatHeaderTests {

        @Test
        void underscoreSeparated() {
            Assertions.assertEquals("Emp Name",
                    GridFormatting.formatHeader("emp_name"));
        }

        @Test
        void singleWord() {
            Assertions.assertEquals("Salary",
                    GridFormatting.formatHeader("salary"));
        }

        @Test
        void allCaps() {
            Assertions.assertEquals("Salary",
                    GridFormatting.formatHeader("SALARY"));
        }

        @Test
        void alreadyFormattedWithSpaces() {
            Assertions.assertEquals("Employee Name",
                    GridFormatting.formatHeader("Employee Name"));
        }

        @Test
        void multipleUnderscores() {
            Assertions.assertEquals("First Name Last",
                    GridFormatting.formatHeader("first_name_last"));
        }

        @Test
        void leadingUnderscore() {
            Assertions.assertEquals("Id", GridFormatting.formatHeader("_id"));
        }

        @Test
        void trailingUnderscore() {
            Assertions.assertEquals("Name",
                    GridFormatting.formatHeader("name_"));
        }

        @Test
        void singleCharacter() {
            Assertions.assertEquals("X", GridFormatting.formatHeader("x"));
        }

        @Test
        void emptyString() {
            Assertions.assertEquals("", GridFormatting.formatHeader(""));
        }

        @Test
        void consecutiveUnderscores() {
            Assertions.assertEquals("A B", GridFormatting.formatHeader("a__b"));
        }
    }

    // --- formatValue ---

    @Nested
    class FormatValueTests {

        @Test
        void localDate() {
            Assertions.assertEquals("2024-01-15",
                    GridFormatting.formatValue(LocalDate.of(2024, 1, 15)));
        }

        @Test
        void localDateTime() {
            Assertions.assertEquals("2024-01-15 12:30", GridFormatting
                    .formatValue(LocalDateTime.of(2024, 1, 15, 12, 30)));
        }

        @Test
        void sqlDate() {
            Assertions.assertEquals("2024-01-15", GridFormatting
                    .formatValue(java.sql.Date.valueOf("2024-01-15")));
        }

        @Test
        void sqlTimestamp() {
            var ts = Timestamp.valueOf("2024-01-15 12:30:00");
            Assertions.assertEquals("2024-01-15 12:30",
                    GridFormatting.formatValue(ts));
        }

        @Test
        void instant() {
            var instant = LocalDateTime.of(2024, 1, 15, 12, 30)
                    .atZone(ZoneId.systemDefault()).toInstant();
            var expected = instant.atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            Assertions.assertEquals(expected,
                    GridFormatting.formatValue(instant));
        }

        @Test
        void booleanTrue() {
            Assertions.assertEquals("Yes", GridFormatting.formatValue(true));
        }

        @Test
        void booleanFalse() {
            Assertions.assertEquals("No", GridFormatting.formatValue(false));
        }

        @Test
        void bigDecimalTrailingZeros() {
            Assertions.assertEquals("1.5",
                    GridFormatting.formatValue(new BigDecimal("1.50")));
        }

        @Test
        void bigDecimalWholeNumber() {
            Assertions.assertEquals("100",
                    GridFormatting.formatValue(new BigDecimal("100.00")));
        }

        @Test
        void doubleValue() {
            Assertions.assertEquals("3.14", GridFormatting.formatValue(3.14));
        }

        @Test
        void floatValue() {
            Assertions.assertEquals("2.5", GridFormatting.formatValue(2.5f));
        }

        @Test
        void integerValue() {
            Assertions.assertEquals("42", GridFormatting.formatValue(42));
        }

        @Test
        void stringValue() {
            Assertions.assertEquals("hello",
                    GridFormatting.formatValue("hello"));
        }

        @Test
        void longValue() {
            Assertions.assertEquals("1000000",
                    GridFormatting.formatValue(1000000L));
        }

        @Test
        void javaUtilDate() {
            var date = new java.util.Date(LocalDateTime.of(2024, 1, 15, 12, 30)
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            var expected = date.toInstant().atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            Assertions.assertEquals(expected, GridFormatting.formatValue(date));
        }
    }

    // --- Lazy data provider ---

    @Nested
    class LazyDataProviderTests {

        @BeforeEach
        void renderGrid() {
            dbProvider.queryResults = List.of(row("a", 1));
            simulateUpdate("SELECT a FROM t");
            dbProvider.executedQueries.clear();
        }

        @Test
        void render_executesSampleQueryWithLimit1() {
            // The sample query was already executed in @BeforeEach.
            // Re-render to capture the SQL.
            dbProvider.executedQueries.clear();
            dbProvider.queryResults = List.of(row("a", 1));
            simulateUpdate("SELECT a FROM t");

            // First query is the validation in updateData, second is
            // the sample row fetch in renderGrid
            var sampleSql = dbProvider.executedQueries.get(1);
            Assertions.assertEquals(
                    "SELECT * FROM (SELECT a FROM t) AS _limited LIMIT 1",
                    sampleSql);
        }

        @Test
        void fetch_wrapsQueryAsSubquery() {
            grid.getDataProvider()
                    .fetch(new Query<>(20, 10, List.of(), null, null));

            var sql = dbProvider.executedQueries.getFirst();
            Assertions.assertEquals(
                    "SELECT * FROM (SELECT a FROM t) AS _limited LIMIT 10 OFFSET 20",
                    sql);
        }

        @Test
        void fetch_withSortOrder_wrapsWithOrderBy() {
            var sort = List
                    .of(new QuerySortOrder("a", SortDirection.DESCENDING));
            grid.getDataProvider().fetch(new Query<>(0, 50, sort, null, null));

            var sql = dbProvider.executedQueries.getFirst();
            Assertions.assertTrue(sql.contains("ORDER BY \"a\" DESC"),
                    "Should contain ORDER BY: " + sql);
            Assertions.assertTrue(sql.contains("LIMIT 50"),
                    "Should contain LIMIT: " + sql);
            Assertions.assertTrue(sql.contains("OFFSET 0"),
                    "Should contain OFFSET: " + sql);
        }

        @Test
        void fetch_withMultipleSortOrders() {
            var sort = List.of(new QuerySortOrder("a", SortDirection.ASCENDING),
                    new QuerySortOrder("b", SortDirection.DESCENDING));
            grid.getDataProvider().fetch(new Query<>(0, 10, sort, null, null));

            var sql = dbProvider.executedQueries.getFirst();
            Assertions.assertTrue(sql.contains("\"a\" ASC, \"b\" DESC"),
                    "Should contain both sort orders: " + sql);
        }

        @Test
        void fetch_returnsRowsFromDatabase() {
            dbProvider.queryResults = List.of(row("a", 10), row("a", 20));
            var rows = grid.getDataProvider()
                    .fetch(new Query<>(0, 50, List.of(), null, null)).toList();

            Assertions.assertEquals(2, rows.size());
            Assertions.assertEquals(10, rows.get(0).get("a"));
            Assertions.assertEquals(20, rows.get(1).get("a"));
        }

        @Test
        void count_wrapsQueryWithCount() {
            dbProvider.queryResults = List.of(row("COUNT(*)", 42));
            var size = grid.getDataProvider().size(new Query<>());

            Assertions.assertEquals(42, size);
            var sql = dbProvider.executedQueries.getFirst();
            Assertions.assertEquals(
                    "SELECT COUNT(*) FROM (SELECT a FROM t) AS _counted", sql);
        }

        @Test
        void count_emptyResult_returnsZero() {
            dbProvider.queryResults = List.of();
            var size = grid.getDataProvider().size(new Query<>());

            Assertions.assertEquals(0, size);
        }

        @Test
        void count_nonNumericResult_returnsZero() {
            dbProvider.queryResults = List.of(row("COUNT(*)", "not a number"));
            var size = grid.getDataProvider().size(new Query<>());

            Assertions.assertEquals(0, size);
        }

        @Test
        void count_rowWithNoColumns_returnsZero() {
            dbProvider.queryResults = List.of(new LinkedHashMap<>());
            var size = grid.getDataProvider().size(new Query<>());

            Assertions.assertEquals(0, size);
        }

        @Test
        void dataProvider_replacedOnSubsequentUpdate() {
            var first = grid.getDataProvider();

            simulateUpdate("SELECT b FROM t");
            var second = grid.getDataProvider();

            Assertions.assertNotSame(first, second);
        }
    }

    // --- Helpers ---

    private LLMProvider.ToolSpec findTool(String name) {
        return controller.getTools().stream()
                .filter(t -> t.getName().equals(name)).findFirst()
                .orElseThrow();
    }

    private static JsonNode json(String json) {
        return JacksonUtils.readTree(json);
    }

    /**
     * Simulates a full LLM update cycle: tool call + onResponseComplete.
     */
    private void simulateUpdate(String query) {
        findTool("update_grid_data")
                .execute(json("{\"query\": \"" + query + "\"}"));
        controller.onResponseComplete();
    }

    private static Map<String, Object> row(Object... keysAndValues) {
        var map = new LinkedHashMap<String, Object>();
        for (var i = 0; i < keysAndValues.length; i += 2) {
            map.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }

    /**
     * Stub DatabaseProvider for unit testing. Returns configurable results
     * without an actual database.
     */
    private static class StubDatabaseProvider implements DatabaseProvider {
        private String schema = "test schema";
        private List<Map<String, Object>> queryResults = List.of();
        private boolean throwOnExecute = false;
        private final List<String> executedQueries = new ArrayList<>();

        @Override
        public String getSchema() {
            return schema;
        }

        @Override
        public List<Map<String, Object>> executeQuery(String sql) {
            executedQueries.add(sql);
            if (throwOnExecute) {
                throw new RuntimeException("Query execution failed");
            }
            return queryResults;
        }
    }
}
