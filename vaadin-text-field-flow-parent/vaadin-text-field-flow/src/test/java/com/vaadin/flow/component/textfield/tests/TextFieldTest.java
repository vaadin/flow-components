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
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.tests.MockUIExtension;

/**
 * Tests for the {@link TextField}.
 */
class TextFieldTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void setValueNull() {
        TextField textField = new TextField();
        assertEquals("", textField.getValue(),
                "Value should be an empty string");

        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class, () -> textField.setValue(null));
        Assertions.assertTrue(
                exception.getMessage().contains("Null value is not supported"));
    }

    @Test
    void initialValueIsNotSpecified_valuePropertyHasEmptyString() {
        TextField textField = new TextField();
        Assertions.assertEquals("", textField.getValue());
        Assertions.assertEquals("",
                textField.getElement().getProperty("value"));
    }

    @Test
    void initialValueIsNull_valuePropertyHasEmptyString() {
        TextField textField = new TextField((String) null);
        Assertions.assertEquals("", textField.getValue());
        Assertions.assertEquals("",
                textField.getElement().getProperty("value"));
    }

    @Test
    void createElementWithValue_createComponentInstanceFromElement_valuePropertyMatchesValue() {
        Element element = new Element("vaadin-text-field");
        element.setProperty("value", "test");

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(ui.getService().getInstantiator())
                .thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(TextField.class))
                .thenAnswer(invocation -> new TextField());

        TextField textField = Component.from(element, TextField.class);
        Assertions.assertEquals("test",
                textField.getElement().getProperty("value"));
    }

    @Test
    void clearButtonVisiblePropertyValue() {
        TextField textField = new TextField();

        assertClearButtonPropertyValueEquals(textField, true);
        assertClearButtonPropertyValueEquals(textField, false);
    }

    public void assertClearButtonPropertyValueEquals(TextField textField,
            Boolean value) {
        textField.setClearButtonVisible(value);
        assertEquals(value, textField.isClearButtonVisible());
        assertEquals(textField.isClearButtonVisible(), textField.getElement()
                .getProperty("clearButtonVisible", value));
    }

    @Test
    void autoselectPropertyValue() {
        TextField textField = new TextField();

        assertAutoselectPropertyValueEquals(textField, true);
        assertAutoselectPropertyValueEquals(textField, false);
    }

    @Test
    void elementHasValue_wrapIntoTextField_propertyIsNotSetToInitialValue() {
        ComponentFromTest
                .elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue(
                        "foo", TextField.class, ui);
    }

    @Test
    void addThemeVariant_themeAttributeContainsThemeVariant() {
        TextField field = new TextField();
        field.addThemeVariants(TextFieldVariant.SMALL);

        ThemeList themeNames = field.getThemeNames();
        Assertions.assertTrue(
                themeNames.contains(TextFieldVariant.SMALL.getVariantName()));
    }

    @Test
    void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        TextField field = new TextField();
        field.addThemeVariants(TextFieldVariant.SMALL);
        field.removeThemeVariants(TextFieldVariant.SMALL);

        ThemeList themeNames = field.getThemeNames();
        Assertions.assertFalse(
                themeNames.contains(TextFieldVariant.SMALL.getVariantName()));
    }

    public void assertAutoselectPropertyValueEquals(TextField textField,
            Boolean value) {
        textField.setAutoselect(value);
        assertEquals(value, textField.isAutoselect());
        assertEquals(textField.isAutoselect(),
                textField.getElement().getProperty("autoselect", value));
    }

    @Test
    void implementsHasAllowedCharPattern() {
        assertTrue(
                HasAllowedCharPattern.class
                        .isAssignableFrom(new TextField().getClass()),
                "TextField should support char pattern");
    }

    @Test
    void implementsHasTooltip() {
        TextField field = new TextField();
        Assertions.assertTrue(field instanceof HasTooltip);
    }

    @Test
    void implementHasAriaLabel() {
        TextField field = new TextField();
        Assertions.assertTrue(field instanceof HasAriaLabel);
    }

    @Test
    void setAriaLabel() {
        TextField field = new TextField();

        field.setAriaLabel("aria-label");
        Assertions.assertTrue(field.getAriaLabel().isPresent());
        Assertions.assertEquals("aria-label", field.getAriaLabel().get());

        field.setAriaLabel(null);
        Assertions.assertTrue(field.getAriaLabel().isEmpty());
    }

    @Test
    void setAriaLabelledBy() {
        TextField field = new TextField();

        field.setAriaLabelledBy("aria-labelledby");
        Assertions.assertTrue(field.getAriaLabelledBy().isPresent());
        Assertions.assertEquals("aria-labelledby",
                field.getAriaLabelledBy().get());

        field.setAriaLabelledBy(null);
        Assertions.assertTrue(field.getAriaLabelledBy().isEmpty());
    }

    @Test
    void implementsInputField() {
        TextField field = new TextField();
        Assertions.assertTrue(
                field instanceof InputField<AbstractField.ComponentValueChangeEvent<TextField, String>, String>);
    }

    @Test
    void setI18n_getI18n() {
        TextField textField = new TextField();
        TextField.TextFieldI18n i18n = new TextField.TextFieldI18n()
                .setRequiredErrorMessage("Required error")
                .setMinLengthErrorMessage("Min length error")
                .setMaxLengthErrorMessage("Max length error")
                .setPatternErrorMessage("Pattern error");
        textField.setI18n(i18n);
        Assertions.assertEquals(i18n, textField.getI18n());
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(TextField.class));
    }
}
