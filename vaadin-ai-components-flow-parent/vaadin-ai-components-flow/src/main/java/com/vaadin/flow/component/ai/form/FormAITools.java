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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;

/**
 * Factory for the {@link LLMProvider.ToolSpec ToolSpec} instances exposed by
 * {@link FormAIController}.
 * <p>
 * Intended only for internal use and can be removed in the future.
 *
 * @author Vaadin Ltd
 */
final class FormAITools {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(FormAITools.class);

    static final String FORM_STATE_TOOL = "get_form_state";
    static final String QUERY_OPTIONS_TOOL = "query_field_options";

    private FormAITools() {
    }

    /**
     * Callback contract used by the tools to talk back to the controller
     * without needing direct access to the field map.
     */
    public interface Callbacks {

        /**
         * Returns the entries the tools should expose to the LLM, in document
         * order. Ignored fields and fields whose type is
         * {@link FormFieldType#UNSUPPORTED} must be filtered out by the
         * implementation.
         */
        List<FormFieldEntry> visibleEntries();

        /**
         * Invokes the value-options query callback for the given field and
         * returns the option labels the LLM should see. Throw
         * {@link ToolException} to surface a curated error message to the LLM
         * (for example when the field id is unknown); other exceptions are
         * caught and replaced with a generic error so internal details are not
         * leaked.
         */
        List<String> queryFieldOptions(String fieldId, String filter,
                int limit);
    }

    /**
     * Thrown by a {@link Callbacks} implementation to surface a curated message
     * to the LLM. The exception {@link #getMessage() message} is forwarded
     * verbatim as the tool's error output, so callers must ensure it is safe to
     * expose: no PII, no internal identifiers other than what the LLM already
     * sent, no third-party error text. For any uncontrolled failure throw a
     * regular {@link RuntimeException} instead — the tool will log it and
     * return a generic error.
     */
    public static class ToolException extends RuntimeException {

        public ToolException(String llmFacingMessage) {
            super(llmFacingMessage);
        }
    }

    /**
     * Creates the {@code get_form_state} tool spec. Takes no parameters and
     * returns a JSON document listing every visible field with its id, merged
     * description, type metadata (type/format/pattern/enum/queryable/array/
     * items), and current value.
     */
    static LLMProvider.ToolSpec formState(Callbacks callbacks) {
        return new LLMProvider.ToolSpec() {

            static final String DESCRIPTION = """
                    Returns the current form as JSON: every fillable field's id, \
                    description, type metadata (type/format/pattern/enum/queryable/\
                    array/items), and current value. Call this first so you know \
                    which ids exist and what each one means.""";

            static final String SCHEMA = """
                    {
                        "type": "object",
                        "properties": {}
                    }""";

            @Override
            public String getName() {
                return FORM_STATE_TOOL;
            }

            @Override
            public String getDescription() {
                return DESCRIPTION;
            }

            @Override
            public String getParametersSchema() {
                return SCHEMA;
            }

            @Override
            public String execute(JsonNode arguments) {
                var root = JacksonUtils.createObjectNode();
                var fields = root.putArray("fields");
                try {
                    for (var e : callbacks.visibleEntries()) {
                        fields.add(FormFieldSchema.build(e.id(), e.field(),
                                e.type(), e.hints()));
                    }
                } catch (Exception ex) {
                    LOGGER.warn("get_form_state failed", ex);
                    return "{\"error\":\"get_form_state failed.\"}";
                }
                return root.toString();
            }
        };
    }

    /**
     * Creates the {@code query_field_options} tool spec.
     */
    static LLMProvider.ToolSpec queryFieldOptions(Callbacks callbacks) {
        return new LLMProvider.ToolSpec() {

            static final int QUERY_OPTIONS_DEFAULT_LIMIT = 50;
            static final int QUERY_OPTIONS_MAX_LIMIT = 200;

            @Override
            public String getName() {
                return QUERY_OPTIONS_TOOL;
            }

            @Override
            public String getDescription() {
                return """
                        Look up value-option labels for a field. Use the field id \
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
                if (limit > QUERY_OPTIONS_MAX_LIMIT) {
                    limit = QUERY_OPTIONS_MAX_LIMIT;
                }
                List<String> items;
                try {
                    items = callbacks.queryFieldOptions(fieldId, filter, limit);
                } catch (ToolException ex) {
                    LOGGER.warn("Tool reported user-facing error for field {}",
                            fieldId, ex);
                    return "Error: " + ex.getMessage();
                } catch (Exception ex) {
                    LOGGER.warn("Value-options query failed for field {}",
                            fieldId, ex);
                    return "Error: value-options query failed.";
                }
                var truncated = false;
                if (items.size() > limit) {
                    items = items.subList(0, limit);
                    truncated = true;
                }
                if (items.isEmpty()) {
                    return "(no matches)\n";
                }
                var b = new StringBuilder();
                for (var item : items) {
                    b.append(escapeLabel(item)).append('\n');
                }
                if (truncated) {
                    b.append("(truncated to ").append(limit)
                            .append(" items)\n");
                }
                return b.toString();
            }
        };
    }

    /**
     * Creates all form tools for the given callbacks.
     */
    static List<LLMProvider.ToolSpec> createAll(Callbacks callbacks) {
        var tools = new ArrayList<LLMProvider.ToolSpec>();
        tools.add(formState(callbacks));
        tools.add(queryFieldOptions(callbacks));
        return tools;
    }

    private static String escapeLabel(String label) {
        return label.replace("\\", "\\\\").replace("\n", "\\n");
    }
}
