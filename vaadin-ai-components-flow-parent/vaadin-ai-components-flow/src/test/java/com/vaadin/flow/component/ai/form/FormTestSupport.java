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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;

/**
 * Common helpers shared by the form-controller test classes: tool lookup, JSON
 * parsing, and small wrappers that drive {@link FormAIController}'s tools the
 * way an LLM would.
 */
final class FormTestSupport {

    private FormTestSupport() {
    }

    static String executeQueryFieldOptions(FormAIController controller,
            HasValue<?, ?> field, String filter, int limit) {
        var fieldId = idOf(field);
        return executeQueryFieldOptions(controller,
                json("{\"field\":\"" + fieldId + "\",\"filter\":\"" + filter
                        + "\",\"limit\":" + limit + "}"));
    }

    static String executeQueryFieldOptions(FormAIController controller,
            JsonNode arguments) {
        return findTool(controller.getTools(), "query_field_options")
                .execute(arguments);
    }

    static LLMProvider.ToolSpec findTool(List<LLMProvider.ToolSpec> tools,
            String name) {
        return tools.stream().filter(t -> t.getName().equals(name)).findFirst()
                .orElseThrow();
    }

    static JsonNode json(String text) {
        return JacksonUtils.readTree(text);
    }

    /**
     * Returns the opaque id that {@link FormAIController} attached to the field
     * at request start. Tests that need to assert against the id (or inject it
     * into a tool argument) read it from here so the field fixture itself does
     * not have to know about the controller's conventions.
     */
    static String idOf(HasValue<?, ?> field) {
        return (String) ComponentUtil.getData((Component) field,
                FormAIController.FIELD_ID_KEY);
    }

    /**
     * Drives {@code get_form_state} and returns the {@code fields} array
     * exploded into a {@link List}, the shape tests assert against.
     */
    static List<JsonNode> formStateFields(FormAIController controller) {
        var result = findTool(controller.getTools(), "get_form_state")
                .execute(JacksonUtils.createObjectNode());
        var root = json(result);
        var out = new ArrayList<JsonNode>();
        root.path("fields").forEach(out::add);
        return out;
    }
}
