/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox;

import org.junit.Test;

import com.vaadin.tests.ThemeVariantTestHelper;

public class MultiSelectComboBoxVariantTest {

    @Test
    public void addThemeVariant_themeNamesContainsThemeVariant() {
        ThemeVariantTestHelper.addThemeVariant_themeNamesContainsThemeVariant(
                new MultiSelectComboBox<>(),
                MultiSelectComboBoxVariant.LUMO_SMALL);
    }

    @Test
    public void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        ThemeVariantTestHelper
                .addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant(
                        new MultiSelectComboBox<>(),
                        MultiSelectComboBoxVariant.LUMO_SMALL);
    }
}
