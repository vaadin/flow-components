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
package com.vaadin.flow.component.datepicker;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DatePickerVariantTest {

    private final DatePicker datePicker = new DatePicker();

    @Test
    public void addAndRemoveLumoAlignCenterVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_ALIGN_CENTER);
        assertThemeAttribute("align-center");
        datePicker.removeThemeVariants(DatePickerVariant.LUMO_ALIGN_CENTER);
        assertThemeAttribute(null);
    }

    @Test
    public void addAndRemoveLumoAlignRightVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_ALIGN_RIGHT);
        assertThemeAttribute("align-right");
        datePicker.removeThemeVariants(DatePickerVariant.LUMO_ALIGN_RIGHT);
        assertThemeAttribute(null);
    }

    @Test
    public void addAndRemoveLumoSmallVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        assertThemeAttribute("small");
        datePicker.removeThemeVariants(DatePickerVariant.LUMO_SMALL);
        assertThemeAttribute(null);
    }

    @Test
    public void addAndRemoveLumoHelperAboveField_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttribute("helper-above-field");
        datePicker
                .removeThemeVariants(DatePickerVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttribute(null);
    }

    private void assertThemeAttribute(String expected) {
        String actual = datePicker.getThemeName();
        assertEquals("Unexpected theme attribute on dialog", expected, actual);
    }
}
