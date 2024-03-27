/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.tabs;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for {@code vaadin-tabsheet} component.
 */
public enum TabSheetVariant implements ThemeVariant {
    //@formatter:off
    LUMO_TABS_ICON_ON_TOP("icon-on-top"),
    LUMO_TABS_CENTERED("centered"),
    LUMO_TABS_SMALL("small"),
    LUMO_TABS_MINIMAL("minimal"),
    LUMO_TABS_HIDE_SCROLL_BUTTONS("hide-scroll-buttons"),
    LUMO_TABS_EQUAL_WIDTH_TABS( "equal-width-tabs"),
    LUMO_BORDERED("bordered"),
    MATERIAL_TABS_FIXED("fixed"),
    MATERIAL_BORDERED("bordered");
    //@formatter:on

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
