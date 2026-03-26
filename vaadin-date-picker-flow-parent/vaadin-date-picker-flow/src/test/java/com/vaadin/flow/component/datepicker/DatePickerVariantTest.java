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
package com.vaadin.flow.component.datepicker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DatePickerVariantTest {

    private DatePicker datePicker;

    @BeforeEach
    void initTest() {
        datePicker = new DatePicker();
    }

    @Test
    void addAndRemoveLumoAlignCenterVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_ALIGN_CENTER);
        assertThemeAttribute("align-center");
        datePicker.removeThemeVariants(DatePickerVariant.LUMO_ALIGN_CENTER);
        assertThemeAttribute(null);
    }

    @Test
    void addLumoAlignRightVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_ALIGN_RIGHT);
        assertThemeAttribute("align-right");
    }

    @Test
    void addLumoSmallVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        assertThemeAttribute("small");
    }

    @Test
    void addLumoAlignLeftVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_ALIGN_LEFT);
        assertThemeAttribute("align-left");
    }

    @Test
    void addLumoHelperAboveField_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttribute("helper-above-field");
    }

    @Test
    void addAndRemoveMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttributeContains("helper-above-field");
        assertThemeAttributeContains("small");
        datePicker
                .removeThemeVariants(DatePickerVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttribute("small");
    }

    @Test
    void addAndRemoveAllMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_HELPER_ABOVE_FIELD);
        datePicker.getThemeNames().clear();
        assertThemeAttribute(null);
    }

    @Test
    void addTwiceAndSeeIbce_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        assertThemeAttribute("small");
    }

    private void assertThemeAttribute(String expected) {
        String actual = datePicker.getThemeName();
        assertEquals(expected, actual,
                "Unexpected theme attribute on date picker");
    }

    private void assertThemeAttributeContains(String expected) {
        String actual = datePicker.getThemeName();
        assertTrue(actual.contains(expected),
                "Theme attribute not present on date picker");
    }
}
