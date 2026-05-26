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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.Binding;

/**
 * Reflection access to {@link Binder} internals.
 * <p>
 * {@link FormAIController} needs the property name supplied when a binding was
 * created, which {@link Binder} does not expose publicly. The property names
 * live in a private {@code boundProperties} field; this class reads it
 * reflectively and caches the {@link Field} reference at static init. If a
 * future {@code Binder} renames or removes the field, the helper logs a warning
 * and returns an empty map so callers degrade to the non-binder code path
 * rather than throwing.
 * </p>
 */
final class BinderReflection {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(BinderReflection.class);

    private static final Field BOUND_PROPERTIES_FIELD = getBinderField(
            "boundProperties");

    private static final Field BINDINGS_FIELD = getBinderField("bindings");

    private BinderReflection() {
    }

    /**
     * Builds a {@code HasValue → propertyName} map of every binding that
     * carries a property name. Lambda-bound bindings are excluded because the
     * binder records no name for them. Returns an empty map when {@code binder}
     * is {@code null} or when the {@link Binder#getClass() Binder} version at
     * runtime no longer exposes the {@code boundProperties} field — callers can
     * call the method unconditionally and the no-binder controller path then
     * runs silently rather than logging a warning on every prompt.
     *
     * @param binder
     *            the binder, may be {@code null}
     * @return the map, never {@code null}; empty when binder is {@code null} or
     *         reflection is unavailable
     */
    @SuppressWarnings("unchecked")
    static Map<HasValue<?, ?>, String> collectPropertyNames(Binder<?> binder) {
        if (binder == null || BOUND_PROPERTIES_FIELD == null) {
            return Collections.emptyMap();
        }
        try {
            return ((Map<String, Binding<?, ?>>) BOUND_PROPERTIES_FIELD
                    .get(binder))
                    .entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getValue().getField(),
                            Map.Entry::getKey, (x, y) -> y,
                            LinkedHashMap::new));
        } catch (Exception ex) {
            LOGGER.warn("Could not extract property names from Binder.", ex);
        }
        return Collections.emptyMap();
    }

    /**
     * Returns the {@link Binding} bound to {@code field} in {@code binder}, or
     * {@code null} when the binder is {@code null}, the field is not bound, or
     * reflection is unavailable.
     *
     * @param binder
     *            the binder, or {@code null}
     * @param field
     *            the field to look up, not {@code null}
     * @return the matching binding, or {@code null}
     */
    @SuppressWarnings({ "unchecked", "java:S1452" })
    static Binding<?, ?> findBinding(Binder<?> binder, HasValue<?, ?> field) {
        if (binder == null || BINDINGS_FIELD == null) {
            return null;
        }
        try {
            var bindings = (Collection<? extends Binding<?, ?>>) BINDINGS_FIELD
                    .get(binder);
            for (var binding : bindings) {
                if (binding.getField() == field) {
                    return binding;
                }
            }
        } catch (Exception ex) {
            LOGGER.warn("Could not extract bindings from Binder.", ex);
        }
        return null;
    }

    private static Field getBinderField(String name) {
        try {
            var field = Binder.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            LOGGER.warn("Could not access Binder.{}; bound-field metadata "
                    + "will not be available.", name, e);
        }
        return null;
    }
}
