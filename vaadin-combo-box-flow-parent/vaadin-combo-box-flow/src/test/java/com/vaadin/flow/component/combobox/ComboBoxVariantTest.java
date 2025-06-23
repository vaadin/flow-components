/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.dom.ThemeList;

public class ComboBoxVariantTest {

    @Test
    public void addThemeVariant_themeNamesContainsThemeVariant() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);

        ThemeList themeNames = comboBox.getThemeNames();
        Assert.assertTrue(themeNames
                .contains(ComboBoxVariant.LUMO_SMALL.getVariantName()));
    }

    @Test
    public void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        comboBox.removeThemeVariants(ComboBoxVariant.LUMO_SMALL);

        ThemeList themeNames = comboBox.getThemeNames();
        Assert.assertFalse(themeNames
                .contains(ComboBoxVariant.LUMO_SMALL.getVariantName()));
    }
}
