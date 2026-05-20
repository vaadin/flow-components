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
 * {@link FormAIController} needs two pieces of information that {@link Binder}
 * does not expose publicly: the property name supplied when a binding was
 * created, and the binding instance for a given {@link HasValue}. Both live in
 * private fields ({@code boundProperties} and {@code bindings}); this class
 * reads them reflectively and caches the {@link Field} references at static
 * init. If a future {@code Binder} renames or removes either field, the helper
 * logs a warning and returns empty results so callers degrade to the non-binder
 * code path rather than throwing.
 * </p>
 */
final class BinderReflection {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(BinderReflection.class);

    private static final Field BOUND_PROPERTIES_FIELD = getBoundPropertiesField();
    private static final Field BINDINGS_FIELD = getBindingsField();

    private BinderReflection() {
    }

    /**
     * Builds a {@code HasValue → propertyName} map of every binding that
     * carries a property name. Lambda-bound bindings are excluded because the
     * binder records no name for them.
     *
     * @param binder
     *            the binder, not {@code null}
     * @return the map, never {@code null}; empty if reflection is unavailable
     */
    @SuppressWarnings("unchecked")
    static Map<HasValue<?, ?>, String> collectPropertyNames(Binder<?> binder) {
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
     * Returns the {@link Binding} registered for the given field, or
     * {@code null} if no binding matches. Matches by reference equality so the
     * same {@code HasValue} added to two binders does not cross-pollute.
     *
     * @param binder
     *            the binder, not {@code null}
     * @param field
     *            the field, not {@code null}
     * @return the matching binding, or {@code null}
     */
    @SuppressWarnings("unchecked")
    static Binding<?, ?> findBinding(Binder<?> binder, HasValue<?, ?> field) {
        try {
            return ((Collection<Binding<?, ?>>) BINDINGS_FIELD.get(binder))
                    .stream().filter(binding -> binding.getField() == field)
                    .findFirst().orElse(null);
        } catch (Exception ex) {
            LOGGER.warn("Could not read bindings from Binder.", ex);
        }
        return null;
    }

    private static Field getBoundPropertiesField() {
        try {
            var field = Binder.class.getDeclaredField("boundProperties");
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            LOGGER.warn("Could not access Binder.boundProperties; bound "
                    + "fields will not contribute a property-name alias to "
                    + "the LLM's view of the form.", e);
        }
        return null;
    }

    private static Field getBindingsField() {
        try {
            var field = Binder.class.getDeclaredField("bindings");
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            LOGGER.warn("Could not access Binder.bindings; "
                    + "FormAIController.findBinding(...) will always return "
                    + "empty.", e);
        }
        return null;
    }
}
