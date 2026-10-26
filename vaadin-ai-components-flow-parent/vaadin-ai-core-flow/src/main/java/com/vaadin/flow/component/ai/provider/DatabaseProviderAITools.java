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
 * Factory for creating reusable data source {@link LLMProvider.ToolSpec}
 * instances. These tools can be used by any controller that works with a
 * {@link QueryableProvider}.
 *
 * @author Vaadin Ltd
 * @since 25.3
 */
public final class DatabaseProviderAITools {

    private DatabaseProviderAITools() {
    }

    /**
     * Creates a tool that retrieves the schema from the
     * {@link QueryableProvider}. The description names the provider's query
     * language.
     *
     * @param provider
     *            the provider, not {@code null}
     * @return the tool definition, never {@code null}
     */
    public static LLMProvider.ToolSpec getDatabaseSchema(
            QueryableProvider provider) {
        Objects.requireNonNull(provider, "provider must not be null");
        var language = provider.getQueryLanguage().getName();
        var description = "Gets the schema the " + language
                + " queries run against: the tables or types, their columns "
                + "or fields with types, and notes on writing " + language
                + " queries.";
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "get_database_schema";
            }

            @Override
            public String getDescription() {
                return description;
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
     *            the provider, not {@code null}
     * @return a list of all database provider tools, never {@code null}
     */
    public static List<LLMProvider.ToolSpec> createAll(
            QueryableProvider provider) {
        return List.of(getDatabaseSchema(provider));
    }
}
