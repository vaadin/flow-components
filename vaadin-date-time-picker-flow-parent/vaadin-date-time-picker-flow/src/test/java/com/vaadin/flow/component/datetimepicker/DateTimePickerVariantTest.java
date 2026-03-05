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
package com.vaadin.flow.component.datetimepicker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class DateTimePickerVariantTest {

    private DateTimePicker dateTimePicker;

    @Before
    public void initTest() {
        dateTimePicker = new DateTimePicker();
    }

    @Test
    public void addAndRemoveAlignCenterVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        dateTimePicker.addThemeVariants(DateTimePickerVariant.ALIGN_CENTER);
        assertThemeAttribute("align-center");
        dateTimePicker.removeThemeVariants(DateTimePickerVariant.ALIGN_CENTER);
        assertThemeAttribute(null);
    }

    @Test
    public void addAlignRightVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        dateTimePicker.addThemeVariants(DateTimePickerVariant.ALIGN_RIGHT);
        assertThemeAttribute("align-right");
    }

    @Test
    public void addSmallVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        dateTimePicker.addThemeVariants(DateTimePickerVariant.SMALL);
        assertThemeAttribute("small");
    }

    @Test
    public void addAlignLeftVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        dateTimePicker.addThemeVariants(DateTimePickerVariant.ALIGN_LEFT);
        assertThemeAttribute("align-left");
    }

    @Test
    public void addHelperAbove_themeAttributeUpdated() {
        assertThemeAttribute(null);
        dateTimePicker.addThemeVariants(DateTimePickerVariant.HELPER_ABOVE);
        assertThemeAttribute("helper-above-field");
    }

    @Test
    public void addAndRemoveMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        dateTimePicker.addThemeVariants(DateTimePickerVariant.SMALL);
        dateTimePicker.addThemeVariants(DateTimePickerVariant.HELPER_ABOVE);
        assertThemeAttributeContains("helper-above-field");
        assertThemeAttributeContains("small");
        dateTimePicker.removeThemeVariants(DateTimePickerVariant.HELPER_ABOVE);
        assertThemeAttribute("small");
    }

    @Test
    public void addAndRemoveAllMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        dateTimePicker.addThemeVariants(DateTimePickerVariant.SMALL);
        dateTimePicker.addThemeVariants(DateTimePickerVariant.HELPER_ABOVE);
        dateTimePicker.getThemeNames().clear();
        assertThemeAttribute(null);
    }

    @Test
    public void addTwiceAndSeeOnce_themeAttributeUpdated() {
        assertThemeAttribute(null);
        dateTimePicker.addThemeVariants(DateTimePickerVariant.SMALL);
        dateTimePicker.addThemeVariants(DateTimePickerVariant.SMALL);
        assertThemeAttribute("small");
    }

    private void assertThemeAttribute(String expected) {
        String actual = dateTimePicker.getThemeName();
        assertEquals("Unexpected theme attribute on date time picker", expected,
                actual);
    }

    private void assertThemeAttributeContains(String expected) {
        String actual = dateTimePicker.getThemeName();
        assertTrue("Theme attribute not present on date time picker",
                actual.contains(expected));
    }
}
