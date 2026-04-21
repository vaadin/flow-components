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

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;

/**
 * Stateless utility for rendering grid data from SQL queries. Handles column
 * creation, type-based rendering, column grouping, and lazy loading via
 * {@link CallbackDataProvider}.
 *
 * @author Vaadin Ltd
 * @see GridAIController
 */
public final class GridRenderer implements Serializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GridRenderer.class);

    private static final String DEFAULT_EMPTY_STATE_TEXT = "No results";

    private GridRenderer() {
    }

    /**
     * Renders the grid with results from the given SQL query. Columns are
     * created dynamically from the query result, with type-appropriate
     * renderers, grouping, and lazy loading.
     * <p>
     * The caller is responsible for ensuring this method runs on the UI thread
     * (e.g., via {@code runWhenAttached} and {@code UI.access()}).
     *
     * @param grid
     *            the grid to render, not {@code null}
     * @param databaseProvider
     *            the database provider for query execution, not {@code null}
     * @param query
     *            the SQL SELECT query, not {@code null}
     */
    public static void renderGrid(Grid<AIDataRow> grid,
            DatabaseProvider databaseProvider, String query) {
        var sampleRows = databaseProvider.executeQuery(wrapWithLimit(query, 1));
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

        var firstRow = new AIDataRow(sampleRows.getFirst());
        var sortedColumns = new ArrayList<>(firstRow.entries());
        sortedColumns.sort((a, b) -> {
            var prefixA = GridFormatting.groupPrefix(a.getKey());
            var prefixB = GridFormatting.groupPrefix(b.getKey());
            return prefixA.compareTo(prefixB);
        });
        for (var entry : sortedColumns) {
            addColumn(grid, entry.getKey(), entry.getValue());
        }
        applyColumnGrouping(grid);
        var dataProvider = createDataProvider(databaseProvider, query);
        grid.setItems(dataProvider);
        LOGGER.info("Grid configured with {} columns", sortedColumns.size());
    }

    private static void addColumn(Grid<AIDataRow> grid, String columnName,
            Object sampleValue) {
        var header = GridFormatting
                .formatHeader(GridFormatting.stripGroupPrefix(columnName));

        Grid.Column<AIDataRow> column;

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

    private static void applyColumnGrouping(Grid<AIDataRow> grid) {
        var groups = new LinkedHashMap<String, List<Grid.Column<AIDataRow>>>();
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

    private static void removeExtraHeaderRows(Grid<AIDataRow> grid) {
        var headerRows = grid.getHeaderRows();
        while (headerRows.size() > 1) {
            grid.removeHeaderRow(headerRows.getFirst());
            headerRows = grid.getHeaderRows();
        }
    }

    private static DataProvider<AIDataRow, Void> createDataProvider(
            DatabaseProvider databaseProvider, String query) {
        var countQuery = wrapWithCount(query);
        return new CallbackDataProvider<>(fetchQuery -> {
            var sql = enrichQuery(query, fetchQuery.getOffset(),
                    fetchQuery.getLimit(), fetchQuery.getSortOrders());
            LOGGER.debug("Fetching rows: {}", sql);
            return databaseProvider.executeQuery(sql).stream()
                    .map(AIDataRow::new);
        }, countFetchQuery -> {
            LOGGER.debug("Counting rows: {}", countQuery);
            var countResult = databaseProvider.executeQuery(countQuery);
            if (countResult.isEmpty()) {
                return 0;
            }
            var entries = new AIDataRow(countResult.getFirst()).entries();
            if (entries.isEmpty()) {
                return 0;
            }
            var firstValue = entries.iterator().next().getValue();
            return firstValue instanceof Number n ? n.intValue() : 0;
        }, row -> row);
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
