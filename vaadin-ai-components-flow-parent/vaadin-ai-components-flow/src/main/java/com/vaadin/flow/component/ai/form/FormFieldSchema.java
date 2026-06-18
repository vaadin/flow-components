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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;

import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ai.form.FormAITools.FormFieldDescriptor;
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

    /**
     * Maximum number of entries emitted into an {@code enum} array. Matches the
     * cap applied by {@code query_field_options} so the LLM never sees more
     * than this many options for any single field regardless of source.
     */
    static final int ENUM_MAX_ITEMS = 200;

    private static final String FIELD_TYPE = "type";
    private static final String FIELD_FORMAT = "format";
    private static final String FIELD_VALUE = "value";
    private static final String TYPE_STRING = "string";

    private FormFieldSchema() {
    }

    /**
     * Builds a JSON node describing one form field: id, merged description,
     * read-only/disabled status, type metadata, and the current value.
     *
     * @param descriptor
     *            the field to describe, not {@code null}
     * @return the field's metadata node
     */
    static ObjectNode build(FormFieldDescriptor descriptor) {
        var field = descriptor.field();
        var type = descriptor.type();
        var hints = descriptor.hints();
        var node = JacksonUtils.createObjectNode();
        node.put("id", descriptor.id());
        var description = mergeDescription(field, hints);
        if (!description.isEmpty()) {
            node.put("description", description);
        }
        // A disabled or read-only field is shown for context only; the flags
        // tell the LLM not to fill it (fill_form rejects writes to it).
        if (descriptor.disabled()) {
            node.put("disabled", true);
        }
        if (descriptor.readOnly()) {
            node.put("readOnly", true);
        }
        applyType(node, field, type, hints);
        if (descriptor.hideValue()) {
            // The value is kept private: render null and flag it, whether or
            // not a value is set, so the LLM never sees the value yet still
            // knows the field exists and can be written.
            node.putNull(FIELD_VALUE);
            node.put("valueHidden", true);
        } else {
            applyValue(node, field, type, hints);
        }
        return node;
    }

    private static void applyType(ObjectNode node, HasValue<?, ?> field,
            FormFieldType type, FormFieldHints hints) {
        if (type == FormFieldType.MULTI_SELECT) {
            node.put("array", true);
            var items = node.putObject("items");
            items.put(FIELD_TYPE, TYPE_STRING);
            applySelectionOptions(items, field, hints);
            return;
        }
        // fieldValueOptions turns any field into a constrained-choice field
        // from the LLM's perspective: the LLM picks a label,
        // valueOptionsToValue converts back. Emit type=string + enum/queryable
        // so the LLM sees the signal regardless of the underlying value type.
        if (type == FormFieldType.SINGLE_SELECT || hasValueOptions(hints)) {
            node.put(FIELD_TYPE, TYPE_STRING);
            applySelectionOptions(node, field, hints);
            return;
        }
        switch (type) {
        case EMAIL -> {
            node.put(FIELD_TYPE, TYPE_STRING);
            node.put(FIELD_FORMAT, "email");
        }
        case BIG_DECIMAL -> {
            node.put(FIELD_TYPE, TYPE_STRING);
            node.put("pattern", FormFieldType.BIG_DECIMAL_PATTERN);
        }
        case NUMBER -> node.put(FIELD_TYPE, "number");
        case INTEGER -> node.put(FIELD_TYPE, "integer");
        case BOOLEAN -> node.put(FIELD_TYPE, "boolean");
        case DATE -> {
            node.put(FIELD_TYPE, TYPE_STRING);
            node.put(FIELD_FORMAT, "date");
        }
        case DATE_TIME -> {
            node.put(FIELD_TYPE, TYPE_STRING);
            node.put(FIELD_FORMAT, "date-time");
        }
        case TIME -> {
            node.put(FIELD_TYPE, TYPE_STRING);
            node.put(FIELD_FORMAT, "time");
        }
        default -> node.put(FIELD_TYPE, TYPE_STRING);
        }
    }

    private static boolean hasValueOptions(FormFieldHints hints) {
        return hints != null && hints.valueOptionsQuery != null;
    }

    private static void applySelectionOptions(ObjectNode target,
            HasValue<?, ?> field, FormFieldHints hints) {
        if (hints != null && hints.fixedOptions) {
            var arr = target.putArray("enum");
            hints.valueOptionsQuery.apply("", ENUM_MAX_ITEMS).stream()
                    .limit(ENUM_MAX_ITEMS).forEach(arr::add);
            return;
        }
        if (hints != null && hints.valueOptionsQuery != null) {
            target.put("queryable", true);
            return;
        }
        var items = FormValueConverter.listDataProviderItems(field);
        if (items.isEmpty()) {
            return;
        }
        var arr = target.putArray("enum");
        items.stream().limit(ENUM_MAX_ITEMS)
                .forEach(item -> arr.add(renderItem(field, hints, item)));
    }

    private static void applyValue(ObjectNode node, HasValue<?, ?> field,
            FormFieldType type, FormFieldHints hints) {
        var value = field.getValue();
        if (FormValueConverter.isEmpty(value)) {
            node.putNull(FIELD_VALUE);
            return;
        }
        // fieldValueOptions rewrites the schema type to "string" +
        // enum/queryable for non-selection fields; render the value as a
        // string so the two halves of the payload agree.
        if (hasValueOptions(hints) && type != FormFieldType.SINGLE_SELECT
                && type != FormFieldType.MULTI_SELECT) {
            node.put(FIELD_VALUE, renderItem(field, hints, value));
            return;
        }
        switch (type) {
        case NUMBER -> applyNumberValue(node, value);
        case INTEGER -> applyIntegerValue(node, value);
        case BIG_DECIMAL ->
            // BigDecimal.toString() emits scientific notation for values
            // with negative scale or adjusted exponent < -6, which
            // violates the BIG_DECIMAL pattern declared in the schema.
            // toPlainString() always produces the canonical decimal
            // representation.
            node.put(FIELD_VALUE, ((BigDecimal) value).toPlainString());
        case BOOLEAN -> node.put(FIELD_VALUE, (Boolean) value);
        case SINGLE_SELECT ->
            node.put(FIELD_VALUE, renderItem(field, hints, value));
        case MULTI_SELECT -> applyMultiSelectValue(node, field, hints, value);
        default -> node.put(FIELD_VALUE, value.toString());
        }
    }

    /**
     * Renders one item to the LLM-facing label. Prefers the resolved
     * valueOptions item-label generator (which honours an explicit
     * {@link ValueOptions#itemLabelGenerator(com.vaadin.flow.component.ItemLabelGenerator)}
     * over the field's own generator) so the value string agrees with the
     * labels surfaced in {@code enum} / {@code query_field_options}. Falls back
     * to {@link FormValueConverter#renderItem} when no valueOptions hint is
     * registered.
     */
    private static String renderItem(HasValue<?, ?> field, FormFieldHints hints,
            Object item) {
        if (hints != null && hints.itemLabelGenerator != null) {
            return hints.itemLabelGenerator.apply(item);
        }
        return FormValueConverter.renderItem(field, item);
    }

    private static void applyNumberValue(ObjectNode node, Object value) {
        // NaN / ±Infinity are valid Java doubles but not legal JSON numbers;
        // surface as null rather than corrupting the payload with a
        // non-standard token in a number-typed slot.
        if (value instanceof Number n && Double.isFinite(n.doubleValue())) {
            node.put(FIELD_VALUE, n.doubleValue());
        } else {
            node.putNull(FIELD_VALUE);
        }
    }

    private static void applyIntegerValue(ObjectNode node, Object value) {
        // BigInteger values can exceed Long.MAX_VALUE; routing them through
        // Number.longValue() silently truncates. Jackson supports BigInteger
        // as a JSON number with full precision.
        if (value instanceof BigInteger bi) {
            node.put(FIELD_VALUE, bi);
        } else {
            node.put(FIELD_VALUE, ((Number) value).longValue());
        }
    }

    private static void applyMultiSelectValue(ObjectNode node,
            HasValue<?, ?> field, FormFieldHints hints, Object value) {
        // MULTI_SELECT is only assigned when the field implements MultiSelect,
        // whose contract guarantees getValue() returns a Set. A non-Collection
        // value would be a contract violation and produces an empty array
        // here as graceful degradation.
        var arr = node.putArray(FIELD_VALUE);
        if (value instanceof Collection<?> coll) {
            for (var v : coll) {
                arr.add(renderItem(field, hints, v));
            }
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
        if (!target.isEmpty()) {
            // Pipe rather than '. ' so labels or helper texts that
            // already end in '.' (common) do not produce '..' in the
            // merged description.
            target.append(" | ");
        }
        target.append(part);
    }

    private static String label(HasValue<?, ?> field) {
        return field instanceof HasLabel hl ? hl.getLabel() : null;
    }

    private static String helperText(HasValue<?, ?> field) {
        return field instanceof HasHelper hh ? hh.getHelperText() : null;
    }

}
