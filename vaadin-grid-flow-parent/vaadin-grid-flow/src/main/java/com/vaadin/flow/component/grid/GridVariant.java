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
package com.vaadin.flow.component.grid;

/**
 * Set of theme variants applicable for {@code vaadin-grid} component.
 */
public enum GridVariant {
    LUMO_NO_BORDER("no-border"),
    LUMO_NO_ROW_BORDERS("no-row-borders"),
    LUMO_COLUMN_BORDERS("column-borders"),
    LUMO_ROW_STRIPES("row-stripes"),
    LUMO_COMPACT("compact"),
    LUMO_WRAP_CELL_CONTENT("wrap-cell-content"),
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
    NO_BORDER("no-border"),
    NO_ROW_BORDERS("no-row-borders"),
    COLUMN_BORDERS("column-borders"),
    ROW_STRIPES("row-stripes"),
    WRAP_CELL_CONTENT("wrap-cell-content");

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
