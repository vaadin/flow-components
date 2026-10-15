/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.tabs;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for {@code vaadin-tabs} component.
 */
public enum TabsVariant implements ThemeVariant {
    /**
     * @deprecated Use {@code TabVariant.LUMO_ICON_ON_TOP} on individual
     *             {@code Tab} instances instead.
     */
    @Deprecated
    LUMO_ICON_ON_TOP("icon-on-top"),
    LUMO_CENTERED("centered"),
    LUMO_SMALL("small"),
    LUMO_MINIMAL("minimal"),
    LUMO_HIDE_SCROLL_BUTTONS("hide-scroll-buttons"),
    LUMO_EQUAL_WIDTH_TABS("equal-width-tabs"),
    /**
     * @deprecated Since 24.7, the Material theme is deprecated and will be
     *             removed in Vaadin 25.
     */
    @Deprecated
    MATERIAL_FIXED("fixed");

    private final String variant;

    TabsVariant(String variant) {
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
