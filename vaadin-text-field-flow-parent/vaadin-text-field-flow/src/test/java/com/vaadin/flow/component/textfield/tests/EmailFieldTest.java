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
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.tests.MockUIExtension;

/**
 * Tests for the {@link EmailField}.
 */
class EmailFieldTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void setValueNull() {
        EmailField emailField = new EmailField();
        assertEquals("", emailField.getValue(),
                "Value should be an empty string");

        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class, () -> emailField.setValue(null));
        Assertions.assertTrue(
                exception.getMessage().contains("Null value is not supported"));
    }

    @Test
    void initialValueIsNotSpecified_valuePropertyHasEmptyString() {
        EmailField emailField = new EmailField();
        Assertions.assertEquals("", emailField.getValue());
        Assertions.assertEquals("",
                emailField.getElement().getProperty("value"));
    }

    @Test
    void initialValueIsNull_valuePropertyHasEmptyString() {
        EmailField emailField = new EmailField((String) null);
        Assertions.assertEquals("", emailField.getValue());
        Assertions.assertEquals("",
                emailField.getElement().getProperty("value"));
    }

    @Test
    void createElementWithValue_createComponentInstanceFromElement_valuePropertyMatchesValue() {
        Element element = new Element("vaadin-email-field");
        element.setProperty("value", "foo@example.com");

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(ui.getService().getInstantiator())
                .thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(EmailField.class))
                .thenAnswer(invocation -> new EmailField());

        EmailField emailField = Component.from(element, EmailField.class);
        Assertions.assertEquals("foo@example.com",
                emailField.getElement().getProperty("value"));
    }

    @Test
    void elementHasValue_wrapIntoTextField_propertyIsNotSetToInitialValue() {
        ComponentFromTest
                .elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue(
                        "foo@example.com", EmailField.class, ui);
    }

    @Test
    void addThemeVariant_themeAttributeContainsThemeVariant() {
        EmailField field = new EmailField();
        field.addThemeVariants(TextFieldVariant.SMALL);

        ThemeList themeNames = field.getThemeNames();
        Assertions.assertTrue(
                themeNames.contains(TextFieldVariant.SMALL.getVariantName()));
    }

    @Test
    void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        EmailField field = new EmailField();
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
                        .isAssignableFrom(new EmailField().getClass()),
                "EmailField should support char pattern");
    }

    @Test
    void implementsHasTooltip() {
        EmailField field = new EmailField();
        Assertions.assertTrue(field instanceof HasTooltip);
    }

    @Test
    void implementHasAriaLabel() {
        EmailField field = new EmailField();
        Assertions.assertTrue(field instanceof HasAriaLabel);
    }

    @Test
    void setAriaLabel() {
        EmailField field = new EmailField();

        field.setAriaLabel("aria-label");
        Assertions.assertTrue(field.getAriaLabel().isPresent());
        Assertions.assertEquals("aria-label", field.getAriaLabel().get());

        field.setAriaLabel(null);
        Assertions.assertTrue(field.getAriaLabel().isEmpty());
    }

    @Test
    void setAriaLabelledBy() {
        EmailField field = new EmailField();

        field.setAriaLabelledBy("aria-labelledby");
        Assertions.assertTrue(field.getAriaLabelledBy().isPresent());
        Assertions.assertEquals("aria-labelledby",
                field.getAriaLabelledBy().get());

        field.setAriaLabelledBy(null);
        Assertions.assertTrue(field.getAriaLabelledBy().isEmpty());
    }

    @Test
    void implementsInputField() {
        EmailField field = new EmailField();
        Assertions.assertTrue(
                field instanceof InputField<AbstractField.ComponentValueChangeEvent<EmailField, String>, String>);
    }
}
