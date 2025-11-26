/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.ai.tool;

import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.internal.JacksonUtils;
import tools.jackson.databind.JsonNode;

import java.util.Objects;
import java.util.function.Function;

/**
 * A builder for creating strongly-typed AI tools programmatically.
 * <p>
 * This builder provides a type-safe, IDE-friendly way to create tools that can
 * be used by LLM providers. Tool handlers receive parsed JSON arguments as
 * {@link JsonNode} objects and can safely access Vaadin components they capture
 * via closures.
 * </p>
 * <p>
 * The orchestrator ensures all tool handlers are executed inside
 * {@code UI.access()}, so they can modify components without extra boilerplate.
 * </p>
 *
 * <h2>Example Usage</h2>
 *
 * <pre>
 * Chart chart = new Chart();
 * DatabaseProvider db = new MyDatabaseProvider();
 *
 * LLMProvider.Tool tool = AiToolBuilder.name("updateChartData")
 *         .description("Executes SQL and updates the sales chart")
 *         .schema("""
 *                 {
 *                   "type": "object",
 *                   "properties": {
 *                     "query": { "type": "string" }
 *                   },
 *                   "required": ["query"]
 *                 }
 *                 """)
 *         .handle(args -> {
 *             String sql = args.get("query").asText();
 *             List&lt;Map&lt;String, Object&gt;&gt; rows = db.executeQuery(sql);
 *             // ... update chart
 *             return "Chart updated successfully";
 *         })
 *         .build();
 * </pre>
 *
 * @author Vaadin Ltd
 */
public final class AiToolBuilder {

    private final String name;
    private String description;
    private String schema;
    private Function<JsonNode, String> handler;

    private AiToolBuilder(String name) {
        this.name = Objects.requireNonNull(name, "Tool name cannot be null");
    }

    /**
     * Creates a new tool builder with the specified name.
     *
     * @param name
     *            the tool name (must be unique within the tool set)
     * @return a new builder instance
     */
    public static AiToolBuilder name(String name) {
        return new AiToolBuilder(name);
    }

    /**
     * Sets the tool description that will be provided to the LLM.
     *
     * @param description
     *            a clear description of what the tool does
     * @return this builder
     */
    public AiToolBuilder description(String description) {
        this.description = Objects.requireNonNull(description,
                "Tool description cannot be null");
        return this;
    }

    /**
     * Sets the JSON schema that defines the tool's parameters.
     * <p>
     * The schema should be a valid JSON Schema object defining the structure
     * and constraints of the tool's input parameters.
     * </p>
     *
     * @param schema
     *            the JSON schema as a string
     * @return this builder
     */
    public AiToolBuilder schema(String schema) {
        this.schema = Objects.requireNonNull(schema,
                "Tool schema cannot be null");
        return this;
    }

    /**
     * Sets the handler function that will be invoked when the tool is called.
     * <p>
     * The handler receives parsed JSON arguments as a {@link JsonNode} and
     * should return a string result that will be sent back to the LLM.
     * </p>
     * <p>
     * The handler can safely access and modify Vaadin components it captures
     * via closure, as the orchestrator ensures execution within
     * {@code UI.access()}.
     * </p>
     *
     * @param handler
     *            the function to handle tool invocations
     * @return this builder
     */
    public AiToolBuilder handle(Function<JsonNode, String> handler) {
        this.handler = Objects.requireNonNull(handler,
                "Tool handler cannot be null");
        return this;
    }

    /**
     * Builds and returns the configured tool.
     *
     * @return a new {@link LLMProvider.Tool} instance
     * @throws IllegalStateException
     *             if any required field is missing
     */
    public LLMProvider.Tool build() {
        if (description == null) {
            throw new IllegalStateException(
                    "Tool description is required but was not set");
        }
        if (schema == null) {
            throw new IllegalStateException(
                    "Tool schema is required but was not set");
        }
        if (handler == null) {
            throw new IllegalStateException(
                    "Tool handler is required but was not set");
        }

        return new BuiltTool(name, description, schema, handler);
    }

    /**
     * Internal implementation of {@link LLMProvider.Tool} created by the
     * builder.
     */
    private static final class BuiltTool implements LLMProvider.Tool {

        private final String name;
        private final String description;
        private final String schema;
        private final Function<JsonNode, String> handler;

        private BuiltTool(String name, String description, String schema,
                Function<JsonNode, String> handler) {
            this.name = name;
            this.description = description;
            this.schema = schema;
            this.handler = handler;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getParametersSchema() {
            return schema;
        }

        @Override
        public String execute(String arguments) {
            try {
                JsonNode args = JacksonUtils.readTree(arguments);
                return handler.apply(args);
            } catch (Exception e) {
                return "Error executing tool: " + e.getMessage();
            }
        }
    }
}
