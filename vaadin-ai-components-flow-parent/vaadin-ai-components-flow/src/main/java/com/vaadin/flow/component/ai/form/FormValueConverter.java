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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxBase;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;

import tools.jackson.databind.JsonNode;

/**
 * Converts JSON payloads from the LLM into typed Java values and renders typed
 * field values back into LLM-facing strings.
 * <p>
 * For typed selectors ({@code ComboBox<T>}, {@code Select<T>}, ...) the
 * conversion tries the registered {@code resolveItemFromString}, the per-turn
 * {@code query_field_options} cache, the registered {@code allowedValues}, and
 * the field's {@link ListDataProvider} items in that order.
 */
final class FormValueConverter {

    private FormValueConverter() {
    }

    /**
     * Signals that an LLM-supplied value could not be applied to a field. The
     * message is plain text and safe to surface back to the LLM.
     */
    static final class RejectedValueException extends RuntimeException {
        RejectedValueException(String message) {
            super(message);
        }
    }

    /**
     * Converts a JSON node from the {@code fill_form} payload into a typed
     * value suitable for {@code HasValue.setValue(...)}.
     */
    static Object convert(FormFieldEntry e, JsonNode value,
            Map<String, List<Object>> queryCache) {
        if (value == null || value.isNull()) {
            return e.field.getEmptyValue();
        }
        switch (e.type) {
        case STRING, EMAIL -> {
            return value.isString() ? value.asString() : value.toString();
        }
        case BIG_DECIMAL -> {
            String text = value.isString() ? value.asString() : value.toString();
            try {
                return new BigDecimal(text);
            } catch (NumberFormatException ex) {
                throw new RejectedValueException(
                        "Not a parseable decimal: " + text);
            }
        }
        case NUMBER -> {
            if (!value.isNumber()) {
                throw new RejectedValueException(
                        "Expected number, got " + value);
            }
            return value.asDouble();
        }
        case INTEGER -> {
            if (!value.isIntegralNumber()) {
                throw new RejectedValueException(
                        "Expected integer, got " + value);
            }
            return value.asInt();
        }
        case BOOLEAN -> {
            if (!value.isBoolean()) {
                throw new RejectedValueException(
                        "Expected boolean, got " + value);
            }
            return value.asBoolean();
        }
        case DATE -> {
            try {
                return LocalDate.parse(value.asString());
            } catch (Exception ex) {
                throw new RejectedValueException(
                        "Not a valid ISO date: " + value);
            }
        }
        case DATE_TIME -> {
            try {
                String text = value.asString();
                if (text.endsWith("Z")) {
                    text = text.substring(0, text.length() - 1);
                }
                return LocalDateTime.parse(text);
            } catch (Exception ex) {
                throw new RejectedValueException(
                        "Not a valid ISO date-time: " + value);
            }
        }
        case TIME -> {
            try {
                return LocalTime.parse(value.asString());
            } catch (Exception ex) {
                throw new RejectedValueException(
                        "Not a valid ISO time: " + value);
            }
        }
        case SINGLE_SELECT -> {
            String text = value.isString() ? value.asString()
                    : value.toString();
            return resolveItem(e, text, queryCache);
        }
        case MULTI_SELECT -> {
            if (!value.isArray()) {
                throw new RejectedValueException(
                        "Expected array, got " + value);
            }
            List<Object> resolved = new ArrayList<>();
            List<String> failures = new ArrayList<>();
            for (JsonNode item : value) {
                String text = item.isString() ? item.asString()
                        : item.toString();
                try {
                    resolved.add(resolveItem(e, text, queryCache));
                } catch (RejectedValueException rex) {
                    failures.add(text + " (" + rex.getMessage() + ")");
                }
            }
            if (!failures.isEmpty()) {
                throw new RejectedValueException(
                        "Could not resolve: " + String.join("; ", failures));
            }
            return new LinkedHashSet<>(resolved);
        }
        default -> throw new RejectedValueException(
                "Unsupported field type: " + e.type);
        }
    }

