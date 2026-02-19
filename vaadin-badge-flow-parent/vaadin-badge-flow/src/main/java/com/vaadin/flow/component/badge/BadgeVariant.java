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
package com.vaadin.flow.component.badge;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for {@code vaadin-badge} component.
 */
public enum BadgeVariant implements ThemeVariant {
    CONTRAST("contrast"),
    DOT("dot"),
    ERROR("error"),
    FILLED("filled"),
    ICON_ONLY("icon-only"),
    NUMBER_ONLY("number-only"),
    PRIMARY("primary"),
    SMALL("small"),
    SUCCESS("success"),
    WARNING("warning");

    private final String variant;

    BadgeVariant(String variant) {
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
