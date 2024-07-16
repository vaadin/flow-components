/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid;

/**
 * Set of theme variants applicable for {@code vaadin-grid} component.
 */
public enum GridVariant {
    LUMO_NO_BORDER("no-border"), LUMO_NO_ROW_BORDERS(
            "no-row-borders"), LUMO_COLUMN_BORDERS(
                    "column-borders"), LUMO_ROW_STRIPES(
                            "row-stripes"), LUMO_COMPACT(
                                    "compact"), LUMO_WRAP_CELL_CONTENT(
                                            "wrap-cell-content"), MATERIAL_COLUMN_DIVIDERS(
                                                    "column-dividers");

    private final String variant;

    GridVariant(String variant) {
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