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
 * the {@code get_form_state} JSON output. The write path — JSON-to-typed
 * conversion plus {@code Current state:} rendering — backs the
 * {@code fill_form} tool. Reflective lookups are used for the data-view /
 * label-generator accessors so this module does not need a compile-time
 * dependency on every individual selection-component module.
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
     * {@link #getMessage() message} is surfaced verbatim in the
     * {@code Rejected:} block of the {@code fill_form} tool result so the LLM
     * sees the same reason that's logged; the field stays on its prior value.
     * Reason text is written here in {@link FormValueConverter} and is curated
     * for LLM consumption (no PII, no internal state).
     */
    static final class RejectedValueException extends RuntimeException {
        RejectedValueException(String message) {
            super(message);
        }
    }

    /**
     * Converts a JSON node into a typed value suitable for
     * {@link HasValue#setValue}. Returns the field's empty value when the JSON
     * node is {@code null} or a JSON {@code null}. Throws
     * {@link RejectedValueException} when the JSON shape doesn't match the
     * field's type.
     */
    static Object convert(FormFieldDescriptor field, JsonNode value) {
        if (value == null || value.isNull()) {
            return field.field().getEmptyValue();
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
        default -> throw new RejectedValueException(
                "Unsupported field type: " + field.type());
        };
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

    /**
     * Renders the field's current value for the {@code fill_form} tool's
     * {@code Written:} write-summary block. Returns {@code <empty>} for null /
     * empty inputs, comma-separates collections, and falls through to
     * {@link String#valueOf} for the rest.
     */
    static String displayValue(FormFieldDescriptor field) {
        var value = field.field().getValue();
        if (isEmpty(value)) {
            return "<empty>";
        }
        if (value instanceof Collection<?> coll) {
            var b = new StringBuilder();
            var first = true;
            for (var v : coll) {
                if (!first) {
                    b.append(", ");
                }
                b.append(renderItem(field.field(), v));
                first = false;
            }
            return b.toString();
        }
        return renderItem(field.field(), value);
    }
}
