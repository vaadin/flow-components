package com.vaadin.flow.component.richtexteditor;

/*
 * #%L
 * Vaadin RichTextEditor for Vaadin 10
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

/**
 * Set of theme variants applicable for {@code vaadin-rich-text-editor}
 * component.
 */
public enum RichTextEditorVariant {
    LUMO_NO_BORDER("no-border"), LUMO_COMPACT("compact"), MATERIAL_NO_BORDER(
            "no-border");

    private final String variant;

    RichTextEditorVariant(String variant) {
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
