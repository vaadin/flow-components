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
package com.vaadin.flow.component.customfield;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomFieldVariantTest {

    private CustomField<String> customField;

    @BeforeEach
    void initTest() {
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
    void addSmall_themeAttributeUpdated() {
        assertThemeAttribute(null);
        customField.addThemeVariants(CustomFieldVariant.SMALL);
        assertThemeAttribute("small");
    }

    @Test
    void addHelperAbove_themeAttributeUpdated() {
        assertThemeAttribute(null);
        customField.addThemeVariants(CustomFieldVariant.HELPER_ABOVE);
        assertThemeAttribute("helper-above-field");
    }

    @Test
    void addLumoWhitespace_themeAttributeUpdated() {
        assertThemeAttribute(null);
        customField.addThemeVariants(CustomFieldVariant.LUMO_WHITESPACE);
        assertThemeAttribute("whitespace");
    }

    @Test
    void addAndRemoveMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        customField.addThemeVariants(CustomFieldVariant.SMALL);
        customField.addThemeVariants(CustomFieldVariant.HELPER_ABOVE);
        assertThemeAttributeContains("helper-above-field");
        assertThemeAttributeContains("small");
        customField.removeThemeVariants(CustomFieldVariant.HELPER_ABOVE);
        assertThemeAttribute("small");
    }

    @Test
    void addAndRemoveAllMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        customField.addThemeVariants(CustomFieldVariant.SMALL);
        customField.addThemeVariants(CustomFieldVariant.HELPER_ABOVE);
        customField.getThemeNames().clear();
        assertThemeAttribute(null);
    }

    private void assertThemeAttribute(String expected) {
        String actual = customField.getThemeName();
        assertEquals(expected, actual,
                "Unexpected theme attribute on custom field");
    }

    private void assertThemeAttributeContains(String expected) {
        String actual = customField.getThemeName();
        assertTrue(actual.contains(expected),
                "Theme attribute not present on custom field");
    }
}
