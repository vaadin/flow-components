/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
