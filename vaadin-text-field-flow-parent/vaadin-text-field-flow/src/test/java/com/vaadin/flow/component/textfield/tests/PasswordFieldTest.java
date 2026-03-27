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
package com.vaadin.flow.component.textfield.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.shared.HasAllowedCharPattern;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.tests.MockUIExtension;

/**
 * Tests for the {@link PasswordField}.
 */
class PasswordFieldTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void setValueNull() {
        PasswordField passwordField = new PasswordField();
        assertEquals("", passwordField.getValue(),
                "Value should be an empty string");

        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class, () -> passwordField.setValue(null));
        Assertions.assertTrue(
                exception.getMessage().contains("Null value is not supported"));
    }

    @Test
    void initialValueIsNotSpecified_valuePropertyHasEmptyString() {
        PasswordField passwordField = new PasswordField();
        Assertions.assertEquals("", passwordField.getValue());
        Assertions.assertEquals("",
                passwordField.getElement().getProperty("value"));
    }

    @Test
    void initialValueIsNull_valuePropertyHasEmptyString() {
        PasswordField passwordField = new PasswordField((String) null);
        Assertions.assertEquals("", passwordField.getValue());
        Assertions.assertEquals("",
                passwordField.getElement().getProperty("value"));
    }

    @Test
    void createElementWithValue_createComponentInstanceFromElement_valuePropertyMatchesValue() {
        Element element = new Element("vaadin-password-field");
        element.setProperty("value", "test");

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(ui.getService().getInstantiator())
                .thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(PasswordField.class))
                .thenAnswer(invocation -> new PasswordField());

        PasswordField passwordField = Component.from(element,
                PasswordField.class);
        Assertions.assertEquals("test",
                passwordField.getElement().getProperty("value"));
    }

    @Test
    void clearButtonVisiblePropertyValue() {
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
    void autoselectPropertyValue() {
        PasswordField passwordField = new PasswordField();

        assertAutoselectPropertyValueEquals(passwordField, true);
        assertAutoselectPropertyValueEquals(passwordField, false);
    }

    @Test
    void elementHasValue_wrapIntoTextField_propertyIsNotSetToInitialValue() {
        ComponentFromTest
                .elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue(
                        "foo", PasswordField.class, ui);
    }

    public void assertAutoselectPropertyValueEquals(PasswordField passwordField,
            Boolean value) {
        passwordField.setAutoselect(value);
        assertEquals(value, passwordField.isAutoselect());
        assertEquals(passwordField.isAutoselect(),
                passwordField.getElement().getProperty("autoselect", value));
    }

    @Test
    void addThemeVariant_themeAttributeContainsThemeVariant() {
        PasswordField field = new PasswordField();
        field.addThemeVariants(TextFieldVariant.SMALL);

        ThemeList themeNames = field.getThemeNames();
        Assertions.assertTrue(
                themeNames.contains(TextFieldVariant.SMALL.getVariantName()));
    }

    @Test
    void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        PasswordField field = new PasswordField();
        field.addThemeVariants(TextFieldVariant.SMALL);
        field.removeThemeVariants(TextFieldVariant.SMALL);

        ThemeList themeNames = field.getThemeNames();
        Assertions.assertFalse(
                themeNames.contains(TextFieldVariant.SMALL.getVariantName()));
    }

    @Test
    void implementsHasAllowedCharPattern() {
        assertTrue(
                HasAllowedCharPattern.class
                        .isAssignableFrom(new PasswordField().getClass()),
                "PasswordField should support char pattern");
    }

    @Test
    void implementHasAriaLabel() {
        PasswordField field = new PasswordField();
        Assertions.assertTrue(field instanceof HasAriaLabel);
    }

    @Test
    void setAriaLabel() {
        PasswordField field = new PasswordField();

        field.setAriaLabel("aria-label");
        Assertions.assertTrue(field.getAriaLabel().isPresent());
        Assertions.assertEquals("aria-label", field.getAriaLabel().get());

        field.setAriaLabel(null);
        Assertions.assertTrue(field.getAriaLabel().isEmpty());
    }

    @Test
    void setAriaLabelledBy() {
        PasswordField field = new PasswordField();

        field.setAriaLabelledBy("aria-labelledby");
        Assertions.assertTrue(field.getAriaLabelledBy().isPresent());
        Assertions.assertEquals("aria-labelledby",
                field.getAriaLabelledBy().get());

        field.setAriaLabelledBy(null);
        Assertions.assertTrue(field.getAriaLabelledBy().isEmpty());
    }

    @Test
    void implementsInputField() {
        PasswordField field = new PasswordField();
        Assertions.assertTrue(
                field instanceof InputField<AbstractField.ComponentValueChangeEvent<PasswordField, String>, String>);
    }
}