    static Object resolveItem(FormFieldEntry e, String text,
            Map<String, List<Object>> queryCache) {
        if (e.itemResolver != null) {
            Object resolved = e.itemResolver.apply(text);
            if (resolved == null) {
                throw new RejectedValueException(
                        "Resolver returned no match for: " + text);
            }
            return resolved;
        }
        List<Object> cached = queryCache.get(e.identifier);
        if (cached != null) {
            for (Object item : cached) {
                if (renderItem(e, item).equals(text)) {
                    return item;
                }
            }
        }
        if (e.allowedValues != null) {
            for (Object item : e.allowedValues) {
                if (renderItem(e, item).equals(text)) {
                    return item;
                }
            }
            String labels = e.allowedValues.stream().map(v -> renderItem(e, v))
                    .reduce((a, b) -> a + ", " + b).orElse("(none)");
            throw new RejectedValueException("Allowed: " + labels
                    + " (received: \"" + text + "\")");
        }
        List<Object> items = listDataProviderItems(e.field);
        if (items != null) {
            List<Object> matches = new ArrayList<>();
            for (Object item : items) {
                if (renderItem(e, item).equals(text)) {
                    matches.add(item);
                }
            }
            if (matches.size() == 1) {
                return matches.get(0);
            }
            if (matches.size() > 1) {
                throw new RejectedValueException("Ambiguous label: \"" + text
                        + "\" matches multiple items. Register a "
                        + "resolveItemFromString to disambiguate.");
            }
            String labels = items.stream().map(v -> renderItem(e, v))
                    .reduce((a, b) -> a + ", " + b).orElse("(none)");
            throw new RejectedValueException("Allowed: " + labels
                    + " (received: \"" + text + "\")");
        }
        throw new RejectedValueException(
                "Cannot map \"" + text + "\" to a typed item without a "
                        + "resolveItemFromString registration.");
    }

    /**
     * Renders a typed item via the field's {@link ItemLabelGenerator}, falling
     * back to {@link Object#toString()}.
     */
    static String renderItem(FormFieldEntry e, Object item) {
        if (item == null) {
            return "";
        }
        ItemLabelGenerator<Object> gen = itemLabelGenerator(e.field);
        try {
            return gen != null ? gen.apply(item) : String.valueOf(item);
        } catch (Exception ex) {
            return String.valueOf(item);
        }
    }

    /**
     * Renders the current value of a field for display in the {@code Current
     * state:} block and the {@code (current: ...)} tool-description suffix.
     */
    static String displayValue(FormFieldEntry e) {
        Object value = e.field.getValue();
        if (isEmpty(value)) {
            return "<empty>";
        }
        if (value instanceof Collection<?> coll) {
            List<String> rendered = new ArrayList<>();
            for (Object v : coll) {
                rendered.add(renderItem(e, v));
            }
            return String.join(", ", rendered);
        }
        return renderItem(e, value);
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
     * Returns the items of the field's data provider when it is in-memory, or
     * {@code null} for backend-loaded providers.
     */
    @SuppressWarnings("unchecked")
    static List<Object> listDataProviderItems(HasValue<?, ?> field) {
        DataProvider<?, ?> provider = dataProvider(field);
        if (provider instanceof ListDataProvider<?> ldp) {
            return new ArrayList<>((Collection<Object>) ldp.getItems());
        }
        return null;
    }

    private static DataProvider<?, ?> dataProvider(HasValue<?, ?> field) {
        if (field instanceof ComboBoxBase<?, ?, ?> cb) {
            return cb.getDataProvider();
        }
        if (field instanceof Select<?> s) {
            return s.getDataProvider();
        }
        if (field instanceof RadioButtonGroup<?> r) {
            return r.getDataProvider();
        }
        if (field instanceof CheckboxGroup<?> c) {
            return c.getDataProvider();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> ItemLabelGenerator<T> itemLabelGenerator(
            HasValue<?, ?> field) {
        if (field instanceof ComboBox<?> cb) {
            return (ItemLabelGenerator<T>) cb.getItemLabelGenerator();
        }
        if (field instanceof MultiSelectComboBox<?> ms) {
            return (ItemLabelGenerator<T>) ms.getItemLabelGenerator();
        }
        if (field instanceof Select<?> s) {
            return (ItemLabelGenerator<T>) s.getItemLabelGenerator();
        }
        if (field instanceof RadioButtonGroup<?> r) {
            return (ItemLabelGenerator<T>) r.getItemLabelGenerator();
        }
        if (field instanceof CheckboxGroup<?> c) {
            return (ItemLabelGenerator<T>) c.getItemLabelGenerator();
        }
        return null;
    }
}
