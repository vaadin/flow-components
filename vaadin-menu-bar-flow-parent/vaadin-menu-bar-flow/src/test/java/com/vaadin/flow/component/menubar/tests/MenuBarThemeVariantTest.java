/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.menubar.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;

public class MenuBarThemeVariantTest {

    private MenuBar menuBar = new MenuBar();

    @Test
    public void addAndRemoveLumoTertiaryVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);
        assertThemeAttribute("tertiary");
        menuBar.removeThemeVariants(MenuBarVariant.LUMO_TERTIARY);
        assertThemeAttribute(null);
    }

    private void assertThemeAttribute(String expected) {
        String theme = menuBar.getElement().getAttribute("theme");
        Assert.assertEquals("Unexpected theme attribute on menu bar", expected,
                theme);
    }
}
