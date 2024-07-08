/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.dialog;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DialogThemeVariantTest {

    private final Dialog dialog = new Dialog();

    @Test
    public void addAndRemoveLumoNoPaddingVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        dialog.addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        assertThemeAttribute("no-padding");
        dialog.removeThemeVariants(DialogVariant.LUMO_NO_PADDING);
        assertThemeAttribute(null);
    }

    @Test
    public void addAndRemoveMaterialNoPaddingVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        dialog.addThemeVariants(DialogVariant.MATERIAL_NO_PADDING);
        assertThemeAttribute("no-padding");
        dialog.removeThemeVariants(DialogVariant.MATERIAL_NO_PADDING);
        assertThemeAttribute(null);
    }

    private void assertThemeAttribute(String expected) {
        String actual = dialog.getThemeName();
        assertEquals("Unexpected theme attribute on dialog", expected, actual);
    }

}
