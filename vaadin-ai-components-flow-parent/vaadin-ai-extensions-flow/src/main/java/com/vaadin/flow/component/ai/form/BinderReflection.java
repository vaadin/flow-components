/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.form;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.Binding;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationResult;

/**
 * Reflection access to {@link Binder} internals.
 * <p>
 * {@link FormAIController} needs two things {@link Binder} does not expose
 * publicly: the property name supplied when a binding was created (kept in a
 * private {@code boundProperties} field), and a way to read bean-level
 * validation without modifying the UI (the protected {@code validate(boolean)}
 * overload). This class reaches both reflectively and caches the {@link Field}
 * and {@link Method} references at static init. If a future {@code Binder}
 * renames or removes either member, the helper logs a warning and returns an
 * empty result so callers degrade gracefully rather than throwing.
 * </p>
 */
final class BinderReflection {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(BinderReflection.class);

    private static final Field BOUND_PROPERTIES_FIELD = getBinderField(
            "boundProperties");

    private static final Field BINDINGS_FIELD = getBinderField("bindings");

    private static final Method VALIDATE_METHOD = getBinderMethod("validate",
            boolean.class);

    private BinderReflection() {
    }

    /**
     * Runs the binder's validation once without modifying the UI and returns
     * its bean-level (cross-field) errors. Invokes the protected
     * {@code Binder.validate(false)} reflectively: {@code fireEvent=false}
     * means no status-change events fire and no field's invalid indicator is
     * touched, so a field the current turn did not write is never marked as a
     * side effect. Bean-level rules only run when a bean is set
     * ({@code setBean}) and every binding is individually valid; otherwise the
     * list is empty.
     *
     * @param binder
     *            the binder, or {@code null}
     * @return the bean-level validation errors, never {@code null}; empty when
     *         the binder is {@code null}, reflection is unavailable, validation
     *         throws, or there are none
     */
    @SuppressWarnings("java:S3011")
    static List<ValidationResult> beanValidationErrors(Binder<?> binder) {
        if (binder == null || VALIDATE_METHOD == null) {
            return List.of();
        }
        try {
            var status = (BinderValidationStatus<?>) VALIDATE_METHOD
                    .invoke(binder, Boolean.FALSE);
            return status.getBeanValidationErrors();
        } catch (Exception ex) {
            LOGGER.warn("Could not run bean-level validation on Binder.", ex);
        }
        return List.of();
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

    private static Method getBinderMethod(String name, Class<?>... params) {
        try {
            var method = Binder.class.getDeclaredMethod(name, params);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            LOGGER.warn("Could not access Binder.{}; bean-level validation "
                    + "will not be available.", name, e);
        }
        return null;
    }
}
