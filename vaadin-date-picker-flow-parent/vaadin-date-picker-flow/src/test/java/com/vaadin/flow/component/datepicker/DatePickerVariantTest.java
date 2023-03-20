
package com.vaadin.flow.component.datepicker;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;

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
    public void addLumoAlignRightVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_ALIGN_RIGHT);
        assertThemeAttribute("align-right");
    }

    @Test
    public void addLumoSmallVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        assertThemeAttribute("small");
    }

    @Test
    public void addLumoAlignLeftVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_ALIGN_LEFT);
        assertThemeAttribute("align-left");
    }

    @Test
    public void addLumoHelperAboveField_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttribute("helper-above-field");
    }

    @Test
    public void addMaterialAlwaysFloatLabel_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(
                DatePickerVariant.MATERIAL_ALWAYS_FLOAT_LABEL);
        assertThemeAttribute("always-float-label");
    }

    @Test
    public void addAndRemoveMultipleVariants_themeAttributeUpdated() {
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
    public void addAndRemoveAllMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        datePicker.addThemeVariants(DatePickerVariant.LUMO_HELPER_ABOVE_FIELD);
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
