/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.gridpro;

/**
 * Set of theme variants applicable for {@code vaadin-grid-pro} component.
 */
public enum GridProVariant {
    LUMO_NO_BORDER("no-border"),
    LUMO_NO_ROW_BORDERS("no-row-borders"),
    LUMO_COLUMN_BORDERS("column-borders"),
    LUMO_ROW_STRIPES("row-stripes"),
    LUMO_COMPACT("compact"),
    LUMO_WRAP_CELL_CONTENT("wrap-cell-content"),
    LUMO_HIGHLIGHT_EDITABLE_CELLS("highlight-editable-cells"),
    LUMO_HIGHLIGHT_READ_ONLY_CELLS("highlight-read-only-cells"),
    AURA_NO_BORDER("no-border"),
    AURA_NO_ROW_BORDERS("no-row-borders"),
    AURA_COLUMN_BORDERS("column-borders"),
    AURA_ROW_STRIPES("row-stripes"),
    AURA_WRAP_CELL_CONTENT("wrap-cell-content"),
    AURA_HIGHLIGHT_EDITABLE_CELLS("highlight-editable-cells"),
    AURA_HIGHLIGHT_READ_ONLY_CELLS("highlight-read-only-cells");

    private final String variant;

    GridProVariant(String variant) {
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
