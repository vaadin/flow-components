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
 * Set of theme variants applicable for {@code vaadin-tabsheet} component.
 */
public enum TabSheetVariant implements ThemeVariant {
    LUMO_TABS_CENTERED("centered"),
    LUMO_TABS_SMALL("small"),
    LUMO_TABS_MINIMAL("minimal"),
    /**
     * @deprecated Use {@link #TABS_HIDE_SCROLL_BUTTONS} instead.
     */
    @Deprecated(since = "25.0", forRemoval = true)
    LUMO_TABS_HIDE_SCROLL_BUTTONS("hide-scroll-buttons"),
    LUMO_TABS_EQUAL_WIDTH_TABS("equal-width-tabs"),
    LUMO_BORDERED("bordered"),
    /**
     * @deprecated Use {@link #NO_PADDING} instead.
     */
    @Deprecated(since = "25.0", forRemoval = true)
    LUMO_NO_PADDING("no-padding"),
    NO_BORDER("no-border"),
    NO_PADDING("no-padding"),
    TABS_HIDE_SCROLL_BUTTONS("hide-scroll-buttons"),
    TABS_SHOW_SCROLL_BUTTONS("show-scroll-buttons");

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
