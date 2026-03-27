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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ComponentUtil;
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
 * Renders grid data from SQL queries. Handles column creation, type-based
 * rendering, column grouping, and lazy loading via
 * {@link CallbackDataProvider}.
 *
 * @author Vaadin Ltd
 * @see GridAIController
 */
public class GridRenderer implements Serializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GridRenderer.class);

    private static final String DEFAULT_EMPTY_STATE_TEXT = "No results";

    private static final String CURRENT_QUERY_KEY = "GridRenderer.currentQuery";

    private final Grid<Map<String, Object>> grid;
    private final DatabaseProvider databaseProvider;

    /**
     * Creates a new grid renderer.
     *
     * @param grid
     *            the grid to render, not {@code null}
     * @param databaseProvider
     *            the database provider for query execution, not {@code null}
     */
    public GridRenderer(Grid<Map<String, Object>> grid,
            DatabaseProvider databaseProvider) {
        this.grid = Objects.requireNonNull(grid, "Grid must not be null");
        this.databaseProvider = Objects.requireNonNull(databaseProvider,
                "databaseProvider must not be null");
    }

    /**
     * Returns the current query stored on the grid, or {@code null} if no query
     * has been applied yet.
     *
     * @return the current SQL query, or {@code null}
     */
    public String getCurrentQuery() {
        return (String) ComponentUtil.getData(grid, CURRENT_QUERY_KEY);
    }

    /**
     * Renders the grid with results from the given SQL query. Columns are
     * created dynamically from the query result, with type-appropriate
     * renderers, grouping, and lazy loading.
     *
     * @param query
     *            the SQL SELECT query, not {@code null}
     */
    public void renderGrid(String query) {
        grid.getElement().getNode().runWhenAttached(ui -> ui.access(() -> {
            try {
                doRenderGrid(query);
                ComponentUtil.setData(grid, CURRENT_QUERY_KEY, query);
            } catch (Exception e) {
                LOGGER.error("Error rendering grid", e);
            }
        }));
    }

    private void doRenderGrid(String query) {
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

    private void applyColumnGrouping() {
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
