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

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads a bean property's leaf type via reflection so the
 * {@code get_form_state} schema can describe a bound field by the bean side it
 * actually feeds. Dotted property paths ({@code "address.street"}) walk each
 * segment in turn; the final {@link #propertyType} reflects the leaf
 * component's return type.
 */
final class BeanPropertyMetadata {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(BeanPropertyMetadata.class);

    final Class<?> propertyType;

    /** Enum constant names when {@link #propertyType} is an {@code enum}. */
    final List<String> enumConstants;

    private BeanPropertyMetadata(Class<?> propertyType,
            List<String> enumConstants) {
        this.propertyType = propertyType;
        this.enumConstants = enumConstants;
    }

    /**
     * Resolves metadata for {@code propertyPath} on {@code beanClass}.
     *
     * @param beanClass
     *            the bean class, or {@code null}
     * @param propertyPath
     *            the dotted property path, or {@code null}
     * @return the metadata, or {@code null} when the path cannot be resolved
     */
    static BeanPropertyMetadata resolve(Class<?> beanClass,
            String propertyPath) {
        if (beanClass == null || propertyPath == null
                || propertyPath.isEmpty()) {
            return null;
        }
        try {
            var current = beanClass;
            for (var part : propertyPath.split("\\.")) {
                var getter = findGetter(current, part);
                if (getter == null) {
                    return null;
                }
                current = getter.getReturnType();
            }
            var enumConstants = current.isEnum()
                    ? Arrays.stream(current.getEnumConstants())
                            .map(Object::toString).toList()
                    : List.<String> of();
            return new BeanPropertyMetadata(current, enumConstants);
        } catch (Exception ex) {
            LOGGER.warn("Failed to resolve bean property type for {}#{}",
                    beanClass.getName(), propertyPath, ex);
            return null;
        }
    }

    private static Method findGetter(Class<?> type, String propertyName) {
        try {
            for (var pd : Introspector.getBeanInfo(type, Object.class)
                    .getPropertyDescriptors()) {
                if (pd.getName().equals(propertyName)) {
                    return pd.getReadMethod();
                }
            }
        } catch (Exception ex) {
            LOGGER.debug("Introspection failed for {}", type.getName(), ex);
        }
        return null;
    }
}
