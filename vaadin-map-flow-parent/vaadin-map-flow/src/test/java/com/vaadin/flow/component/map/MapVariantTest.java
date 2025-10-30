/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map;

import org.junit.Test;

import com.vaadin.tests.ThemeVariantTestHelper;

public class MapVariantTest {
    @Test
    public void addThemeVariant_themeNamesContainsThemeVariant() {
        ThemeVariantTestHelper.addThemeVariant_themeNamesContainsThemeVariant(
                new Map(), MapVariant.LUMO_NO_BORDER);
    }

    @Test
    public void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        ThemeVariantTestHelper
                .addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant(
                        new Map(), MapVariant.LUMO_NO_BORDER);
    }
}
