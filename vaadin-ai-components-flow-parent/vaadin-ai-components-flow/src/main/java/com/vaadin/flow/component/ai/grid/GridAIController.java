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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.orchestrator.AIController;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.DatabaseProviderAITools;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.internal.JacksonUtils;

/**
 * AI controller for populating a {@link Grid} with database data via LLM tool
 * calls. The grid automatically creates columns from query results with:
 * <ul>
 * <li>Lazy loading with SQL {@code LIMIT}/{@code OFFSET}</li>
 * <li>Built-in renderers for dates and numbers</li>
 * <li>Right-alignment for numeric columns</li>
 * <li>Resizable, sortable columns with auto-width</li>
 * <li>Column grouping from dot-separated aliases</li>
 * </ul>
 * <p>
 * Tools are provided by {@link GridAITools} and
 * {@link DatabaseProviderAITools}. State changes are deferred and applied in
 * {@link #onRequestCompleted()}.
 * </p>
 * <p>
 * This controller is <b>not serializable</b>.
 * </p>
 *
 * @author Vaadin Ltd
 * @see GridAITools
 * @see DatabaseProviderAITools
 */
public class GridAIController implements AIController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GridAIController.class);

    private final Grid<Map<String, Object>> grid;
    private final DatabaseProvider databaseProvider;

    private final AtomicReference<String> currentQuery = new AtomicReference<>();
    private final AtomicReference<String> pendingQuery = new AtomicReference<>();

    private static final String DEFAULT_EMPTY_STATE_TEXT = "No results";
    private static final String DEFAULT_GRID_ID = "grid";

    /**
     * Creates a new grid AI controller.
     *
     * @param grid
     *            the grid to populate, not {@code null}
     * @param databaseProvider
     *            the database provider for schema and query execution, not
     *            {@code null}
     */
    public GridAIController(Grid<Map<String, Object>> grid,
            DatabaseProvider databaseProvider) {
        this.grid = Objects.requireNonNull(grid, "Grid must not be null");
        this.databaseProvider = Objects.requireNonNull(databaseProvider,
                "DatabaseProvider must not be null");
    }

    /**
     * Returns the recommended system prompt for grid data display capabilities.
     *
     * @return the system prompt text
     */
    public static String getSystemPrompt() {
        return """
                You have access to grid data display capabilities:

                TOOLS:
                1. get_database_schema() - Retrieves database schema (tables, columns, types)
                2. get_grid_state() - Returns current grid state (query)
                3. update_grid_data(query) - Updates grid data with SQL SELECT query

                WORKFLOW:
                Complete the user's request in a SINGLE response by calling all needed tools.
                1. Call get_grid_state() to see what's already configured
                2. Use get_database_schema() if you need to understand available data
                3. Call update_grid_data() with a SQL SELECT query

                The grid automatically creates columns based on query result columns.
                Column headers are derived from SQL column names/aliases.
                Use SQL aliases (AS) to provide human-readable column headers.
                Do NOT use LIMIT or OFFSET — the grid handles pagination automatically.

                COLUMN GROUPING:
                Use dot-separated aliases to group related columns under a shared header.
                Example: SELECT email AS "Contact.Email", phone AS "Contact.Phone" FROM t
                This creates a "Contact" group header spanning both columns.

                Example: SELECT name AS "Employee Name", salary AS "Salary" FROM employees

                IMPORTANT:
                - Call get_grid_state() and update_grid_data() in the SAME response
                - Do NOT stop after get_grid_state()
                - Use double quotes for column aliases with spaces or dots
                """;
    }

    @Override
    public List<LLMProvider.ToolSpec> getTools() {
        var tools = new ArrayList<LLMProvider.ToolSpec>();
        tools.addAll(DatabaseProviderAITools.createAll(databaseProvider));
        tools.addAll(GridAITools.createAll(new GridAITools.Callbacks() {
            @Override
            public String getState(String gridId) {
                var query = currentQuery.get();
                var node = JacksonUtils.createObjectNode();
                node.put("gridId", gridId);
                if (query == null) {
                    node.put("status", "empty");
                    node.put("message", "No grid data has been loaded yet");
                } else {
                    node.put("query", query);
                }
                return node.toString();
            }

            @Override
            public void updateData(String gridId, String query) {
                databaseProvider.executeQuery(wrapWithLimit(query, 1));
                pendingQuery.set(query);
            }

            @Override
            public Set<String> getGridIds() {
                return Set.of(DEFAULT_GRID_ID);
            }
        }));
        return tools;
    }

    @Override
    public void onRequestCompleted() {
        var query = pendingQuery.getAndSet(null);
        if (query == null) {
            LOGGER.info("onRequestCompleted: no pending query");
            return;
        }
        try {
            LOGGER.info("onRequestCompleted: applying query: {}", query);
            applyQuery(query);
            currentQuery.set(query);
            LOGGER.info("onRequestCompleted: grid updated successfully");
        } catch (Exception e) {
            LOGGER.error("Error updating grid", e);
        }
    }

    // --- Grid rendering ---

    private void applyQuery(String query) {
        grid.getUI().ifPresentOrElse(ui -> ui.access(() -> {
            try {
                renderGrid(query);
            } catch (Exception e) {
                LOGGER.error("Error inside UI.access()", e);
            }
        }), () -> renderGrid(query));
    }

    private void renderGrid(String query) {
        var sampleRows = databaseProvider.executeQuery(wrapWithLimit(query, 1));
        removeExtraHeaderRows();
        grid.removeAllColumns();
        if (sampleRows.isEmpty()) {
            if (grid.getEmptyStateText() == null
                    && grid.getEmptyStateComponent() == null) {
                grid.setEmptyStateText(DEFAULT_EMPTY_STATE_TEXT);
            }
            grid.setItems(List.of());
            return;
        }
        var firstRow = sampleRows.getFirst();
        // Sort columns by group prefix so grouped columns are adjacent
        // (required by HeaderRow.join)
        var sortedColumns = new ArrayList<>(firstRow.entrySet());
        sortedColumns.sort((a, b) -> {
            var prefixA = GridFormatting.groupPrefix(a.getKey());
            var prefixB = GridFormatting.groupPrefix(b.getKey());
            return prefixA.compareTo(prefixB);
        });
        for (var entry : sortedColumns) {
            addColumn(entry.getKey(), entry.getValue());
        }
        applyColumnGrouping();
        setupLazyDataProvider(query);
        LOGGER.info("Grid configured with {} columns", firstRow.size());
    }

    /**
     * Adds a column to the grid with type-appropriate rendering, alignment, and
     * resizing based on the sample value.
     */
    private void addColumn(String columnName, Object sampleValue) {
        var header = GridFormatting
                .formatHeader(GridFormatting.stripGroupPrefix(columnName));

        Grid.Column<Map<String, Object>> column;

        if (sampleValue instanceof LocalDate
                || sampleValue instanceof java.sql.Date) {
            column = grid.addColumn(new LocalDateRenderer<>(
                    row -> toLocalDate(row.get(columnName))));
        } else if (sampleValue instanceof LocalDateTime
                || sampleValue instanceof Timestamp) {
            column = grid.addColumn(new LocalDateTimeRenderer<>(
                    row -> toLocalDateTime(row.get(columnName))));
        } else if (sampleValue instanceof Number) {
            column = grid.addColumn(
                    new NumberRenderer<>(row -> (Number) row.get(columnName),
                            java.text.NumberFormat.getInstance()));
            column.setTextAlign(ColumnTextAlign.END);
        } else {
            column = grid.addColumn(row -> {
                var value = row.get(columnName);
                return value != null ? GridFormatting.formatValue(value) : "";
            });
        }

        column.setHeader(header).setKey(columnName).setSortable(true)
                .setAutoWidth(true).setResizable(true);
    }

    /**
     * Applies column grouping based on dot-separated column names. Columns with
     * names like {@code "Contact.Email"} and {@code "Contact.Phone"} are
     * grouped under a {@code "Contact"} header.
     */
    private void applyColumnGrouping() {
        // Collect columns by group prefix
        var groups = new LinkedHashMap<String, List<Grid.Column<Map<String, Object>>>>();
        for (var column : grid.getColumns()) {
            var key = column.getKey();
            var dotIndex = key.indexOf('.');
            if (dotIndex > 0) {
                var prefix = key.substring(0, dotIndex);
                groups.computeIfAbsent(prefix, k -> new ArrayList<>())
                        .add(column);
            }
        }

        // Only create group headers for groups with 2+ columns
        var groupsToApply = groups.entrySet().stream()
                .filter(e -> e.getValue().size() > 1).toList();

        if (groupsToApply.isEmpty()) {
            return;
        }

        var groupRow = grid.prependHeaderRow();
        for (var entry : groupsToApply) {
            var columns = entry.getValue();
            var cells = columns.stream().map(groupRow::getCell).toList();
            groupRow.join(cells).setText(entry.getKey());
        }
    }

    /**
     * Removes header rows added by previous column grouping, keeping only the
     * default column header row.
     */
    private void removeExtraHeaderRows() {
        var headerRows = grid.getHeaderRows();
        while (headerRows.size() > 1) {
            grid.removeHeaderRow(headerRows.getFirst());
            headerRows = grid.getHeaderRows();
        }
    }

    private void setupLazyDataProvider(String query) {
        var countQuery = wrapWithCount(query);
        var dataProvider = new CallbackDataProvider<Map<String, Object>, Void>(
                fetchQuery -> {
                    var sql = enrichQuery(query, fetchQuery.getOffset(),
                            fetchQuery.getLimit(), fetchQuery.getSortOrders());
                    LOGGER.debug("Fetching rows: {}", sql);
                    return databaseProvider.executeQuery(sql).stream();
                }, countFetchQuery -> {
                    LOGGER.debug("Counting rows: {}", countQuery);
                    var countResult = databaseProvider.executeQuery(countQuery);
                    if (!countResult.isEmpty()) {
                        var firstValue = countResult.getFirst().values()
                                .iterator().next();
                        return firstValue instanceof Number n ? n.intValue()
                                : 0;
                    }
                    return 0;
                }, row -> row);

        grid.setItems(dataProvider);
    }

    // --- Type conversion helpers ---

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate ld) {
            return ld;
        }
        if (value instanceof java.sql.Date sd) {
            return sd.toLocalDate();
        }
        return null;
    }

    private static LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime ldt) {
            return ldt;
        }
        if (value instanceof Timestamp ts) {
            return ts.toLocalDateTime();
        }
        return null;
    }

    // --- SQL helpers ---

    private static String enrichQuery(String query, int offset, int limit,
            List<QuerySortOrder> sortOrders) {
        var sortOrdersExpression = sortOrders.stream().map(sortOrder -> {
            var col = sortOrder.getSorted();
            var dir = sortOrder.getDirection() == SortDirection.ASCENDING
                    ? "ASC"
                    : "DESC";
            return "\"" + col + "\" " + dir;
        }).collect(Collectors.joining(", "));
        if (!sortOrdersExpression.isEmpty()) {
            query = "SELECT * FROM (" + query + ") AS _t ORDER BY "
                    + sortOrdersExpression;
        }
        return wrapWithLimit(query, limit) + " OFFSET " + offset;
    }

    private static String wrapWithLimit(String query, int limit) {
        return "SELECT * FROM (" + query + ") AS _limited LIMIT " + limit;
    }

    private static String wrapWithCount(String query) {
        return "SELECT COUNT(*) FROM (" + query + ") AS _counted";
    }
}
