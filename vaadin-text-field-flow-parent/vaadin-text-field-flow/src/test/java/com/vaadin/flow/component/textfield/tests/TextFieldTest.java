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
import com.vaadin.flow.component.textfield.TextField;
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

import java.time.LocalDate;

/**
 * Tests for the {@link TextField}.
 */
public class TextFieldTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void setValueNull() {
        TextField textField = new TextField();
        assertEquals("Value should be an empty string", "",
                textField.getValue());

        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Null value is not supported");

        textField.setValue(null);
    }

    @Test
    public void initialValueIsNotSpecified_valuePropertyHasEmptyString() {
        TextField textField = new TextField();
        Assert.assertEquals("", textField.getValue());
        Assert.assertEquals("", textField.getElement().getProperty("value"));
    }

    @Test
    public void initialValueIsNull_valuePropertyHasEmptyString() {
        TextField textField = new TextField((String) null);
        Assert.assertEquals("", textField.getValue());
        Assert.assertEquals("", textField.getElement().getProperty("value"));
    }

    @Test
    public void createElementWithValue_createComponentInstanceFromElement_valuePropertyMatchesValue() {
        Element element = new Element("vaadin-text-field");
        element.setProperty("value", "test");
        UI ui = new UI();
        UI.setCurrent(ui);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        ui.getInternals().setSession(session);
        VaadinService service = Mockito.mock(VaadinService.class);
        Mockito.when(session.getService()).thenReturn(service);

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(service.getInstantiator()).thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(TextField.class))
                .thenAnswer(invocation -> new TextField());

        TextField textField = Component.from(element, TextField.class);
        Assert.assertEquals("test",
                textField.getElement().getProperty("value"));
    }

    @Test
    public void clearButtonVisiblePropertyValue() {
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
    public void autoselectPropertyValue() {
        TextField textField = new TextField();

        assertAutoselectPropertyValueEquals(textField, true);
        assertAutoselectPropertyValueEquals(textField, false);
    }

    @Test
    public void elementHasValue_wrapIntoTextField_propertyIsNotSetToInitialValue() {
        ComponentFromTest
                .elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue(
                        "foo", TextField.class);
    }

    @Test
    public void addThemeVariant_themeAttributeContainsThemeVariant() {
        TextField field = new TextField();
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);

        ThemeList themeNames = field.getThemeNames();
        Assert.assertTrue(themeNames
                .contains(TextFieldVariant.LUMO_SMALL.getVariantName()));
    }

    @Test
    public void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        TextField field = new TextField();
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        field.removeThemeVariants(TextFieldVariant.LUMO_SMALL);

        ThemeList themeNames = field.getThemeNames();
        Assert.assertFalse(themeNames
                .contains(TextFieldVariant.LUMO_SMALL.getVariantName()));
    }

    public void assertAutoselectPropertyValueEquals(TextField textField,
            Boolean value) {
        textField.setAutoselect(value);
        assertEquals(value, textField.isAutoselect());
        assertEquals(textField.isAutoselect(),
                textField.getElement().getProperty("autoselect", value));
    }

    @Test
    public void implementsHasAllowedCharPattern() {
        assertTrue("TextField should support char pattern",
                HasAllowedCharPattern.class
                        .isAssignableFrom(new TextField().getClass()));
    }

    @Test
    public void implementsHasTooltip() {
        TextField field = new TextField();
        Assert.assertTrue(field instanceof HasTooltip);
    }

    @Test
    public void implementHasAriaLabel() {
        TextField field = new TextField();
        Assert.assertTrue(field instanceof HasAriaLabel);
    }

    @Test
    public void setAriaLabel() {
        TextField field = new TextField();

        field.setAriaLabel("aria-label");
        Assert.assertTrue(field.getAriaLabel().isPresent());
        Assert.assertEquals("aria-label", field.getAriaLabel().get());

        field.setAriaLabel(null);
        Assert.assertTrue(field.getAriaLabel().isEmpty());
    }

    @Test
    public void setAriaLabelledBy() {
        TextField field = new TextField();

        field.setAriaLabelledBy("aria-labelledby");
        Assert.assertTrue(field.getAriaLabelledBy().isPresent());
        Assert.assertEquals("aria-labelledby", field.getAriaLabelledBy().get());

        field.setAriaLabelledBy(null);
        Assert.assertTrue(field.getAriaLabelledBy().isEmpty());
    }

    @Test
    public void implementsInputField() {
        TextField field = new TextField();
        Assert.assertTrue(
                field instanceof InputField<AbstractField.ComponentValueChangeEvent<TextField, String>, String>);
    }

    @Test
    public void setI18n_getI18n() {
        TextField textField = new TextField();
        TextField.TextFieldI18n i18n = new TextField.TextFieldI18n()
                .setRequiredErrorMessage("Required error")
                .setMinLengthErrorMessage("Min length error")
                .setMaxLengthErrorMessage("Max length error")
                .setPatternErrorMessage("Pattern error");
        textField.setI18n(i18n);
        Assert.assertEquals(i18n, textField.getI18n());
    }
}
