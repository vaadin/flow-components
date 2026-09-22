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
package com.vaadin.flow.component.slider;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for the slider components.
 *
 * @since 25.4
 */
public enum SliderVariant implements ThemeVariant {
    /**
     * Places the helper text above the slider instead of below it.
     */
    HELPER_ABOVE("helper-above-field"),
    /**
     * Places the label next to the slider instead of above it.
     */
    LABEL_ASIDE("label-aside");

    private final String variant;

    SliderVariant(String variant) {
        this.variant = variant;
    }

    /**
     * Gets the variant name.
     *
     * @return variant name
     */
    @Override
    public String getVariantName() {
        return variant;
    }
}
