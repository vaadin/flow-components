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
    /**
     * @deprecated Use {@link #NO_BORDER} instead.
     */
    @Deprecated
    AURA_NO_BORDER("no-border"),
    /**
     * @deprecated Use {@link #NO_ROW_BORDERS} instead.
     */
    @Deprecated
    AURA_NO_ROW_BORDERS("no-row-borders"),
    /**
     * @deprecated Use {@link #COLUMN_BORDERS} instead.
     */
    @Deprecated
    AURA_COLUMN_BORDERS("column-borders"),
    /**
     * @deprecated Use {@link #ROW_STRIPES} instead.
     */
    @Deprecated
    AURA_ROW_STRIPES("row-stripes"),
    /**
     * @deprecated Use {@link #WRAP_CELL_CONTENT} instead.
     */
    @Deprecated
    AURA_WRAP_CELL_CONTENT("wrap-cell-content"),
    /**
     * @deprecated Use {@link #HIGHLIGHT_EDITABLE_CELLS} instead.
     */
    @Deprecated
    AURA_HIGHLIGHT_EDITABLE_CELLS("highlight-editable-cells"),
    /**
     * @deprecated Use {@link #HIGHLIGHT_READ_ONLY_CELLS} instead.
     */
    @Deprecated
    AURA_HIGHLIGHT_READ_ONLY_CELLS("highlight-read-only-cells"),
    NO_BORDER("no-border"),
    NO_ROW_BORDERS("no-row-borders"),
    COLUMN_BORDERS("column-borders"),
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
