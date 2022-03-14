/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;

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
