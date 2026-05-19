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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;

/**
 * Helpers for the state-tool subset of value/label rendering: emptiness check,
 * single-item rendering via the field's {@link ItemLabelGenerator}, and
 * extracting items from a field's {@link ListDataProvider} via reflection (kept
 * reflective so this module does not need to depend on every individual
 * selection-component module).
 * <p>
 * The full string-to-typed-item conversion lives elsewhere — this class only
 * carries the read path needed by {@code get_form_state}.
 */
final class FormValueConverter {

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
            return gen.apply(item);
        } catch (Exception ex) {
            return String.valueOf(item);
        }
    }

    /**
     * Returns the items of the field's data provider when it is in-memory, or
     * {@code null} for backend-loaded providers or fields that do not have a
     * data provider at all.
     */
    @SuppressWarnings("unchecked")
    static List<Object> listDataProviderItems(HasValue<?, ?> field) {
        var provider = dataProvider(field);
        if (provider instanceof ListDataProvider<?> ldp) {
            return new ArrayList<>((Collection<Object>) ldp.getItems());
        }
        return null;
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
            return null;
        }
    }

    private static Optional<Method> lookupMethod(MethodKey key) {
        for (var c = key.type(); c != null
                && c != Object.class; c = c.getSuperclass()) {
            try {
                var method = c.getDeclaredMethod(key.name());
                method.setAccessible(true);
                return Optional.of(method);
            } catch (NoSuchMethodException ignored) {
                // try the next superclass
            }
        }
        return Optional.empty();
    }
}
