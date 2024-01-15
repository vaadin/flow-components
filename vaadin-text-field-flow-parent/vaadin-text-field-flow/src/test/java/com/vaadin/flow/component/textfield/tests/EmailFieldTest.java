/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.shared.HasAllowedCharPattern;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link EmailField}.
 */
public class EmailFieldTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void setValueNull() {
        EmailField emailField = new EmailField();
        assertEquals("Value should be an empty string", "",
                emailField.getValue());

        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Null value is not supported");

        emailField.setValue(null);
    }

    @Test
    public void initialValueIsNotSpecified_valuePropertyHasEmptyString() {
        EmailField emailField = new EmailField();
        Assert.assertEquals("", emailField.getValue());
        Assert.assertEquals("", emailField.getElement().getProperty("value"));
    }

    @Test
    public void initialValueIsNull_valuePropertyHasEmptyString() {
        EmailField emailField = new EmailField((String) null);
        Assert.assertEquals("", emailField.getValue());
        Assert.assertEquals("", emailField.getElement().getProperty("value"));
    }

    @Test
    public void createElementWithValue_createComponentInstanceFromElement_valuePropertyMatchesValue() {
        Element element = new Element("vaadin-email-field");
        element.setProperty("value", "foo@example.com");
        UI ui = new UI();
        UI.setCurrent(ui);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        ui.getInternals().setSession(session);
        VaadinService service = Mockito.mock(VaadinService.class);
        Mockito.when(session.getService()).thenReturn(service);

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(service.getInstantiator()).thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(EmailField.class))
                .thenAnswer(invocation -> new EmailField());

        EmailField emailField = Component.from(element, EmailField.class);
        Assert.assertEquals("foo@example.com",
                emailField.getElement().getProperty("value"));
    }

    @Test
    public void elementHasValue_wrapIntoTextField_propertyIsNotSetToInitialValue() {
        ComponentFromTest
                .elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue(
                        "foo@example.com", EmailField.class);
    }

    @Test
    public void addThemeVariant_themeAttributeContainsThemeVariant() {
        EmailField field = new EmailField();
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);

        ThemeList themeNames = field.getThemeNames();
        Assert.assertTrue(themeNames
                .contains(TextFieldVariant.LUMO_SMALL.getVariantName()));
    }

    @Test
    public void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        EmailField field = new EmailField();
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        field.removeThemeVariants(TextFieldVariant.LUMO_SMALL);

        ThemeList themeNames = field.getThemeNames();
        Assert.assertFalse(themeNames
                .contains(TextFieldVariant.LUMO_SMALL.getVariantName()));
    }

    @Test
    public void implementsHasAllowedCharPattern() {
        assertTrue("EmailField should support char pattern",
                HasAllowedCharPattern.class
                        .isAssignableFrom(new EmailField().getClass()));
    }

    @Test
    public void implementsHasTooltip() {
        EmailField field = new EmailField();
        Assert.assertTrue(field instanceof HasTooltip);
    }

    @Test
    public void implementHasAriaLabel() {
        EmailField field = new EmailField();
        Assert.assertTrue(field instanceof HasAriaLabel);
    }

    @Test
    public void setAriaLabel() {
        EmailField field = new EmailField();

        field.setAriaLabel("aria-label");
        Assert.assertTrue(field.getAriaLabel().isPresent());
        Assert.assertEquals("aria-label", field.getAriaLabel().get());

        field.setAriaLabel(null);
        Assert.assertTrue(field.getAriaLabel().isEmpty());
    }

    @Test
    public void setAriaLabelledBy() {
        EmailField field = new EmailField();

        field.setAriaLabelledBy("aria-labelledby");
        Assert.assertTrue(field.getAriaLabelledBy().isPresent());
        Assert.assertEquals("aria-labelledby", field.getAriaLabelledBy().get());

        field.setAriaLabelledBy(null);
        Assert.assertTrue(field.getAriaLabelledBy().isEmpty());
    }

    @Test
    public void implementsInputField() {
        EmailField field = new EmailField();
        Assert.assertTrue(
                field instanceof InputField<AbstractField.ComponentValueChangeEvent<EmailField, String>, String>);
    }
}
