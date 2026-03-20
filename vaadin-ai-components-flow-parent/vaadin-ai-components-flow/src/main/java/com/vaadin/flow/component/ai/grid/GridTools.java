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
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Factory for creating reusable grid {@link LLMProvider.ToolSpec} instances.
 * These tools are not tied to any specific controller and can be used by both
 * standalone grid controllers and dashboard controllers.
 * <p>
 * The tools use a {@code gridId} parameter to identify which grid to operate
 * on, allowing a single set of tools to manage multiple grids (e.g., in a
 * dashboard). Grid state ({@link GridEntry}) is stored directly on each
 * {@link Grid} instance via {@link GridEntry#getOrCreate(Grid, String)}.
 * </p>
 *
 * @author Vaadin Ltd
 */
public final class GridTools {

    private GridTools() {
    }

    /**
     * Resolves the grid ID from the tool arguments. If {@code gridId} is not
     * provided and there is exactly one grid, that grid's ID is used as the
     * default.
     */
    private static String resolveGridId(JsonNode args,
            Supplier<Set<String>> gridIdsSupplier) {
        JsonNode idNode = args.get("gridId");
        if (idNode != null && !idNode.isNull()) {
            return idNode.asString();
        }
        var ids = gridIdsSupplier.get();
        if (ids.size() == 1) {
            return ids.iterator().next();
        }
        throw new IllegalArgumentException(
                "gridId is required when multiple grids exist. "
                        + "Available grid IDs: " + ids);
    }

    /**
     * Resolves a grid by ID, throwing if not found.
     */
    private static Grid<Map<String, Object>> resolveGrid(String gridId,
            Function<String, Grid<Map<String, Object>>> gridResolver) {
        Grid<Map<String, Object>> grid = gridResolver.apply(gridId);
        if (grid == null) {
            throw new IllegalArgumentException(
                    "No grid found with ID '" + gridId + "'");
        }
        return grid;
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
                2. get_grid_state(gridId) - Returns current grid state (query)
                3. update_grid_data(gridId, query) - Updates grid data with SQL SELECT query

                WORKFLOW:
                1. ALWAYS call get_grid_state() FIRST before making changes
                2. Use getSchema() to understand available data
                3. Use update_grid_data() to populate the grid with query results

                The grid automatically creates columns based on query result columns.
                Column headers are derived from SQL column names/aliases.
                Use SQL aliases (AS) to provide human-readable column headers.

                Example: SELECT name AS "Employee Name", salary AS "Salary" FROM employees
                """;
    }

    /**
     * Creates a tool that retrieves the current state of a grid, including its
     * SQL query.
     *
     * @param gridResolver
     *            resolves a grid ID to a {@link Grid} instance, returning
     *            {@code null} if not found; not {@code null}
     * @param gridIdsSupplier
     *            supplies the set of available grid IDs; not {@code null}
     * @return the tool definition, never {@code null}
     */
    public static LLMProvider.ToolSpec getGridState(
            Function<String, Grid<Map<String, Object>>> gridResolver,
            Supplier<Set<String>> gridIdsSupplier) {
        Objects.requireNonNull(gridResolver,
                "gridResolver must not be null");
        Objects.requireNonNull(gridIdsSupplier,
                "gridIdsSupplier must not be null");
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "get_grid_state";
            }

            @Override
            public String getDescription() {
                return "Returns the current state of the grid including "
                        + "the SQL query. Takes no parameters when there "
                        + "is only one grid.";
            }

            @Override
            public String getParametersSchema() {
                return """
                        {
                          "type": "object",
                          "properties": {
                            "gridId": {
                              "type": "string",
                              "description": "The ID of the grid. Optional when there is only one grid."
                            }
                          }
                        }""";
            }

            @Override
            public String execute(String arguments) {
                JsonNode args = JacksonUtils.readTree(arguments);
                String gridId = resolveGridId(args, gridIdsSupplier);
                Grid<Map<String, Object>> grid = resolveGrid(gridId,
                        gridResolver);
                GridEntry entry = GridEntry.getOrCreate(grid, gridId);

                ObjectNode result = JacksonUtils.createObjectNode();
                result.put("gridId", gridId);

                String query = entry.getQuery();
                if (query != null) {
                    result.put("query", query);
                } else {
                    result.put("status", "empty");
                    result.put("message",
                            "No grid data has been loaded yet");
                }
                return result.toString();
            }
        };
    }

    /**
     * Creates a tool that updates a grid's data source query. The query is
     * validated and stored as pending state, applied when the request completes.
     *
     * @param gridResolver
     *            resolves a grid ID to a {@link Grid} instance, returning
     *            {@code null} if not found; not {@code null}
     * @param gridIdsSupplier
     *            supplies the set of available grid IDs; not {@code null}
     * @param queryValidator
     *            validates SQL queries before accepting them, or {@code null}
     *            to skip validation
     * @return the tool definition, never {@code null}
     */
    public static LLMProvider.ToolSpec updateGridData(
            Function<String, Grid<Map<String, Object>>> gridResolver,
            Supplier<Set<String>> gridIdsSupplier,
            Consumer<String> queryValidator) {
        Objects.requireNonNull(gridResolver,
                "gridResolver must not be null");
        Objects.requireNonNull(gridIdsSupplier,
                "gridIdsSupplier must not be null");
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "update_grid_data";
            }

            @Override
            public String getDescription() {
                return """
                    Updates the grid data using a SQL SELECT query.
                    The grid will automatically create columns based on the query result columns.
                    Use SQL column aliases (AS) to provide human-readable column headers.

                    Example: SELECT name AS "Employee Name", salary AS "Salary" FROM employees

                    Parameters:
                    - gridId (string): The ID of the grid. Optional when there is only one grid.
                    - query (string, required): SQL SELECT query to retrieve data

                    Changes are applied when the request completes.""";
            }

            @Override
            public String getParametersSchema() {
                return """
                        {
                          "type": "object",
                          "properties": {
                            "gridId": {
                              "type": "string",
                              "description": "The ID of the grid. Optional when there is only one grid."
                            },
                            "query": {
                              "type": "string",
                              "description": "SQL SELECT query to retrieve data"
                            }
                          },
                          "required": ["query"]
                        }""";
            }

            @Override
            public String execute(String arguments) {
                try {
                    JsonNode args = JacksonUtils.readTree(arguments);
                    String gridId = resolveGridId(args, gridIdsSupplier);
                    Grid<Map<String, Object>> grid = resolveGrid(gridId,
                            gridResolver);
                    GridEntry entry = GridEntry.getOrCreate(grid, gridId);

                    String query = args.get("query").asString();

                    if (queryValidator != null) {
                        queryValidator.accept(query);
                    }

                    entry.setQuery(query);
                    entry.setPendingDataUpdate(true);

                    return "Grid '" + gridId
                            + "' data update queued successfully. "
                            + "Changes will be applied when the request completes.";
                } catch (Exception e) {
                    return "Error updating grid data: " + e.getMessage();
                }
            }
        };
    }

    /**
     * Creates all grid tools for the given grid resolver.
     *
     * @param gridResolver
     *            resolves a grid ID to a {@link Grid} instance, returning
     *            {@code null} if not found; not {@code null}
     * @param gridIdsSupplier
     *            supplies the set of available grid IDs; not {@code null}
     * @param queryValidator
     *            validates SQL queries before accepting them, or {@code null}
     *            to skip validation
     * @return a list of all grid tools, never {@code null}
     */
    public static List<LLMProvider.ToolSpec> createAll(
            Function<String, Grid<Map<String, Object>>> gridResolver,
            Supplier<Set<String>> gridIdsSupplier,
            Consumer<String> queryValidator) {
        return List.of(
                getGridState(gridResolver, gridIdsSupplier),
                updateGridData(gridResolver, gridIdsSupplier,
                        queryValidator));
    }
}
