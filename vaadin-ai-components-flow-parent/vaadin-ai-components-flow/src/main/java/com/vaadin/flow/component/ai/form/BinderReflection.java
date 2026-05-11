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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.Binding;

/**
 * Reflection access to {@link Binder} internals.
 * <p>
 * {@code Binder} does not expose its full bindings list through a public API.
 * We walk the private {@code bindings} collection and the
 * {@code boundProperties} map to discover both the bindings keyed by
 * {@link HasValue} and their associated property names. Both fields are
 * package-stable parts of the {@code Binder} implementation.
 */
final class BinderReflection {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(BinderReflection.class);

    private static final Field BINDINGS_FIELD;
    private static final Field BOUND_PROPERTIES_FIELD;

    static {
        Field bindings = null;
        Field properties = null;
        try {
            bindings = Binder.class.getDeclaredField("bindings");
            bindings.setAccessible(true);
            properties = Binder.class.getDeclaredField("boundProperties");
            properties.setAccessible(true);
        } catch (NoSuchFieldException e) {
            LOGGER.warn("Could not access Binder internals via reflection; "
                    + "Binder integration will fall back to label derivation "
                    + "and skip programmatic validation.", e);
        }
        BINDINGS_FIELD = bindings;
        BOUND_PROPERTIES_FIELD = properties;
    }

    private BinderReflection() {
    }

    /**
     * Returns a HasValue → property-name map for every binding created with a
     * known property name (e.g. {@code bind(field, "name")}). Lambda-bound
     * bindings are not included.
     *
     * @param binder
     *            the binder, not {@code null}
     * @return the map, never {@code null}
     */
    static Map<HasValue<?, ?>, String> collectPropertyNames(Binder<?> binder) {
        if (BOUND_PROPERTIES_FIELD == null) {
            return Collections.emptyMap();
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Binding<?, ?>> bound = (Map<String, Binding<?, ?>>) BOUND_PROPERTIES_FIELD
                    .get(binder);
            Map<HasValue<?, ?>, String> out = new LinkedHashMap<>();
            bound.forEach((name, binding) -> out.put(binding.getField(), name));
            return out;
        } catch (IllegalAccessException ex) {
            LOGGER.warn("Could not read Binder.boundProperties", ex);
            return Collections.emptyMap();
        }
    }

    /**
     * Returns the binding registered for the given field, or {@code null} if
     * the field is not bound.
     *
     * @param binder
     *            the binder, not {@code null}
     * @param field
     *            the field, not {@code null}
     * @return the matching binding, or {@code null}
     */
    static Binding<?, ?> findBinding(Binder<?> binder, HasValue<?, ?> field) {
        if (BINDINGS_FIELD == null) {
            return null;
        }
        try {
            @SuppressWarnings("unchecked")
            Collection<Binding<?, ?>> all = (Collection<Binding<?, ?>>) BINDINGS_FIELD
                    .get(binder);
            for (Binding<?, ?> binding : all) {
                if (binding.getField() == field) {
                    return binding;
                }
            }
        } catch (IllegalAccessException ex) {
            LOGGER.warn("Could not read Binder.bindings", ex);
        }
        return null;
    }
}
