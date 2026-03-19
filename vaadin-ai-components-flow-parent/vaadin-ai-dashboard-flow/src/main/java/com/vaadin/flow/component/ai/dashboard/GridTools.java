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
package com.vaadin.flow.component.ai.dashboard;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ObjectNode;

/**
 * Provides LLM tool definitions for grid data display operations.
 * <p>
 * This class encapsulates the tools that allow an LLM to query database data
 * and display it in a {@link Grid} component. Each instance is bound to a
 * specific {@link Grid} and {@link DatabaseProvider}.
 * </p>
 * <p>
 * The tools can be used by any controller type. For example, a dashboard
 * controller managing multiple grids can create a {@code GridTools} instance per
 * grid.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class GridTools implements Serializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GridTools.class);

    private final Grid<Map<String, Object>> grid;
    private final DatabaseProvider databaseProvider;

    private String currentSqlQuery;
    private String pendingDataQuery;

    /**
     * Creates a new grid tools instance bound to the given grid and database
     * provider.
     *
     * @param grid
     *            the grid component to update, not {@code null}
     * @param databaseProvider
     *            the database provider for query execution, not {@code null}
     */
    public GridTools(Grid<Map<String, Object>> grid,
            DatabaseProvider databaseProvider) {
        this.grid = Objects.requireNonNull(grid, "Grid cannot be null");
        this.databaseProvider = Objects.requireNonNull(databaseProvider,
                "Database provider cannot be null");
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
                1. getSchema() - Retrieves database schema (tables, columns, types)
                2. getGridCurrentState() - Returns current grid state (query)
                3. updateGridData(query) - Updates grid data with SQL SELECT query

                WORKFLOW:
                1. ALWAYS call getGridCurrentState() FIRST before making changes
                2. Use getSchema() to understand available data
                3. Use updateGridData() to populate the grid with query results

                The grid automatically creates columns based on query result columns.
                Column headers are derived from SQL column names/aliases.
                Use SQL aliases (AS) to provide human-readable column headers.

                Example: SELECT name AS "Employee Name", salary AS "Salary" FROM employees
                """;
    }

    /**
     * Returns the tool definitions for grid operations.
     *
     * @return list of tool definitions
     */
    public List<LLMProvider.ToolSpec> getTools() {
        return List.of(createGetCurrentStateTool(), createUpdateDataTool());
    }

    /**
     * Returns the grid component bound to this tools instance.
     *
     * @return the grid component
     */
    public Grid<Map<String, Object>> getGrid() {
        return grid;
    }

    /**
     * Returns the current SQL query.
     *
     * @return the current SQL query, or {@code null} if no query has been
     *         executed
     */
    public String getCurrentSqlQuery() {
        return currentSqlQuery;
    }

    /**
     * Sets the current SQL query. Used when restoring state.
     *
     * @param sqlQuery
     *            the SQL query to set
     */
    public void setCurrentSqlQuery(String sqlQuery) {
        this.currentSqlQuery = sqlQuery;
    }

    /**
     * Returns the pending data query set by the updateGridData tool.
     *
     * @return the pending data query, or {@code null} if none
     */
    public String getPendingDataQuery() {
        return pendingDataQuery;
    }

    /**
     * Sets the pending data query for deferred rendering.
     *
     * @param query
     *            the SQL query to set as pending
     */
    public void setPendingDataQuery(String query) {
        this.pendingDataQuery = query;
    }

    /**
     * Clears pending data query after rendering.
     */
    public void clearPending() {
        pendingDataQuery = null;
    }

    /**
     * Renders the grid with results from the given SQL query.
     *
     * @param sqlQuery
     *            the SQL query to execute and display
     */
    public void renderGrid(String sqlQuery) {
        List<Map<String, Object>> results = databaseProvider
                .executeQuery(sqlQuery);

        grid.getUI().ifPresentOrElse(currentUI -> {
            currentUI.access(() -> {
                grid.removeAllColumns();
                if (!results.isEmpty()) {
                    for (String columnName : results.get(0).keySet()) {
                        grid.addColumn(
                                row -> row.get(columnName) != null
                                        ? row.get(columnName).toString()
                                        : "")
                                .setHeader(columnName).setAutoWidth(true)
                                .setSortable(true);
                    }
                }
                grid.setItems(results);
            });
        }, () -> {
            throw new IllegalStateException(
                    "Grid is not attached to a UI");
        });
    }

    // ===== Tool Implementations =====

    private LLMProvider.ToolSpec createGetCurrentStateTool() {
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "getGridCurrentState";
            }

            @Override
            public String getDescription() {
                return "Returns the current state of the grid including the SQL query. Takes no parameters.";
            }

            @Override
            public String getParametersSchema() {
                return null;
            }

            @Override
            public String execute(String arguments) {
                if (currentSqlQuery == null) {
                    return "{\"status\":\"empty\",\"message\":\"No grid data has been loaded yet\"}";
                }
                return "{\"query\":\""
                        + currentSqlQuery.replace("\"", "\\\"") + "\"}";
            }
        };
    }

    private LLMProvider.ToolSpec createUpdateDataTool() {
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "updateGridData";
            }

            @Override
            public String getDescription() {
                return """
                    Updates the grid data using a SQL SELECT query.
                    The grid will automatically create columns based on the query result columns.
                    Use SQL column aliases (AS) to provide human-readable column headers.

                    Example: SELECT name AS "Employee Name", salary AS "Salary" FROM employees

                    Parameters: query (string) - SQL SELECT query to retrieve data
                    """;
            }

            @Override
            public String getParametersSchema() {
                return null;
            }

            @Override
            public String execute(String arguments) {
                try {
                    ObjectNode node = (ObjectNode) JacksonUtils
                            .readTree(arguments);
                    String query = node.get("query").asString();

                    // Test the query
                    databaseProvider.executeQuery(query);

                    currentSqlQuery = query;
                    pendingDataQuery = query;

                    return "Grid data update queued successfully";
                } catch (Exception e) {
                    return "Error updating grid data: " + e.getMessage();
                }
            }
        };
    }
}
