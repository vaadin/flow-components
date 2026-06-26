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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.ai.form.FormAITools.FormFieldDescriptor;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;

import tools.jackson.databind.JsonNode;

/**
 * Helpers for value rendering and conversion shared by the form tools.
 * <p>
 * The read path — emptiness check, single-item rendering via
 * {@link ItemLabelGenerator}, and {@link ListDataProvider} extraction — backs
 * the {@code get_form_state} JSON output. The write path —
 * {@link #convert(FormFieldDescriptor, JsonNode) JSON-to-typed conversion} —
 * backs the {@code fill_form} tool. Reflective lookups are used for the
 * data-view / label-generator accessors so this module does not need a
 * compile-time dependency on every individual selection-component module.
 */
final class FormValueConverter {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(FormValueConverter.class);

    /**
     * Caches the {@link Method} resolved for a given (concrete class, method
     * name) pair. {@link Optional#empty()} marks lookups that fell off the
     * superclass chain so the negative result is not re-computed either.
     */
    private static final Map<MethodKey, Optional<Method>> METHOD_CACHE = new ConcurrentHashMap<>();

    private record MethodKey(Class<?> type, String name) {
    }

    private FormValueConverter() {
    }

    static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String s) {
            return s.isEmpty();
        }
        if (value instanceof Collection<?> c) {
            return c.isEmpty();
        }
        return false;
    }

    /**
     * Renders a typed item via the field's {@link ItemLabelGenerator}, falling
     * back to {@link Object#toString()} when no generator is registered or it
     * throws.
     */
    static String renderItem(HasValue<?, ?> field, Object item) {
        if (item == null) {
            return "";
        }
        var gen = itemLabelGenerator(field);
        if (gen == null) {
            return String.valueOf(item);
        }
        try {
            var label = gen.apply(item);
            return label != null ? label : String.valueOf(item);
        } catch (Exception ex) {
            LOGGER.warn("Item label generator threw for {}", item.getClass(),
                    ex);
            return String.valueOf(item);
        }
    }

    /**
     * Returns the items of the field's data provider when it is in-memory, or
     * an empty list for backend-loaded providers or fields that do not have a
     * data provider at all.
     */
    static List<Object> listDataProviderItems(HasValue<?, ?> field) {
        var provider = dataProvider(field);
        if (provider instanceof ListDataProvider<?> ldp) {
            return new ArrayList<>(ldp.getItems());
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private static ItemLabelGenerator<Object> itemLabelGenerator(
            HasValue<?, ?> field) {
        var result = invokeNoArg(field, "getItemLabelGenerator");
        return result instanceof ItemLabelGenerator<?> gen
                ? (ItemLabelGenerator<Object>) gen
                : null;
    }

    private static DataProvider<?, ?> dataProvider(HasValue<?, ?> field) {
        var result = invokeNoArg(field, "getDataProvider");
        return result instanceof DataProvider<?, ?> dp ? dp : null;
    }

    private static Object invokeNoArg(Object target, String methodName) {
        var method = METHOD_CACHE.computeIfAbsent(
                new MethodKey(target.getClass(), methodName),
                FormValueConverter::lookupMethod);
        if (method.isEmpty()) {
            return null;
        }
        try {
            return method.get().invoke(target);
        } catch (ReflectiveOperationException ex) {
            LOGGER.debug("Reflective call to {}#{} failed",
                    target.getClass().getName(), methodName, ex);
            return null;
        }
    }

    private static Optional<Method> lookupMethod(MethodKey key) {
        for (var c = key.type(); c != null; c = c.getSuperclass()) {
            try {
                var method = c.getDeclaredMethod(key.name());
                return Optional.of(method);
            } catch (NoSuchMethodException ignored) {
                // try the next superclass
            }
        }
        return Optional.empty();
    }

    /**
     * Signals that an LLM-supplied value could not be applied to a field. The
     * {@link #getMessage() message} is surfaced verbatim as the {@code reason}
     * of the matching entry in the {@code fill_form} tool's {@code rejected}
     * array, so the LLM sees the same reason that's logged; the field stays on
     * its prior value. Reason text is written here in
     * {@link FormValueConverter} and is curated for LLM consumption (no PII, no
     * internal state).
     */
    static final class RejectedValueException extends RuntimeException {
        RejectedValueException(String message) {
            super(message);
        }
    }

    /**
     * Converts a JSON node into a typed value suitable for
     * {@link HasValue#setValue}. Returns the field's empty value for
     * {@code null} or JSON {@code null}; throws {@link RejectedValueException}
     * when the JSON shape doesn't match the field's type, or when a registered
     * {@code fieldValueOptions} cannot resolve a label.
     * <p>
     * For selection fields, resolution tries the registration's observed items
     * first (matching the LLM-supplied label against
     * {@link FormFieldHints#itemLabelGenerator} per item), then falls back to
     * the field's own in-memory items (an eager {@code setItems(...)}). A
     * selection field with no item source is rejected with a curated reason
     * naming the missing registration. Multi-select fields expect a JSON array
     * of labels; single-select and other label-routed fields expect a single
     * string.
     */
    static Object convert(FormFieldDescriptor field, JsonNode value) {
        if (value == null || value.isNull()) {
            return field.field().getEmptyValue();
        }
        // Multi-select takes an array of labels; handled before the
        // fieldValueOptions check so the array shape is enforced even on
        // fields whose value type is itself a String set.
        if (field.type() == FormFieldType.MULTI_SELECT) {
            return convertMultiSelect(field, value);
        }
        // Once fieldValueOptions is registered the LLM picks a label,
        // regardless of the field's underlying value type — type-driven
        // parsing would hand setValue a raw String and the field would reject
        // it.
        var hints = field.hints();
        if (hints != null && hints.valueOptionsItems != null) {
            return convertSingleLabelFromObservedItems(value, hints);
        }
        return switch (field.type()) {
        case STRING, EMAIL -> convertString(value);
        case BIG_DECIMAL -> convertBigDecimal(value);
        case NUMBER -> convertNumber(value);
        case INTEGER -> convertInteger(value);
        case BOOLEAN -> convertBoolean(value);
        case DATE -> convertDate(value);
        case DATE_TIME -> convertDateTime(value);
        case TIME -> convertTime(value);
        case SINGLE_SELECT -> convertSingleSelectFromItems(field, value);
        default -> throw new RejectedValueException(
                "Unsupported field type: " + field.type());
        };
    }

    /**
     * Fallback resolver for a SINGLE_SELECT field that has no
     * {@code fieldValueOptions(...)} registered: matches the LLM-supplied label
     * against the field's in-memory items via {@link #renderItem}. This is the
     * inverse of the schema path that emits the same items as an {@code enum}
     * array — both sides agree on the label set, so the LLM can write through
     * an eager {@code setItems(...)} field without the developer wiring up
     * {@code fieldValueOptions(...)} too.
     */
    private static Object convertSingleSelectFromItems(
            FormFieldDescriptor field, JsonNode value) {
        if (!value.isString()) {
            throw new RejectedValueException(
                    "Expected string label, got " + value);
        }
        var items = listDataProviderItems(field.field());
        if (items.isEmpty()) {
            // No items and no fieldValueOptions — the field has no option
            // source at all. Point the developer at the missing wiring rather
            // than the missing match, since the model couldn't have picked any
            // label.
            throw new RejectedValueException(
                    "Selection field has no value options registered — register "
                            + "options via "
                            + "FormAIController.fieldValueOptions(...) or call "
                            + "setItems(...) so the AI knows what to pick.");
        }
        return resolveLabelAgainstItems(field.field(), value.asString(), items);
    }

    /**
     * Resolves one LLM-supplied label against the items the registration has
     * seen. Walks {@link FormFieldHints#valueOptionsItems} and returns the
     * first item whose label (via {@link FormFieldHints#itemLabelGenerator})
     * matches. An empty observed-items list is the query-mode "registered but
     * never queried" case and produces a rejection that nudges the LLM to call
     * {@code query_field_options} first.
     */
    private static Object convertSingleLabelFromObservedItems(JsonNode value,
            FormFieldHints hints) {
        if (!value.isString()) {
            throw new RejectedValueException(
                    "Expected string label, got " + value);
        }
        return resolveLabelAgainstObservedItems(value.asString(), hints);
    }

    private static Object convertMultiSelect(FormFieldDescriptor field,
            JsonNode value) {
        if (!value.isArray()) {
            throw new RejectedValueException(
                    "Expected array of string labels, got " + value);
        }
        // Empty array is the LLM clearing the field; defer to the field's
        // own emptyValue() so the type matches what setValue expects (Vaadin
        // multi-selects return Set.of()).
        if (value.isEmpty()) {
            return field.field().getEmptyValue();
        }
        var hints = field.hints();
        if (hints != null && hints.valueOptionsItems != null) {
            var result = new LinkedHashSet<>();
            for (var node : value) {
                if (!node.isString()) {
                    throw new RejectedValueException(
                            "Expected string label, got " + node);
                }
                result.add(resolveLabelAgainstObservedItems(node.asString(),
                        hints));
            }
            return result;
        }
        return convertMultiSelectFromItems(field, value);
    }

    /**
     * Returns the item that was indexed under the LLM-supplied label in
     * {@link FormFieldHints#valueOptionsItems}. An empty map is the query-mode
     * "not queried yet this turn" case and is called out with a hint at
     * {@code query_field_options}, since that's the only way the LLM could have
     * reached this point without seeing options.
     */
    private static Object resolveLabelAgainstObservedItems(String label,
            FormFieldHints hints) {
        var item = hints.valueOptionsItems.get(label);
        if (item != null) {
            return item;
        }
        if (hints.valueOptionsItems.isEmpty()) {
            throw new RejectedValueException("No matching option for label: "
                    + label
                    + " (call query_field_options first to load the field's options)");
        }
        throw new RejectedValueException(
                "No matching option for label: " + label);
    }

    /**
     * Fallback resolver for a MULTI_SELECT field that has no
     * {@code fieldValueOptions(...)} registered: matches each LLM-supplied
     * label against the field's in-memory items via {@link #renderItem}.
     * Mirrors {@link #convertSingleSelectFromItems} so eager-items
     * {@code MultiSelectComboBox<T>} and {@code CheckboxGroup<T>} are writable
     * without the developer wiring up {@code fieldValueOptions(...)} too.
     */
    private static Object convertMultiSelectFromItems(FormFieldDescriptor field,
            JsonNode value) {
        var items = listDataProviderItems(field.field());
        if (items.isEmpty()) {
            throw new RejectedValueException(
                    "Multi-select field has no value options registered — "
                            + "register options via "
                            + "FormAIController.fieldValueOptions(...) or call "
                            + "setItems(...) so the AI knows what to pick.");
        }
        var result = new LinkedHashSet<>();
        for (var node : value) {
            if (!node.isString()) {
                throw new RejectedValueException(
                        "Expected string label, got " + node);
            }
            result.add(resolveLabelAgainstItems(field.field(), node.asString(),
                    items));
        }
        return result;
    }

    /**
     * Walks the field's in-memory items list and returns the first item whose
     * {@link #renderItem} matches the LLM-supplied label. The matching mirrors
     * what the schema sent the LLM under {@code enum}, so both halves of the
     * tool protocol agree on the label set.
     */
    private static Object resolveLabelAgainstItems(HasValue<?, ?> field,
            String label, List<Object> items) {
        for (var item : items) {
            if (label.equals(renderItem(field, item))) {
                return item;
            }
        }
        throw new RejectedValueException(
                "No matching option for label: " + label);
    }

    private static String convertString(JsonNode value) {
        if (!value.isString()) {
            throw new RejectedValueException("Expected string, got " + value);
        }
        return value.asString();
    }

    private static BigDecimal convertBigDecimal(JsonNode value) {
        // Schema declares BIG_DECIMAL as string + pattern, but accept a JSON
        // number too; LLMs sometimes emit bare numbers for amounts. Jackson
        // round-trips through toString() losslessly here.
        var text = value.isString() ? value.asString() : value.toString();
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException ex) {
            throw new RejectedValueException(
                    "Not a parseable decimal: " + text);
        }
    }

    private static Double convertNumber(JsonNode value) {
        if (!value.isNumber()) {
            throw new RejectedValueException("Expected number, got " + value);
        }
        return value.asDouble();
    }

    private static Integer convertInteger(JsonNode value) {
        // Accept JSON integers and whole-number floating-point values (e.g.
        // 3.0) — LLMs sometimes emit the latter for integer fields.
        // Fractional values are rejected.
        if (value.isIntegralNumber()) {
            return value.asInt();
        }
        if (value.isFloatingPointNumber()) {
            var d = value.asDouble();
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return (int) d;
            }
        }
        throw new RejectedValueException("Expected integer, got " + value);
    }

    private static Boolean convertBoolean(JsonNode value) {
        if (!value.isBoolean()) {
            throw new RejectedValueException("Expected boolean, got " + value);
        }
        return value.asBoolean();
    }

    private static LocalDate convertDate(JsonNode value) {
        try {
            return LocalDate.parse(value.asString());
        } catch (Exception ex) {
            throw new RejectedValueException("Not a valid ISO date: " + value);
        }
    }

    private static LocalDateTime convertDateTime(JsonNode value) {
        // DateTimePicker is zone-naive; accept either a naive ISO local
        // date-time or any ISO date-time with offset / 'Z' (the zone is
        // dropped, since the picker can't represent it).
        var text = value.asString();
        try {
            return OffsetDateTime.parse(text).toLocalDateTime();
        } catch (Exception ignoredOffset) {
            try {
                return LocalDateTime.parse(text);
            } catch (Exception ex) {
                throw new RejectedValueException(
                        "Not a valid ISO date-time: " + value);
            }
        }
    }

    private static LocalTime convertTime(JsonNode value) {
        try {
            return LocalTime.parse(value.asString());
        } catch (Exception ex) {
            throw new RejectedValueException("Not a valid ISO time: " + value);
        }
    }

}
