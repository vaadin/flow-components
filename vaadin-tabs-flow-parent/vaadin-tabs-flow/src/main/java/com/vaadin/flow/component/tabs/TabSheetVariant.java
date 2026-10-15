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
 * Set of theme variants applicable for {@code vaadin-tabsheet} component.
 */
public enum TabSheetVariant implements ThemeVariant {
    /**
     * @deprecated Use {@code TabVariant.LUMO_ICON_ON_TOP} on individual
     *             {@code Tab} instances instead.
     */
    @Deprecated
    LUMO_TABS_ICON_ON_TOP("icon-on-top"),
    LUMO_TABS_CENTERED("centered"),
    LUMO_TABS_SMALL("small"),
    LUMO_TABS_MINIMAL("minimal"),
    LUMO_TABS_HIDE_SCROLL_BUTTONS("hide-scroll-buttons"),
    LUMO_TABS_EQUAL_WIDTH_TABS("equal-width-tabs"),
    LUMO_BORDERED("bordered"),
    LUMO_NO_PADDING("no-padding"),
    /**
     * @deprecated Since 24.7, the Material theme is deprecated and will be
     *             removed in Vaadin 25.
     */
    @Deprecated
    MATERIAL_TABS_FIXED("fixed"),
    /**
     * @deprecated Since 24.7, the Material theme is deprecated and will be
     *             removed in Vaadin 25.
     */
    @Deprecated
    MATERIAL_BORDERED("bordered");

    private final String variant;

    TabSheetVariant(String variant) {
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
