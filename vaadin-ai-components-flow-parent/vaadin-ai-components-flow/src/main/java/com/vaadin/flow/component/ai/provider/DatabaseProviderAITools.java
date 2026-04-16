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
 * Factory for creating reusable database {@link LLMProvider.ToolSpec}
 * instances. These tools can be used by any controller that works with a
 * {@link DatabaseProvider}.
 *
 * @author Vaadin Ltd
 */
public final class DatabaseProviderAITools {

    private DatabaseProviderAITools() {
    }

    /**
     * Creates a tool that retrieves the database schema from the
     * {@link DatabaseProvider}.
     *
     * @param provider
     *            the database provider, not {@code null}
     * @return the tool definition, never {@code null}
     */
    public static LLMProvider.ToolSpec getDatabaseSchema(
            DatabaseProvider provider) {
        Objects.requireNonNull(provider, "provider must not be null");
        return new LLMProvider.ToolSpec() {
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
     * Creates all database provider tools for the given provider.
     *
     * @param provider
     *            the database provider, not {@code null}
     * @return a list of all database provider tools, never {@code null}
     */
    public static List<LLMProvider.ToolSpec> createAll(
            DatabaseProvider provider) {
        return List.of(getDatabaseSchema(provider));
    }
}
