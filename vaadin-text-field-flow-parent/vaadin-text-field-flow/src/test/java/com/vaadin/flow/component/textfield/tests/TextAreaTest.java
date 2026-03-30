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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextAreaVariant;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.tests.MockUIExtension;

/**
 * Tests for the {@link TextArea}.
 */
class TextAreaTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void setValueNull() {
        TextArea textArea = new TextArea();
        assertEquals("", textArea.getValue(),
                "Value should be an empty string");

        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class, () -> textArea.setValue(null));
        Assertions.assertTrue(
                exception.getMessage().contains("Null value is not supported"));
    }

    @Test
    void initialValueIsNotSpecified_valuePropertyHasEmptyString() {
        TextArea textArea = new TextArea();
        Assertions.assertEquals("", textArea.getValue());
        Assertions.assertEquals("", textArea.getElement().getProperty("value"));
    }

    @Test
    void initialValueIsNull_valuePropertyHasEmptyString() {
        TextArea textArea = new TextArea((String) null);
        Assertions.assertEquals("", textArea.getValue());
        Assertions.assertEquals("", textArea.getElement().getProperty("value"));
    }

    @Test
    void createElementWithValue_createComponentInstanceFromElement_valuePropertyMatchesValue() {
        Element element = new Element("vaadin-text-area");
        element.setProperty("value", "test");

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(ui.getService().getInstantiator())
                .thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(TextArea.class))
                .thenAnswer(invocation -> new TextArea());

        TextArea textArea = Component.from(element, TextArea.class);
        Assertions.assertEquals("test",
                textArea.getElement().getProperty("value"));
    }

    @Test
    void clearButtonVisiblePropertyValue() {
        TextArea textArea = new TextArea();

        assertClearButtonPropertyValueEquals(textArea, true);
        assertClearButtonPropertyValueEquals(textArea, false);
    }

    public void assertClearButtonPropertyValueEquals(TextArea textArea,
            Boolean value) {
        textArea.setClearButtonVisible(value);
        assertEquals(value, textArea.isClearButtonVisible());
        assertEquals(textArea.isClearButtonVisible(),
                textArea.getElement().getProperty("clearButtonVisible", value));
    }

    @Test
    void autoselectPropertyValue() {
        TextArea textArea = new TextArea();

        assertAutoselectPropertyValueEquals(textArea, true);
        assertAutoselectPropertyValueEquals(textArea, false);
    }

    @Test
    void elementHasValue_wrapIntoTextField_propertyIsNotSetToInitialValue() {
        ComponentFromTest
                .elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue(
                        "foo", TextArea.class, ui);
    }

    @Test
    void patternPropertyValue() {
        String testPattern = "TEST";
        TextArea textArea = new TextArea();

        textArea.setPattern(testPattern);
        assertEquals(testPattern, textArea.getPattern());
        assertEquals(testPattern,
                textArea.getElement().getProperty("pattern", ""));
    }

    public void assertAutoselectPropertyValueEquals(TextArea textArea,
            Boolean value) {
        textArea.setAutoselect(value);
        assertEquals(value, textArea.isAutoselect());
        assertEquals(textArea.isAutoselect(),
                textArea.getElement().getProperty("autoselect", value));
    }

    @Test
    void addThemeVariant_themeAttributeContainsThemeVariant() {
        TextArea textArea = new TextArea();
        textArea.addThemeVariants(TextAreaVariant.SMALL);

        ThemeList themeNames = textArea.getThemeNames();
        Assertions.assertTrue(
                themeNames.contains(TextAreaVariant.SMALL.getVariantName()));
    }

    @Test
    void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        TextArea textArea = new TextArea();
        textArea.addThemeVariants(TextAreaVariant.SMALL);
        textArea.removeThemeVariants(TextAreaVariant.SMALL);

        ThemeList themeNames = textArea.getThemeNames();
        Assertions.assertFalse(
                themeNames.contains(TextAreaVariant.SMALL.getVariantName()));
    }

    @Test
    void implementsHasAllowedCharPattern() {
        assertTrue(
                HasAllowedCharPattern.class
                        .isAssignableFrom(new TextArea().getClass()),
                "TextArea should support char pattern");
    }

    @Test
    void implementsHasTooltip() {
        TextArea textArea = new TextArea();
        Assertions.assertTrue(textArea instanceof HasTooltip);
    }

    @Test
    void implementHasAriaLabel() {
        TextArea field = new TextArea();
        Assertions.assertTrue(field instanceof HasAriaLabel);
    }

    @Test
    void setAriaLabel() {
        TextArea field = new TextArea();

        field.setAriaLabel("aria-label");
        Assertions.assertTrue(field.getAriaLabel().isPresent());
        Assertions.assertEquals("aria-label", field.getAriaLabel().get());

        field.setAriaLabel(null);
        Assertions.assertTrue(field.getAriaLabel().isEmpty());
    }

    @Test
    void setAriaLabelledBy() {
        TextArea field = new TextArea();

        field.setAriaLabelledBy("aria-labelledby");
        Assertions.assertTrue(field.getAriaLabelledBy().isPresent());
        Assertions.assertEquals("aria-labelledby",
                field.getAriaLabelledBy().get());

        field.setAriaLabelledBy(null);
        Assertions.assertTrue(field.getAriaLabelledBy().isEmpty());
    }

    @Test
    void implementsInputField() {
        TextArea field = new TextArea();
        Assertions.assertTrue(
                field instanceof InputField<AbstractField.ComponentValueChangeEvent<TextArea, String>, String>);
    }

    @Test
    void getMinRows_defaultValue() {
        TextArea field = new TextArea();

        Assertions.assertEquals(2, field.getMinRows());
    }

    @Test
    void setMinRows() {
        TextArea field = new TextArea();
        field.setMinRows(5);

        Assertions.assertEquals(5, field.getMinRows());
        Assertions.assertEquals(5,
                field.getElement().getProperty("minRows", 0));
    }

    @Test
    void getMaxRows_defaultValue() {
        TextArea field = new TextArea();

        Assertions.assertNull(field.getMaxRows());
    }

    @Test
    void setMaxRows() {
        TextArea field = new TextArea();
        field.setMaxRows(5);

        Assertions.assertEquals(5, (int) field.getMaxRows());
        Assertions.assertEquals(5,
                field.getElement().getProperty("maxRows", 0));

        field.setMaxRows(null);

        Assertions.assertNull(field.getMaxRows());
        Assertions.assertNull(field.getElement().getProperty("maxRows"));
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(TextArea.class));
    }
}
