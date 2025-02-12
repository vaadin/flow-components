/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.richtexteditor;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for {@code vaadin-rich-text-editor}
 * component.
 */
public enum RichTextEditorVariant implements ThemeVariant {
    LUMO_NO_BORDER("no-border"),
    LUMO_COMPACT("compact"),
    /**
     * @deprecated Since 24.7, the Material theme is deprecated and will be
     *             removed in Vaadin 25.
     */
    @Deprecated
    MATERIAL_NO_BORDER("no-border");

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
