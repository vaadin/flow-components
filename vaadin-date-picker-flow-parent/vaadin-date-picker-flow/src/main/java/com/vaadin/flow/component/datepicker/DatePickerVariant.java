/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.datepicker;

/**
 * Set of theme variants applicable for {@code vaadin-date-picker} component.
 */
public enum DatePickerVariant {
    LUMO_SMALL("small"), LUMO_ALIGN_LEFT("align-left"), LUMO_ALIGN_CENTER(
            "align-center"), LUMO_ALIGN_RIGHT(
                    "align-right"), LUMO_HELPER_ABOVE_FIELD(
                            "helper-above-field"), MATERIAL_ALWAYS_FLOAT_LABEL(
                                    "always-float-label");

    private final String variant;

    DatePickerVariant(String variant) {
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
