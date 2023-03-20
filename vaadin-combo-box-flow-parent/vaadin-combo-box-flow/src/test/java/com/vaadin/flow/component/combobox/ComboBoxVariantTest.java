
package com.vaadin.flow.component.combobox;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;

public class ComboBoxVariantTest {

    private ComboBox<String> comboBox;

    @Before
    public void initTest() {
        comboBox = new ComboBox<>();
    }

    @Test
    public void addAndRemoveLumoAlignCenterVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        comboBox.addThemeVariants(ComboBoxVariant.LUMO_ALIGN_CENTER);
        assertThemeAttribute("align-center");
        comboBox.removeThemeVariants(ComboBoxVariant.LUMO_ALIGN_CENTER);
        assertThemeAttribute(null);
    }

    @Test
    public void addLumoAlignRightVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        comboBox.addThemeVariants(ComboBoxVariant.LUMO_ALIGN_RIGHT);
        assertThemeAttribute("align-right");
    }

    @Test
    public void addLumoSmallVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        comboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        assertThemeAttribute("small");
    }

    @Test
    public void addLumoAlignLeftVariant_themeAttributeUpdated() {
        assertThemeAttribute(null);
        comboBox.addThemeVariants(ComboBoxVariant.LUMO_ALIGN_LEFT);
        assertThemeAttribute("align-left");
    }

    @Test
    public void addLumoHelperAboveField_themeAttributeUpdated() {
        assertThemeAttribute(null);
        comboBox.addThemeVariants(ComboBoxVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttribute("helper-above-field");
    }

    @Test
    public void addMaterialAlwaysFloatLabel_themeAttributeUpdated() {
        assertThemeAttribute(null);
        comboBox.addThemeVariants(ComboBoxVariant.MATERIAL_ALWAYS_FLOAT_LABEL);
        assertThemeAttribute("always-float-label");
    }

    @Test
    public void addAndRemoveMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        comboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        comboBox.addThemeVariants(ComboBoxVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttributeContains("helper-above-field");
        assertThemeAttributeContains("small");
        comboBox.removeThemeVariants(ComboBoxVariant.LUMO_HELPER_ABOVE_FIELD);
        assertThemeAttribute("small");
    }

    @Test
    public void addAndRemoveAllMultipleVariants_themeAttributeUpdated() {
        assertThemeAttribute(null);
        comboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        comboBox.addThemeVariants(ComboBoxVariant.LUMO_HELPER_ABOVE_FIELD);
        comboBox.getThemeNames().clear();
        assertThemeAttribute(null);
    }

    @Test
    public void addTwiceAndSeeIbce_themeAttributeUpdated() {
        assertThemeAttribute(null);
        comboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        comboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        assertThemeAttribute("small");
    }

    private void assertThemeAttribute(String expected) {
        String actual = comboBox.getThemeName();
        assertEquals("Unexpected theme attribute on combobox", expected,
                actual);
    }

    private void assertThemeAttributeContains(String expected) {
        String actual = comboBox.getThemeName();
        assertTrue("Theme attribute not present on combobox",
                actual.contains(expected));
    }
}
