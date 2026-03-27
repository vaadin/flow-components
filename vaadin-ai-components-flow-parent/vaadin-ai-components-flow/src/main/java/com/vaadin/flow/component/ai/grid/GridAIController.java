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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.orchestrator.AIController;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.DatabaseProviderAITools;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.grid.Grid;
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
 * {@link DatabaseProviderAITools}. Rendering is handled by
 * {@link GridRenderer}. State changes are deferred and applied in
 * {@link #onRequestCompleted()}.
 * </p>
 *
 * @author Vaadin Ltd
 * @see GridAITools
 * @see GridRenderer
 * @see DatabaseProviderAITools
 */
public class GridAIController implements AIController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GridAIController.class);

    private static final String DEFAULT_GRID_ID = "grid";

    private final Grid<Map<String, Object>> grid;
    private final DatabaseProvider databaseProvider;
    private final GridRenderer gridRenderer;

    private final AtomicReference<String> currentQuery = new AtomicReference<>();
    private final AtomicReference<String> pendingQuery = new AtomicReference<>();

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
        this.gridRenderer = new GridRenderer(databaseProvider);
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
                databaseProvider
                        .executeQuery(GridRenderer.wrapWithLimit(query, 1));
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
            LOGGER.debug("onRequestCompleted: no pending query");
            return;
        }
        LOGGER.info("onRequestCompleted: applying query: {}", query);
        grid.getElement().getNode().runWhenAttached(ui -> ui.access(() -> {
            try {
                gridRenderer.renderGrid(grid, query);
                currentQuery.set(query);
                LOGGER.info("onRequestCompleted: grid updated successfully");
            } catch (Exception e) {
                LOGGER.error("Error updating grid", e);
            }
        }));
    }
}
