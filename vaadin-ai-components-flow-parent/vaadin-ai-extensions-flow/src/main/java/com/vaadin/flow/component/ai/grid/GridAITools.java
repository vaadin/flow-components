/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.grid;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.common.QueryLanguage;
import com.vaadin.flow.component.ai.extensions.AIExtensionsLicense;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.provider.ToolException;

import tools.jackson.databind.JsonNode;

/**
 * Factory for creating reusable grid {@link LLMProvider.ToolSpec} instances.
 * <p>
 * The tools use a {@code gridId} parameter to identify which grid to operate
 * on, allowing a single set of tools to manage multiple grids (e.g., in a
 * dashboard). Callers provide a {@link Callbacks} implementation for state
 * retrieval and mutation, keeping this class decoupled from {@code Grid}.
 * </p>
 * <p>
 * Intended only for internal use and can be removed in the future.
 * </p>
 *
 * @author Vaadin Ltd
 * @since 25.3
 */
public final class GridAITools {

    /**
     * Tail of the error a tool returns when it failed for a reason the LLM was
     * given no detail about, because the cause was not a {@link ToolException}
     * and so is not safe to pass on. Repeating the call unchanged can only fail
     * the same way, which is what the LLM did without this; a rewritten attempt
     * is still worth one try, since the cause is often something the LLM can
     * avoid by itself, such as an identifier the database reserves.
     */
    private static final String RETRY_ONCE = " No details about the cause are "
            + "available. Do not repeat the same query: change it and try "
            + "once more, or report the failure if you cannot.";

