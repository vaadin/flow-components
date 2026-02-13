/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.richtexteditor;

import org.junit.Test;

import com.vaadin.flow.component.shared.ThemeVariantTestHelper;

public class RichTextEditorVariantTest {

    @Test
    public void addThemeVariant_themeNamesContainsThemeVariant() {
        ThemeVariantTestHelper.addThemeVariant_themeNamesContainsThemeVariant(
                new RichTextEditor(), RichTextEditorVariant.LUMO_NO_BORDER);
    }

    @Test
    public void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        ThemeVariantTestHelper
                .addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant(
                        new RichTextEditor(),
                        RichTextEditorVariant.LUMO_NO_BORDER);
    }
}
