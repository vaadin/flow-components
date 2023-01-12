/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.shared;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.HasTheme;

/**
 * Mixin interface that allows adding and removing typed theme variants to /
 * from a component
 *
 * @param <TVariantEnum>
 *            The specific theme variant enum type
 */
public interface HasThemeVariant<TVariantEnum extends ThemeVariant>
        extends HasTheme {
    /**
     * Adds theme variants to the component.
     *
     * @param variants
     *            theme variants to add
     */
    @SuppressWarnings("unchecked")
    default void addThemeVariants(TVariantEnum... variants) {
        getThemeNames()
                .addAll(Stream.of(variants).map(TVariantEnum::getVariantName)
                        .collect(Collectors.toList()));
    }

    /**
     * Removes theme variants from the component.
     *
     * @param variants
     *            theme variants to remove
     */
    @SuppressWarnings("unchecked")
    default void removeThemeVariants(TVariantEnum... variants) {
        getThemeNames()
                .removeAll(Stream.of(variants).map(TVariantEnum::getVariantName)
                        .collect(Collectors.toList()));
    }
}
