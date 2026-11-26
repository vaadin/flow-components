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
package com.vaadin.flow.component.ai.provider;

import java.util.List;
import java.util.Objects;

import tools.jackson.databind.JsonNode;

/**
 * Factory for creating reusable database {@link LlmProvider.ToolSpec}
 * instances. These tools can be used by any controller that works with a
 * {@link QueryExecutor}.
 *
 * @author Vaadin Ltd
 * @since 25.3
 */
public final class QueryExecutorAiTools {

    private QueryExecutorAiTools() {
    }

    /**
     * Creates a tool that retrieves the database schema from the
     * {@link QueryExecutor}.
     *
     * @param provider
     *            the query executor, not {@code null}
     * @return the tool definition, never {@code null}
     */
    public static LlmProvider.ToolSpec getDatabaseSchema(
            QueryExecutor provider) {
        Objects.requireNonNull(provider, "provider must not be null");
        return new LlmProvider.ToolSpec() {
            @Override
            public String getName() {
                return "get_database_schema";
            }

            @Override
            public String getDescription() {
                return "Gets the database schema including table names, "
                        + "column names with their types, and the SQL dialect.";
            }

            @Override
            public String getParametersSchema() {
                return null;
            }

            @Override
            public String execute(JsonNode arguments) {
                return provider.getSchema();
            }
        };
    }

    /**
     * Creates all query executor tools for the given executor.
     *
     * @param provider
     *            the query executor, not {@code null}
     * @return a list of all query executor tools, never {@code null}
     */
    public static List<LlmProvider.ToolSpec> createAll(QueryExecutor provider) {
        return List.of(getDatabaseSchema(provider));
    }
}
