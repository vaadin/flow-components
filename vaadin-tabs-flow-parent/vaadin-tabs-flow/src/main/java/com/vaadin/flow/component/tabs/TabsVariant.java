/*
 * Copyright 2000-2025 Vaadin Ltd.
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
    LUMO_EQUAL_WIDTH_TABS("equal-width-tabs");

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
