/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.customfield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class CustomFieldVariantTest {

    private CustomField<String> customField;

    @Before
    public void initTest() {
        customField = new CustomField<String>() {
            @Override
            protected String generateModelValue() {
                return null;
            }

            @Override
            protected void setPresentationValue(String newPresentationValue) {

            }
        };
    }

    @Test
    public void addLumoSmall_themeAttributeUpdated() {
        assertThemeAttribute(null);
        customField.addThemeVariants(CustomFieldVariant.LUMO_SMALL);
        assertThemeAttribute("small");
    }

    @Test
    public void addLumoHelperAboveField_themeAttributeUpdated() {
        assertThemeAttribute(null);
        customField
                .addThemeVariants(CustomFieldVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttribute("helper-above-field");
    }

    @Test
    public void addLumoWhitespace_themeAttributeUpdated() {
        assertThemeAttribute(null);
        customField.addThemeVariants(CustomFieldVariant.LUMO_WHITESPACE);
        assertThemeAttribute("whitespace");
    }

    @Test
    public void addAndRemoveMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        customField.addThemeVariants(CustomFieldVariant.LUMO_SMALL);
        customField
                .addThemeVariants(CustomFieldVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttributeContains("helper-above-field");
        assertThemeAttributeContains("small");
        customField.removeThemeVariants(
                CustomFieldVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttribute("small");
    }

    @Test
    public void addAndRemoveAllMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        customField.addThemeVariants(CustomFieldVariant.LUMO_SMALL);
        customField
                .addThemeVariants(CustomFieldVariant.LUMO_HELPER_ABOVE_FIELD);
        customField.getThemeNames().clear();
        assertThemeAttribute(null);
    }

    private void assertThemeAttribute(String expected) {
        String actual = customField.getThemeName();
        assertEquals("Unexpected theme attribute on custom field", expected,
                actual);
    }

    private void assertThemeAttributeContains(String expected) {
        String actual = customField.getThemeName();
        assertTrue("Theme attribute not present on custom field",
                actual.contains(expected));
    }
}
