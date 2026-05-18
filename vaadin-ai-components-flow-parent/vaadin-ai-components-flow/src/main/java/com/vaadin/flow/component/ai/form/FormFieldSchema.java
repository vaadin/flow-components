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

import java.util.Collection;
import java.util.List;

import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ObjectNode;

/**
 * Builds the per-field JSON metadata block returned by the
 * {@code get_form_state} tool. The shape mirrors a JSON Schema fragment (type /
 * format / pattern / enum / array / items / queryable) but it is delivered as a
 * tool result, not as a tool input schema, so the {@code get_form_state} tool
 * definition itself stays static across requests.
 */
final class FormFieldSchema {

    private FormFieldSchema() {
    }

    /**
     * Builds a JSON node describing one form field: id, merged description,
     * type metadata, and the current value.
     */
    static ObjectNode build(String id, HasValue<?, ?> field, FormFieldType type,
            FormFieldHints hints) {
        var node = JacksonUtils.createObjectNode();
        node.put("id", id);
        var description = mergeDescription(field, hints);
        if (!description.isEmpty()) {
            node.put("description", description);
        }
        applyType(node, field, type, hints);
        applyValue(node, field, type);
        return node;
    }

    private static void applyType(ObjectNode node, HasValue<?, ?> field,
            FormFieldType type, FormFieldHints hints) {
        switch (type) {
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
            applySelectionOptions(node, field, hints);
        }
        case MULTI_SELECT -> {
            node.put("array", true);
            var items = node.putObject("items");
            items.put("type", "string");
            applySelectionOptions(items, field, hints);
        }
        default -> node.put("type", "string");
        }
    }

    private static void applySelectionOptions(ObjectNode target,
            HasValue<?, ?> field, FormFieldHints hints) {
        if (hints != null && hints.fixedOptions != null) {
            var arr = target.putArray("enum");
            hints.fixedOptions.forEach(arr::add);
            return;
        }
        if (hints != null && hints.valueOptionsQuery != null) {
            target.put("queryable", true);
            return;
        }
        var items = FormValueConverter.listDataProviderItems(field);
        if (items == null || items.isEmpty()) {
            return;
        }
        var arr = target.putArray("enum");
        for (var item : items) {
            arr.add(FormValueConverter.renderItem(field, item));
        }
    }

    private static void applyValue(ObjectNode node, HasValue<?, ?> field,
            FormFieldType type) {
        var value = field.getValue();
        if (FormValueConverter.isEmpty(value)) {
            node.putNull("value");
            return;
        }
        switch (type) {
        case NUMBER -> {
            if (value instanceof Number n) {
                node.put("value", n.doubleValue());
            } else {
                node.put("value", value.toString());
            }
        }
        case INTEGER -> {
            if (value instanceof Number n) {
                node.put("value", n.longValue());
            } else {
                node.put("value", value.toString());
            }
        }
        case BOOLEAN -> {
            if (value instanceof Boolean b) {
                node.put("value", b);
            } else {
                node.put("value", value.toString());
            }
        }
        case SINGLE_SELECT ->
            node.put("value", FormValueConverter.renderItem(field, value));
        case MULTI_SELECT -> {
            var arr = node.putArray("value");
            if (value instanceof Collection<?> coll) {
                for (var v : coll) {
                    arr.add(FormValueConverter.renderItem(field, v));
                }
            } else {
                arr.add(FormValueConverter.renderItem(field, value));
            }
        }
        default -> node.put("value", value.toString());
        }
    }

    private static String mergeDescription(HasValue<?, ?> field,
            FormFieldHints hints) {
        var desc = new StringBuilder();
        appendPart(desc, label(field));
        appendPart(desc, hints != null ? hints.description : null);
        appendPart(desc, helperText(field));
        return desc.toString();
    }

    private static void appendPart(StringBuilder target, String part) {
        if (part == null || part.isBlank()) {
            return;
        }
        if (target.length() > 0) {
            target.append(". ");
        }
        target.append(part);
    }

    private static String label(HasValue<?, ?> field) {
        return field instanceof HasLabel hl ? hl.getLabel() : null;
    }

    private static String helperText(HasValue<?, ?> field) {
        return field instanceof HasHelper hh ? hh.getHelperText() : null;
    }

    /**
     * For tests: returns the labels that would be emitted in {@code enum} for
     * the given field, or {@code null} when the field would render as
     * {@code queryable} or with no option metadata.
     */
    static List<String> enumLabels(HasValue<?, ?> field, FormFieldHints hints) {
        if (hints != null && hints.fixedOptions != null) {
            return List.copyOf(hints.fixedOptions);
        }
        if (hints != null && hints.valueOptionsQuery != null) {
            return null;
        }
        var items = FormValueConverter.listDataProviderItems(field);
        if (items == null) {
            return null;
        }
        return items.stream().map(v -> FormValueConverter.renderItem(field, v))
                .toList();
    }
}
