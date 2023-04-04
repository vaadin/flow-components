/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.shared.HasAllowedCharPattern;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.dom.ThemeList;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link PasswordField}.
 */
public class PasswordFieldTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void setValueNull() {
        PasswordField passwordField = new PasswordField();
        assertEquals("Value should be an empty string", "",
                passwordField.getValue());

        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Null value is not supported");

        passwordField.setValue(null);
    }

    @Test
    public void initialValuePropertyValue() {
        PasswordField passwordField = new PasswordField();
        assertEquals(passwordField.getEmptyValue(),
                passwordField.getElement().getProperty("value"));
    }

    @Test
    public void clearButtonVisiblePropertyValue() {
        PasswordField passwordField = new PasswordField();

        assertClearButtonPropertyValueEquals(passwordField, true);
        assertClearButtonPropertyValueEquals(passwordField, false);
    }

    public void assertClearButtonPropertyValueEquals(
            PasswordField passwordField, Boolean value) {
        passwordField.setClearButtonVisible(value);
        assertEquals(value, passwordField.isClearButtonVisible());
        assertEquals(passwordField.isClearButtonVisible(), passwordField
                .getElement().getProperty("clearButtonVisible", value));
    }

    @Test
    public void autoselectPropertyValue() {
        PasswordField passwordField = new PasswordField();

        assertAutoselectPropertyValueEquals(passwordField, true);
        assertAutoselectPropertyValueEquals(passwordField, false);
    }

    @Test
    public void elementHasValue_wrapIntoTextField_propertyIsNotSetToInitialValue() {
        ComponentFromTest
                .elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue(
                        "foo", PasswordField.class);
    }

    public void assertAutoselectPropertyValueEquals(PasswordField passwordField,
            Boolean value) {
        passwordField.setAutoselect(value);
        assertEquals(value, passwordField.isAutoselect());
        assertEquals(passwordField.isAutoselect(),
                passwordField.getElement().getProperty("autoselect", value));
    }

    @Test
    public void addThemeVariant_themeAttributeContainsThemeVariant() {
        PasswordField field = new PasswordField();
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);

        ThemeList themeNames = field.getThemeNames();
        Assert.assertTrue(themeNames
                .contains(TextFieldVariant.LUMO_SMALL.getVariantName()));
    }

    @Test
    public void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        PasswordField field = new PasswordField();
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        field.removeThemeVariants(TextFieldVariant.LUMO_SMALL);

        ThemeList themeNames = field.getThemeNames();
        Assert.assertFalse(themeNames
                .contains(TextFieldVariant.LUMO_SMALL.getVariantName()));
    }

    @Test
    public void implementsHasAllowedCharPattern() {
        assertTrue("PasswordField should support char pattern",
                HasAllowedCharPattern.class
                        .isAssignableFrom(new PasswordField().getClass()));
    }

    @Test
    public void implementHasAriaLabel() {
        PasswordField field = new PasswordField();
        Assert.assertTrue(field instanceof HasAriaLabel);
    }

    @Test
    public void setAriaLabel() {
        PasswordField field = new PasswordField();

        field.setAriaLabel("aria-label");
        Assert.assertTrue(field.getAriaLabel().isPresent());
        Assert.assertEquals("aria-label", field.getAriaLabel().get());

        field.setAriaLabel(null);
        Assert.assertTrue(field.getAriaLabel().isEmpty());
    }

    @Test
    public void setAriaLabelledBy() {
        PasswordField field = new PasswordField();

        field.setAriaLabelledBy("aria-labelledby");
        Assert.assertTrue(field.getAriaLabelledBy().isPresent());
        Assert.assertEquals("aria-labelledby", field.getAriaLabelledBy().get());

        field.setAriaLabelledBy(null);
        Assert.assertTrue(field.getAriaLabelledBy().isEmpty());
    }
}
