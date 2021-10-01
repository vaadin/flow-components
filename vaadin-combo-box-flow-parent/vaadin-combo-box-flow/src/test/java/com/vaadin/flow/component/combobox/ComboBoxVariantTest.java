/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.flow.component.combobox;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ComboBoxVariantTest {

    private final ComboBox<String> comboBox = new ComboBox<>();

    @Test
    public void addAndRemoveLumoAlignCenterVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        comboBox.addThemeVariants(ComboBoxVariant.LUMO_ALIGN_CENTER);
        assertThemeAttribute("align-center");
        comboBox.removeThemeVariants(ComboBoxVariant.LUMO_ALIGN_CENTER);
        assertThemeAttribute(null);
    }

    @Test
    public void addAndRemoveLumoAlignRightVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        comboBox.addThemeVariants(ComboBoxVariant.LUMO_ALIGN_RIGHT);
        assertThemeAttribute("align-right");
        comboBox.removeThemeVariants(ComboBoxVariant.LUMO_ALIGN_RIGHT);
        assertThemeAttribute(null);
    }

    @Test
    public void addAndRemoveLumoSmallVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        comboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        assertThemeAttribute("small");
        comboBox.removeThemeVariants(ComboBoxVariant.LUMO_SMALL);
        assertThemeAttribute(null);
    }

    @Test
    public void addAndRemoveLumoHelperAboveField_themeAttributeUpdated() {
        assertThemeAttribute(null);
        comboBox.addThemeVariants(ComboBoxVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttribute("helper-above-field");
        comboBox.removeThemeVariants(ComboBoxVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttribute(null);
    }

    private void assertThemeAttribute(String expected) {
        String actual = comboBox.getThemeName();
        assertEquals("Unexpected theme attribute on dialog", expected, actual);
    }
}
