/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.popover;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for the {@link Popover} component.
 */
public enum PopoverVariant implements ThemeVariant {
    ARROW("arrow"),
    LUMO_NO_PADDING("no-padding"),
    /**
     * @deprecated Since 24.7, the Material theme is deprecated and will be
     *             removed in Vaadin 25.
     */
    @Deprecated
    MATERIAL_NO_PADDING("no-padding");

    private final String variant;

    PopoverVariant(String variant) {
        this.variant = variant;
    }

    /**
     * Gets the variant name.
     *
     * @return variant name
     */
    public String getVariantName() {
        return variant;
    }
}
