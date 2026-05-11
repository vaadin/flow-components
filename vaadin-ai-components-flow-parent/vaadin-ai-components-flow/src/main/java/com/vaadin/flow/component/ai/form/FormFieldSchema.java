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

import java.util.List;

import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Builds the per-field JSON Schema fragments embedded into the {@code
 * fill_form} tool definition.
 */
final class FormFieldSchema {

    private FormFieldSchema() {
    }

    /**
     * Builds a JSON Schema node describing one form field. The node includes a
     * merged description (label + helper + custom description + current value
     * suffix) and the type-specific keywords ({@code format}, {@code pattern},
     * {@code enum}, etc.).
     */
    static ObjectNode build(FormFieldEntry e) {
        ObjectNode node = JacksonUtils.createObjectNode();
        String description = mergeDescription(e);
        if (!description.isEmpty()) {
            node.put("description", description);
        }
        switch (e.type) {
        case STRING -> node.put("type", "string");
        case EMAIL -> {
            node.put("type", "string");
            node.put("format", "email");
        }
        case BIG_DECIMAL -> {
            node.put("type", "string");
            node.put("pattern", FormFieldType.BIG_DECIMAL_PATTERN);
        }
        case NUMBER -> node.put("type", "number");
        case INTEGER -> node.put("type", "integer");
        case BOOLEAN -> node.put("type", "boolean");
        case DATE -> {
            node.put("type", "string");
            node.put("format", "date");
        }
        case DATE_TIME -> {
            node.put("type", "string");
            node.put("format", "date-time");
        }
        case TIME -> {
            node.put("type", "string");
            node.put("format", "time");
        }
        case SINGLE_SELECT -> {
            node.put("type", "string");
            addEnum(node, e);
        }
        case MULTI_SELECT -> {
            node.put("type", "array");
            ObjectNode items = node.putObject("items");
            items.put("type", "string");
            addEnum(items, e);
        }
        default -> node.put("type", "string");
        }
        return node;
    }

    private static String mergeDescription(FormFieldEntry e) {
        StringBuilder desc = new StringBuilder();
        if (e.label != null && !e.label.isBlank()) {
            desc.append(e.label);
        }
        if (e.description != null && !e.description.isBlank()) {
            if (desc.length() > 0) {
                desc.append(". ");
            }
            desc.append(e.description);
        }
        if (e.helperText != null && !e.helperText.isBlank()) {
            if (desc.length() > 0) {
                desc.append(". ");
            }
            desc.append(e.helperText);
        }
        Object current = e.field.getValue();
        if (!FormValueConverter.isEmpty(current)) {
            if (desc.length() > 0) {
                desc.append(' ');
            }
            desc.append("(current: ").append(FormValueConverter.displayValue(e))
                    .append(')');
        }
        return desc.toString();
    }

    private static void addEnum(ObjectNode node, FormFieldEntry e) {
        List<String> labels = enumLabels(e);
        if (labels == null || labels.isEmpty()) {
            return;
        }
        ArrayNode arr = node.putArray("enum");
        labels.forEach(arr::add);
    }

    /**
     * Returns the labels of options the LLM may pick from, or {@code null} when
     * the field has no statically-known options (queryable / backend-loaded
     * data providers do not contribute an {@code enum}).
     */
    static List<String> enumLabels(FormFieldEntry e) {
        if (e.allowedValues != null) {
            return e.allowedValues.stream()
                    .map(v -> FormValueConverter.renderItem(e, v)).toList();
        }
        if (e.queryable != null) {
            return null;
        }
        List<Object> items = FormValueConverter.listDataProviderItems(e.field);
        if (items == null) {
            return null;
        }
        return items.stream().map(v -> FormValueConverter.renderItem(e, v))
                .toList();
    }
}
