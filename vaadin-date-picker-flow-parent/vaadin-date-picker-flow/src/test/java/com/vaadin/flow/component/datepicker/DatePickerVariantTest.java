/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class DatePickerVariantTest {

    private DatePicker datePicker;

    @Before
    public void initTest() {
        datePicker = new DatePicker();
    }

    @Test
    public void addAndRemoveLumoAlignCenterVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_ALIGN_CENTER);
        assertThemeAttribute("align-center");
        datePicker.removeThemeVariants(DatePickerVariant.LUMO_ALIGN_CENTER);
        assertThemeAttribute(null);
    }

    @Test
    public void addAlignRightVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_ALIGN_RIGHT);
        assertThemeAttribute("align-right");
    }

    @Test
    public void addSmallVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        assertThemeAttribute("small");
    }

    @Test
    public void addAlignLeftVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_ALIGN_LEFT);
        assertThemeAttribute("align-left");
    }

    @Test
    public void addHelperAboveField_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.HELPER_ABOVE_FIELD);
        assertThemeAttribute("helper-above-field");
    }

    @Test
    public void addAndRemoveMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        datePicker.addThemeVariants(DatePickerVariant.HELPER_ABOVE_FIELD);
        assertThemeAttributeContains("helper-above-field");
        assertThemeAttributeContains("small");
        datePicker.removeThemeVariants(DatePickerVariant.HELPER_ABOVE_FIELD);
        assertThemeAttribute("small");
    }

    @Test
    public void addAndRemoveAllMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        datePicker.addThemeVariants(DatePickerVariant.HELPER_ABOVE_FIELD);
        datePicker.getThemeNames().clear();
        assertThemeAttribute(null);
    }

    @Test
    public void addTwiceAndSeeIbce_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        assertThemeAttribute("small");
    }

    private void assertThemeAttribute(String expected) {
        String actual = datePicker.getThemeName();
        assertEquals("Unexpected theme attribute on date picker", expected,
                actual);
    }

    private void assertThemeAttributeContains(String expected) {
        String actual = datePicker.getThemeName();
        assertTrue("Theme attribute not present on date picker",
                actual.contains(expected));
    }
}
