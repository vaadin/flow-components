/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.timepicker.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.timepicker.TimePickerVariant;

public class TimePickerVariantTest {

    private TimePicker timePicker;

    @Before
    public void initTest() {
        timePicker = new TimePicker();
    }

    @Test
    public void addAndRemoveLumoAlignCenterVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        timePicker.addThemeVariants(TimePickerVariant.LUMO_ALIGN_CENTER);
        assertThemeAttribute("align-center");
        timePicker.removeThemeVariants(TimePickerVariant.LUMO_ALIGN_CENTER);
        assertThemeAttribute(null);
    }

    @Test
    public void addLumoAlignRightVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        timePicker.addThemeVariants(TimePickerVariant.LUMO_ALIGN_RIGHT);
        assertThemeAttribute("align-right");
    }

    @Test
    public void addLumoSmallVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        timePicker.addThemeVariants(TimePickerVariant.LUMO_SMALL);
        assertThemeAttribute("small");
    }

    @Test
    public void addLumoAlignLeftVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        timePicker.addThemeVariants(TimePickerVariant.LUMO_ALIGN_LEFT);
        assertThemeAttribute("align-left");
    }

    @Test
    public void addLumoHelperAboveField_themeAttributeUpdated() {
        assertThemeAttribute(null);
        timePicker.addThemeVariants(TimePickerVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttribute("helper-above-field");
    }

    @Test
    public void addMaterialAlwaysFloatLabel_themeAttributeUpdated() {
        assertThemeAttribute(null);
        timePicker.addThemeVariants(
                TimePickerVariant.MATERIAL_ALWAYS_FLOAT_LABEL);
        assertThemeAttribute("always-float-label");
    }

    @Test
    public void addAndRemoveMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        timePicker.addThemeVariants(TimePickerVariant.LUMO_SMALL);
        timePicker.addThemeVariants(TimePickerVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttributeContains("helper-above-field");
        assertThemeAttributeContains("small");
        timePicker
                .removeThemeVariants(TimePickerVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttribute("small");
    }

    @Test
    public void addAndRemoveAllMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        timePicker.addThemeVariants(TimePickerVariant.LUMO_SMALL);
        timePicker.addThemeVariants(TimePickerVariant.LUMO_HELPER_ABOVE_FIELD);
        timePicker.getThemeNames().clear();
        assertThemeAttribute(null);
    }

    @Test
    public void addTwiceAndSeeIbce_themeAttributeUpdated() {
        assertThemeAttribute(null);
        timePicker.addThemeVariants(TimePickerVariant.LUMO_SMALL);
        timePicker.addThemeVariants(TimePickerVariant.LUMO_SMALL);
        assertThemeAttribute("small");
    }

    private void assertThemeAttribute(String expected) {
        String actual = timePicker.getThemeName();
        assertEquals("Unexpected theme attribute on time picker", expected,
                actual);
    }

    private void assertThemeAttributeContains(String expected) {
        String actual = timePicker.getThemeName();
        assertTrue("Theme attribute not present on time picker",
                actual.contains(expected));
    }
}
