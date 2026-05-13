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
package com.vaadin.flow.component.ai.form;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.ai.provider.LLMProvider;

import tools.jackson.databind.JsonNode;

/**
 * Factory for the {@link LLMProvider.ToolSpec ToolSpec} instances exposed by
 * {@link FormAIController}.
 * <p>
 * Tools delegate back to the controller through a {@link Callbacks} instance,
 * keeping the JSON serialization concerns here and the field map and side
 * effects in the controller.
 * <p>
 * Intended only for internal use and can be removed in the future.
 *
 * @author Vaadin Ltd
 */
public final class FormAITools {

    private FormAITools() {
    }

    /**
     * Callback contract used by the tools to talk back to the controller
     * without needing direct access to the field map.
     */
    public interface Callbacks extends Serializable {

        /**
         * Invokes the queryable callback for the given field. Implementations
         * should throw if the field is unknown or not queryable; the message is
         * surfaced to the LLM verbatim.
         */
        List<Object> queryFieldOptions(String fieldId, String filter,
                int limit);
    }

    /**
     * Creates the {@code query_field_options} tool spec.
     */
    static LLMProvider.ToolSpec queryFieldOptions(Callbacks callbacks) {
        Objects.requireNonNull(callbacks, "callbacks must not be null");
        return new LLMProvider.ToolSpec() {

            static final int QUERY_OPTIONS_DEFAULT_LIMIT = 50;
            static final int QUERY_OPTIONS_MAX_LIMIT = 200;

            @Override
            public String getName() {
                return "query_field_options";
            }

            @Override
            public String getDescription() {
                return """
                        Look up option labels for a queryable field. Use the field id \
                        from get_form_state. Pass an empty filter for a top-N sample.""";
            }

            @Override
            public String getParametersSchema() {
                return """
                        {
                        "type": "object",
                        "properties": {
                            "field": {
                            "type": "string",
                            "description": "Field id (from get_form_state) to query."
                            },
                            "filter": {
                            "type": "string",
                            "description": "User-typed-style search string; empty for a top-N sample."
                            },
                            "limit": {
                            "type": "integer",
                            "description": "Maximum number of items to return; capped server-side."
                            }
                        },
                        "required": ["field", "filter"]
                        }""";
            }

            @Override
            public String execute(JsonNode arguments) {
                if (arguments == null || !arguments.isObject()) {
                    return "Error: arguments must be a JSON object.";
                }
                var fieldId = arguments.path("field").asString(null);
                if (fieldId == null) {
                    return "Error: missing 'field' argument.";
                }
                var filter = arguments.path("filter").asString("");
                var limit = arguments.path("limit")
                        .asInt(QUERY_OPTIONS_DEFAULT_LIMIT);
                if (limit <= 0) {
                    limit = QUERY_OPTIONS_DEFAULT_LIMIT;
                }
                var truncated = false;
                if (limit > QUERY_OPTIONS_MAX_LIMIT) {
                    limit = QUERY_OPTIONS_MAX_LIMIT;
                    truncated = true;
                }
                List<Object> items;
                try {
                    items = callbacks.queryFieldOptions(fieldId, filter, limit);
                } catch (Exception ex) {
                    return "Error: queryable failed: " + ex.getMessage();
                }
                if (items.size() > QUERY_OPTIONS_MAX_LIMIT) {
                    items = items.subList(0, QUERY_OPTIONS_MAX_LIMIT);
                    truncated = true;
                }
                var b = new StringBuilder();
                for (var item : items) {
                    b.append(String.valueOf(item)).append('\n');
                }
                if (truncated) {
                    b.append("(truncated to ").append(QUERY_OPTIONS_MAX_LIMIT)
                            .append(" items)\n");
                }
                return b.toString();
            }
        };
    }

    /**
     * Creates all form tools for the given callbacks.
     */
    public static List<LLMProvider.ToolSpec> createAll(Callbacks callbacks) {
        Objects.requireNonNull(callbacks, "callbacks must not be null");
        return List.of(queryFieldOptions(callbacks));
    }
}
