/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
    public void addAndRemoveLumoAlignCenterVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        dateTimePicker
                .addThemeVariants(DateTimePickerVariant.LUMO_ALIGN_CENTER);
        assertThemeAttribute("align-center");
        dateTimePicker
                .removeThemeVariants(DateTimePickerVariant.LUMO_ALIGN_CENTER);
        assertThemeAttribute(null);
    }

    @Test
    public void addLumoAlignRightVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        dateTimePicker.addThemeVariants(DateTimePickerVariant.LUMO_ALIGN_RIGHT);
        assertThemeAttribute("align-right");
    }

    @Test
    public void addLumoSmallVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        dateTimePicker.addThemeVariants(DateTimePickerVariant.LUMO_SMALL);
        assertThemeAttribute("small");
    }

    @Test
    public void addLumoAlignLeftVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        dateTimePicker.addThemeVariants(DateTimePickerVariant.LUMO_ALIGN_LEFT);
        assertThemeAttribute("align-left");
    }

    @Test
    public void addLumoHelperAboveField_themeAttributeUpdated() {
        assertThemeAttribute(null);
        dateTimePicker.addThemeVariants(
                DateTimePickerVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttribute("helper-above-field");
    }

    @Test
    public void addMaterialAlwaysFloatLabel_themeAttributeUpdated() {
        assertThemeAttribute(null);
        dateTimePicker.addThemeVariants(
                DateTimePickerVariant.MATERIAL_ALWAYS_FLOAT_LABEL);
        assertThemeAttribute("always-float-label");
    }

    @Test
    public void addAndRemoveMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        dateTimePicker.addThemeVariants(DateTimePickerVariant.LUMO_SMALL);
        dateTimePicker.addThemeVariants(
                DateTimePickerVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttributeContains("helper-above-field");
        assertThemeAttributeContains("small");
        dateTimePicker.removeThemeVariants(
                DateTimePickerVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttribute("small");
    }

    @Test
    public void addAndRemoveAllMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        dateTimePicker.addThemeVariants(DateTimePickerVariant.LUMO_SMALL);
        dateTimePicker.addThemeVariants(
                DateTimePickerVariant.LUMO_HELPER_ABOVE_FIELD);
        dateTimePicker.getThemeNames().clear();
        assertThemeAttribute(null);
    }

    @Test
    public void addTwiceAndSeeIbce_themeAttributeUpdated() {
        assertThemeAttribute(null);
        dateTimePicker.addThemeVariants(DateTimePickerVariant.LUMO_SMALL);
        dateTimePicker.addThemeVariants(DateTimePickerVariant.LUMO_SMALL);
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