    /**
     * Description of the {@code update_grid_data} tool, with the query
     * language's name, rules and examples filled in from the callbacks.
     */
    private static final String UPDATE_GRID_DATA_DESCRIPTION = """
            Updates the grid data using a {LANGUAGE} query. \
            The grid creates its columns from the names of the result columns.

            {LANGUAGE} RULES:
            {RULES}

            GRID RULES:
            - ALWAYS give every column a human-readable name; it becomes the column header
            - Do NOT limit or page the rows in the query — the grid fetches them in pages
            - Use ONLY the names listed by get_database_schema()
            Shape (placeholders, not real names):
            {SHAPE_EXAMPLE}

            COLUMN GROUPING (only when the user asks for grouping):
            When the user mentions "grouped under X" in their request:
            1. Select ONLY the columns mentioned for grouping
            2. Name each column "X.ReadableName" (with the group prefix and a dot)
            3. Do NOT include columns that are not part of a group
            Example request: "ColumnA and ColumnB grouped under GroupName"
            Correct query: {GROUPING_EXAMPLE}
            Result: A "GroupName" header spanning both columns.
            Do NOT use "X.Name" format unless the user asks for grouping.
            """;

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GridAITools.class);

    private GridAITools() {
    }

    /**
     * Callback interface for grid state access and mutation.
     */
    public interface Callbacks extends Serializable {

        /**
         * Returns the current state of a grid as a JSON string. Should throw if
         * the grid is not found.
         *
         * @param gridId
         *            the grid ID
         * @return the grid state as JSON
         */
        String getState(String gridId);

        /**
         * Handles a query for the given grid. Implementations should validate
         * the query and store it for deferred rendering. Should throw if the
         * grid is not found or the query is invalid: a {@link ToolException}
         * relays its message to the LLM so it can correct the query, any other
         * exception is replaced with a generic error message.
         *
         * @param gridId
         *            the grid ID
         * @param query
         *            the query, in the language of {@link #getQueryLanguage()}
         */
        void updateData(String gridId, String query);

        /**
         * Returns the set of available grid IDs.
         *
         * @return the grid IDs, never {@code null}
         */
        Set<String> getGridIds();

        /**
         * Returns the language the LLM writes the queries in. The tools name it
         * and show its rules and examples in their descriptions, which the LLM
         * providers cache, so the language must be the same on every call.
         * Defaults to {@link QueryLanguage#SQL}.
         *
         * @return the query language, never {@code null}
         * @since 25.4
         */
        default QueryLanguage getQueryLanguage() {
            return QueryLanguage.SQL;
        }
    }

    /**
     * Resolves the grid ID from the tool arguments. If {@code gridId} is not
     * provided and there is exactly one grid, that grid's ID is used.
     */
    private static String resolveGridId(JsonNode args, Callbacks callbacks) {
        var idNode = args.get("gridId");
        if (idNode != null && !idNode.isNull()) {
            return idNode.asString();
        }
        var ids = callbacks.getGridIds();
        if (ids.size() == 1) {
            return ids.iterator().next();
        }
        if (ids.isEmpty()) {
            throw new ToolException("No grids available.");
        }
        throw new ToolException("gridId is required when multiple grids exist. "
                + "Available grid IDs: " + ids);
    }

    /**
     * Creates a tool that returns the current grid state.
     *
     * @param callbacks
     *            the callbacks for grid state access, not {@code null}
     * @return the tool definition, never {@code null}
     */
    static LLMProvider.ToolSpec getGridState(Callbacks callbacks) {
        Objects.requireNonNull(callbacks, "callbacks must not be null");
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "get_grid_state";
            }

            @Override
            public String getDescription() {
                return "Returns the current grid state including its query.";
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
            public String execute(JsonNode arguments) {
                try {
                    LOGGER.info("get_grid_state called");
                    var gridId = resolveGridId(arguments, callbacks);
                    return callbacks.getState(gridId);
                } catch (ToolException e) {
                    LOGGER.warn("get_grid_state validation failed", e);
                    return "Error getting grid state: " + e.getMessage();
                } catch (Exception e) {
                    LOGGER.error("get_grid_state failed", e);
                    return "Error getting grid state.";
                }
            }
        };
    }

    /**
     * Creates a tool that updates the grid data with a query. The description
     * names the callbacks' query language and shows its rules and examples. If
     * the handler throws a {@link ToolException}, its message is returned to
     * the LLM; any other exception is replaced with a generic error.
     *
     * @param callbacks
     *            the callbacks for grid mutation, not {@code null}
     * @return the tool definition, never {@code null}
     */
    static LLMProvider.ToolSpec updateGridData(Callbacks callbacks) {
        Objects.requireNonNull(callbacks, "callbacks must not be null");
        var description = describeUpdateGridData(callbacks.getQueryLanguage());
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "update_grid_data";
            }

            @Override
            public String getDescription() {
                return description;
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
                              "description": "The query, listing specific columns with human-readable names. Do not limit or page the result."
                            }
                          },
                          "required": ["query"]
                        }""";
            }

            @Override
            public String execute(JsonNode arguments) {
                try {
                    LOGGER.info("update_grid_data called with: {}", arguments);
                    var gridId = resolveGridId(arguments, callbacks);
                    var query = arguments.get("query").asString();
                    LOGGER.info("update_grid_data gridId={} query={}", gridId,
                            query);
                    callbacks.updateData(gridId, query);
                    return "Grid '" + gridId
                            + "' data update queued successfully";
                } catch (ToolException e) {
                    LOGGER.warn("update_grid_data validation failed", e);
                    return "Error updating grid data: " + e.getMessage();
                } catch (Exception e) {
                    LOGGER.error("update_grid_data failed", e);
                    return "Error updating grid data." + RETRY_ONCE;
                }
            }
        };
    }

    /**
     * Fills the language into the description template. The replacements are
     * plain, not format strings, as the rules of a language may contain any
     * character.
     */
    private static String describeUpdateGridData(QueryLanguage language) {
        var placeholders = List.of("ColumnA", "ColumnB");
        return UPDATE_GRID_DATA_DESCRIPTION
                .replace("{SHAPE_EXAMPLE}",
                        language.exampleQuery("TableName", placeholders,
                                List.of("Readable A", "Readable B")))
                .replace("{GROUPING_EXAMPLE}",
                        language.exampleQuery("TableName", placeholders,
                                List.of("GroupName.Readable A",
                                        "GroupName.Readable B")))
                .replace("{RULES}", language.getRules())
                .replace("{LANGUAGE}", language.getName());
    }

    /**
     * Creates all grid tools for the given callbacks.
     *
     * @param callbacks
     *            the callbacks for grid state access and mutation, not
     *            {@code null}
     * @return a list of all grid tools, never {@code null}
     */
    public static List<LLMProvider.ToolSpec> createAll(Callbacks callbacks) {
        AIExtensionsLicense.check();
        Objects.requireNonNull(callbacks, "callbacks must not be null");
        return List.of(getGridState(callbacks), updateGridData(callbacks));
    }
}
