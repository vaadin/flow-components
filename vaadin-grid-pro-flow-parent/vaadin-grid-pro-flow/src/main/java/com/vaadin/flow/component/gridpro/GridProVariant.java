/**
 * Copyright 2000-2025 Vaadin Ltd.
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
    /**
     * @deprecated Use {@link #NO_BORDER} instead.
     */
    @Deprecated(since = "25.0", forRemoval = true)
    LUMO_NO_BORDER("no-border"),
    LUMO_NO_ROW_BORDERS("no-row-borders"),
    LUMO_COLUMN_BORDERS("column-borders"),
    /**
     * @deprecated Use {@link #ROW_STRIPES} instead.
     */
    @Deprecated(since = "25.0", forRemoval = true)
    LUMO_ROW_STRIPES("row-stripes"),
    LUMO_COMPACT("compact"),
    /**
     * @deprecated Use {@link #WRAP_CELL_CONTENT} instead.
     */
    @Deprecated(since = "25.0", forRemoval = true)
    LUMO_WRAP_CELL_CONTENT("wrap-cell-content"),
    /**
     * @deprecated Use {@link #HIGHLIGHT_EDITABLE_CELLS} instead.
     */
    @Deprecated(since = "25.0", forRemoval = true)
    LUMO_HIGHLIGHT_EDITABLE_CELLS("highlight-editable-cells"),
    /**
     * @deprecated Use {@link #HIGHLIGHT_READ_ONLY_CELLS} instead.
     */
    @Deprecated(since = "25.0", forRemoval = true)
    LUMO_HIGHLIGHT_READ_ONLY_CELLS("highlight-read-only-cells"),
    NO_BORDER("no-border"),
    ROW_STRIPES("row-stripes"),
    WRAP_CELL_CONTENT("wrap-cell-content"),
    HIGHLIGHT_EDITABLE_CELLS("highlight-editable-cells"),
    HIGHLIGHT_READ_ONLY_CELLS("highlight-read-only-cells");

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
