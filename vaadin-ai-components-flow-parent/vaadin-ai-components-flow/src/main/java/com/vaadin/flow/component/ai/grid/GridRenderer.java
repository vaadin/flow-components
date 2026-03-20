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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.grid.Grid;

/**
 * Applies pending grid state and renders grid data. Encapsulates the
 * query-execution-to-setItems pipeline so that different controllers
 * (standalone grid, dashboard) share the same rendering logic.
 *
 * @author Vaadin Ltd
 */
public class GridRenderer {

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
     * Renders a grid by executing a query and setting up columns and items.
     *
     * @param grid
     *            the grid to render, not {@code null}
     * @param sqlQuery
     *            the SQL query to execute, not {@code null}
     */
    public void renderGrid(Grid<Map<String, Object>> grid, String sqlQuery) {
        List<Map<String, Object>> results = databaseProvider
                .executeQuery(sqlQuery);

        grid.getUI().ifPresentOrElse(ui -> {
            ui.access(() -> {
                grid.removeAllColumns();
                if (!results.isEmpty()) {
                    for (String columnName : results.get(0).keySet()) {
                        grid.addColumn(row -> row.get(columnName) != null
                                ? row.get(columnName).toString()
                                : "").setHeader(columnName).setAutoWidth(true)
                                .setSortable(true);
                    }
                }
                grid.setItems(results);
            });
        }, () -> {
            throw new IllegalStateException("Grid is not attached to a UI");
        });
    }
}
