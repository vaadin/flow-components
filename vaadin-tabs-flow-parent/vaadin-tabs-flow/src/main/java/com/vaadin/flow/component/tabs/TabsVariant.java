/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.tabs;

/**
 * Set of theme variants applicable for {@code vaadin-tabs} component.
 */
public enum TabsVariant {
    LUMO_ICON_ON_TOP("icon-on-top"), LUMO_CENTERED("centered"), LUMO_SMALL(
            "small"), LUMO_MINIMAL("minimal"), LUMO_HIDE_SCROLL_BUTTONS(
                    "hide-scroll-buttons"), LUMO_EQUAL_WIDTH_TABS(
                            "equal-width-tabs"), MATERIAL_FIXED("fixed");

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