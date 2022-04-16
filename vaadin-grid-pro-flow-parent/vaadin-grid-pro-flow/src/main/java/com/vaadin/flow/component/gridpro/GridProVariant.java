/*
 * #%L
 * Vaadin GridPro
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */
package com.vaadin.flow.component.gridpro;

/**
 * Set of theme variants applicable for {@code vaadin-grid-pro} component.
 */
public enum GridProVariant {
    //@formatter:off
    LUMO_NO_BORDER("no-border"),
    LUMO_NO_ROW_BORDERS("no-row-borders"),
    LUMO_COLUMN_BORDERS("column-borders"),
    LUMO_ROW_STRIPES("row-stripes"),
    LUMO_COMPACT("compact"),
    LUMO_WRAP_CELL_CONTENT("wrap-cell-content"),
    MATERIAL_COLUMN_DIVIDERS("column-dividers"),
    LUMO_HIGHLIGHT_EDITABLE_CELLS("highlight-editable-cells"),
    LUMO_HIGHLIGHT_READ_ONLY_CELLS("highlight-read-only-cells");

    //@formatter:on

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