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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.binder.HasItems;
import com.vaadin.flow.data.provider.HasDataView;
import com.vaadin.flow.data.provider.HasLazyDataView;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.flow.data.selection.MultiSelect;

/**
 * Classifies a {@link HasValue} field into the {@link FormAIController}'s
 * internal type taxonomy.
 * <p>
 * Detection is primarily contract-based: the value type is resolved by walking
 * the field's class hierarchy and reading the type argument of
 * {@code HasValue<?, V>}, and the selection variants are picked up via the
 * framework-level marker interfaces {@link MultiSelect},
 * {@link HasListDataView}, {@link HasLazyDataView}, {@link HasDataView}, and
 * {@link HasItems}. Two component-specific cases are recognised by class name
 * because their behaviour cannot be inferred from {@code HasValue}'s contract:
 * {@code PasswordField} is auto-ignored to keep secret values out of LLM tool
 * payloads, and {@code EmailField} is marked with {@code format=email} for the
 * schema. The lookups are class-hierarchy walks against fully-qualified names,
 * not {@code instanceof} on imported classes, so this module does not pull in
 * the text-field component module at compile time.
 * <p>
 * For any other custom {@code HasValue}, classification works without
 * registration: the value type is resolved through the hierarchy and mapped to
 * the closest primitive variant, or falls back to {@link #STRING}.
 */
enum FormFieldType {
    STRING,
    EMAIL,
    BIG_DECIMAL,
    NUMBER,
    INTEGER,
    BOOLEAN,
    DATE,
    DATE_TIME,
    TIME,
    SINGLE_SELECT,
    MULTI_SELECT,
    UNSUPPORTED;

    /**
     * Pattern accepted for {@link #BIG_DECIMAL} string payloads. Rejects locale
     * separators and scientific notation at the JSON-protocol layer.
     */
    static final String BIG_DECIMAL_PATTERN = "^-?\\d+(\\.\\d+)?$";

    private static final String PASSWORD_FIELD_FQN = "com.vaadin.flow.component.textfield.PasswordField";
    private static final String EMAIL_FIELD_FQN = "com.vaadin.flow.component.textfield.EmailField";

    /**
     * Memoizes the {@code Class -> FormFieldType} mapping so the generic
     * hierarchy walk in {@link #resolveValueType} and the marker-interface
     * checks run at most once per concrete field class for the lifetime of the
     * classloader.
     */
    private static final ClassValue<FormFieldType> CLASSIFY_CACHE = new ClassValue<>() {
        @Override
        protected FormFieldType computeValue(Class<?> type) {
            return doClassify(type);
        }
    };

    static FormFieldType classify(HasValue<?, ?> field) {
        if (field == null) {
            return UNSUPPORTED;
        }
        return CLASSIFY_CACHE.get(field.getClass());
    }

    private static FormFieldType doClassify(Class<?> fieldClass) {
        if (isAssignableTo(fieldClass, PASSWORD_FIELD_FQN)) {
            return UNSUPPORTED;
        }
        if (isAssignableTo(fieldClass, EMAIL_FIELD_FQN)) {
            return EMAIL;
        }
        if (MultiSelect.class.isAssignableFrom(fieldClass)) {
            return MULTI_SELECT;
        }
        if (HasListDataView.class.isAssignableFrom(fieldClass)
                || HasLazyDataView.class.isAssignableFrom(fieldClass)
                || HasDataView.class.isAssignableFrom(fieldClass)
                || HasItems.class.isAssignableFrom(fieldClass)) {
            return SINGLE_SELECT;
        }
        var valueType = resolveValueType(fieldClass);
        if (valueType == Boolean.class) {
            return BOOLEAN;
        }
        if (valueType == Integer.class || valueType == Long.class
                || valueType == Short.class || valueType == Byte.class
                || valueType == BigInteger.class) {
            return INTEGER;
        }
        if (valueType == Double.class || valueType == Float.class) {
            return NUMBER;
        }
        if (valueType == BigDecimal.class) {
            return BIG_DECIMAL;
        }
        if (valueType == LocalDate.class) {
            return DATE;
        }
        if (valueType == LocalDateTime.class) {
            return DATE_TIME;
        }
        if (valueType == LocalTime.class) {
            return TIME;
        }
        return STRING;
    }

    /**
     * Walks the class hierarchy of the given concrete field class to find the
     * {@code V} type argument of {@code HasValue<?, V>}, resolving type
     * variables through any intermediate generic superclasses and
     * super-interfaces. Returns {@code null} when {@code V} cannot be narrowed
     * to a concrete {@link Class} (for example {@code ComboBox<T>} where
     * {@code T} is left generic).
     */
    private static Class<?> resolveValueType(Class<?> startClass) {
        var bindings = new HashMap<TypeVariable<?>, Type>();
        Class<?> c = startClass;
        while (c != null && c != Object.class) {
            for (var iface : c.getGenericInterfaces()) {
                var found = findHasValueValueArg(iface, bindings);
                if (found != null) {
                    return found;
                }
            }
            var superType = c.getGenericSuperclass();
            if (superType instanceof ParameterizedType pt) {
                bindTypeArgs((Class<?>) pt.getRawType(),
                        pt.getActualTypeArguments(), bindings);
            }
            c = c.getSuperclass();
        }
        return null;
    }

    private static Class<?> findHasValueValueArg(Type iface,
            Map<TypeVariable<?>, Type> bindings) {
        if (iface instanceof ParameterizedType pt) {
            var raw = (Class<?>) pt.getRawType();
            if (raw == HasValue.class) {
                var resolved = resolveType(pt.getActualTypeArguments()[1],
                        bindings);
                return resolved instanceof Class<?> cls ? cls : null;
            }
            var deeper = new HashMap<>(bindings);
            bindTypeArgs(raw, pt.getActualTypeArguments(), deeper);
            for (var sub : raw.getGenericInterfaces()) {
                var found = findHasValueValueArg(sub, deeper);
                if (found != null) {
                    return found;
                }
            }
        } else if (iface instanceof Class<?> cls) {
            for (var sub : cls.getGenericInterfaces()) {
                var found = findHasValueValueArg(sub, bindings);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static void bindTypeArgs(Class<?> raw, Type[] args,
            Map<TypeVariable<?>, Type> bindings) {
        var vars = raw.getTypeParameters();
        for (var i = 0; i < vars.length && i < args.length; i++) {
            bindings.put(vars[i], resolveType(args[i], bindings));
        }
    }

    private static Type resolveType(Type t,
            Map<TypeVariable<?>, Type> bindings) {
        while (t instanceof TypeVariable<?> tv) {
            var bound = bindings.get(tv);
            if (bound == null || bound == tv) {
                return tv;
            }
            t = bound;
        }
        return t;
    }

    /**
     * Walks the class hierarchy looking for a superclass with the given
     * fully-qualified name. Used by the {@code PasswordField} / {@code
     * EmailField} special cases so this module does not need a compile-time
     * dependency on {@code vaadin-text-field-flow}.
     */
    private static boolean isAssignableTo(Class<?> type, String superTypeName) {
        for (var c = type; c != null
                && c != Object.class; c = c.getSuperclass()) {
            if (c.getName().equals(superTypeName)) {
                return true;
            }
        }
        return false;
    }
}
