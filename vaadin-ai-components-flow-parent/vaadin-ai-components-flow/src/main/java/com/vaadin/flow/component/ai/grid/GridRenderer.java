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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;

/**
 * Applies pending grid state and renders grid data. Encapsulates the
 * query-execution-to-grid pipeline so that different controllers (standalone
 * grid, dashboard) share the same rendering logic.
 * <p>
 * Rendering includes:
 * <ul>
 * <li>Lazy loading with SQL {@code LIMIT}/{@code OFFSET}</li>
 * <li>Built-in renderers for dates and numbers</li>
 * <li>Right-alignment for numeric columns</li>
 * <li>Resizable, sortable columns with auto-width</li>
 * <li>Column grouping from dot-separated aliases</li>
 * </ul>
 *
 * @author Vaadin Ltd
 */
public class GridRenderer {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GridRenderer.class);

    private static final String DEFAULT_EMPTY_STATE_TEXT = "No results";

    private final DatabaseProvider databaseProvider;

    /**
     * Creates a new grid renderer.
     *
     * @param databaseProvider
     *            the database provider for query execution, not {@code null}
     */
    public GridRenderer(DatabaseProvider databaseProvider) {
        this.databaseProvider = Objects.requireNonNull(databaseProvider,
                "databaseProvider must not be null");
    }

    /**
     * Applies pending state from the grid's {@link GridEntry} if present. After
     * applying, the pending state is cleared.
     *
     * @param grid
     *            the grid to update, not {@code null}
     */
    @SuppressWarnings("unchecked")
    public void applyPendingState(Grid<?> grid) {
        GridEntry entry = GridEntry.get(grid);
        if (entry == null || !entry.hasPendingState()) {
            return;
        }
        try {
            renderGrid((Grid<Map<String, Object>>) grid, entry.getQuery());
        } finally {
            entry.clearPendingState();
        }
    }

    /**
     * Renders a grid by executing a sample query for column detection, then
     * setting up type-aware columns and a lazy data provider.
     *
     * @param grid
     *            the grid to render, not {@code null}
     * @param sqlQuery
     *            the base SQL query (without LIMIT/OFFSET), not {@code null}
     */
    public void renderGrid(Grid<Map<String, Object>> grid, String sqlQuery) {
        var sampleRows = databaseProvider
                .executeQuery(wrapWithLimit(sqlQuery, 1));

        grid.getUI().ifPresentOrElse(
                ui -> ui.access(
                        () -> doRenderGrid(grid, sqlQuery, sampleRows)),
                () -> doRenderGrid(grid, sqlQuery, sampleRows));
    }

    private void doRenderGrid(Grid<Map<String, Object>> grid, String sqlQuery,
            List<Map<String, Object>> sampleRows) {
        try {
            removeExtraHeaderRows(grid);
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
                addColumn(grid, entry.getKey(), entry.getValue());
            }

            applyColumnGrouping(grid);
            setupLazyDataProvider(grid, sqlQuery);
            LOGGER.info("Grid configured with {} columns", firstRow.size());
        } catch (Exception e) {
            LOGGER.error("Error rendering grid", e);
        }
    }

    // --- Column setup ---

    /**
     * Adds a column to the grid with type-appropriate rendering, alignment, and
     * resizing based on the sample value.
     */
    private static void addColumn(Grid<Map<String, Object>> grid,
            String columnName, Object sampleValue) {
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
    private static void applyColumnGrouping(Grid<Map<String, Object>> grid) {
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
    private static void removeExtraHeaderRows(Grid<?> grid) {
        var headerRows = grid.getHeaderRows();
        while (headerRows.size() > 1) {
            grid.removeHeaderRow(headerRows.getFirst());
            headerRows = grid.getHeaderRows();
        }
    }

    // --- Data provider ---

    private void setupLazyDataProvider(Grid<Map<String, Object>> grid,
            String query) {
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

    static String enrichQuery(String query, int offset, int limit,
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

    static String wrapWithLimit(String query, int limit) {
        return "SELECT * FROM (" + query + ") AS _limited LIMIT " + limit;
    }

    static String wrapWithCount(String query) {
        return "SELECT COUNT(*) FROM (" + query + ") AS _counted";
    }
}
