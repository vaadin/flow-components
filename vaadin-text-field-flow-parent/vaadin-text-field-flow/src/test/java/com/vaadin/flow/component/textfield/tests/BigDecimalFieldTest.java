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

import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.tests.MockUIExtension;

class BigDecimalFieldTest extends TextFieldTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private BigDecimalField field;

    @BeforeEach
    void setup() {
        field = new BigDecimalField();
        field.setLocale(Locale.US);
    }

    @Override
    @Test
    void setValueNull() {
        assertNull(field.getValue(), "Value should be null");
        field.setValue(new BigDecimal("1"));
        field.setValue(null); // not throwing
    }

    @Override
    @Test
    void initialValueIsNotSpecified_valuePropertyHasEmptyString() {
        BigDecimalField bigDecimalField = new BigDecimalField();
        Assertions.assertNull(bigDecimalField.getValue());
        Assertions.assertEquals("",
                bigDecimalField.getElement().getProperty("value"));
    }

    @Override
    @Test
    void initialValueIsNull_valuePropertyHasEmptyString() {
    }

    @Override
    @Test
    void createElementWithValue_createComponentInstanceFromElement_valuePropertyMatchesValue() {
        Element element = new Element("vaadin-big-decimal-field");
        element.setProperty("value", "1");

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(ui.getService().getInstantiator())
                .thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(BigDecimalField.class))
                .thenAnswer(invocation -> new BigDecimalField());

        BigDecimalField bigDecimalField = Component.from(element,
                BigDecimalField.class);
        Assertions.assertEquals("1",
                bigDecimalField.getElement().getProperty("value"));
    }

    @Test
    void valueFormatting_scientificNotationRemoved() {
        assertValueFormatting(new BigDecimal("1e9"), "1000000000");
        assertValueFormatting(new BigDecimal("-1e9"), "-1000000000");

        assertValueFormatting(new BigDecimal("1e-9"), "0.000000001");
        assertValueFormatting(new BigDecimal("-1e-9"), "-0.000000001");
    }

    @Test
    void valueFormatting_trailingZerosPreserved_leadingZerosRemoved() {
        assertValueFormatting(new BigDecimal("001.100"), "1.100");
    }

    @Test
    void valueFormatting_scalePreserved() {
        assertValueFormatting(new BigDecimal("1.10").setScale(4), "1.1000");
        assertValueFormatting(new BigDecimal("1.10").setScale(1), "1.1");
    }

    @Test
    void valueFormatting_scaleWithRounding() {
        assertValueFormatting(
                new BigDecimal("1.01").setScale(1, BigDecimal.ROUND_CEILING),
                "1.1");
        assertValueFormatting(
                new BigDecimal("1.01").setScale(1, BigDecimal.ROUND_FLOOR),
                "1.0");
    }

    @Override
    @Test
    void elementHasValue_wrapIntoTextField_propertyIsNotSetToInitialValue() {
        ComponentFromTest
                .elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue(
                        BigDecimal.TEN.toString(), BigDecimalField.class, ui);
    }

    @Test
    void addThemeVariant_themeAttributeContainsThemeVariant() {
        field.addThemeVariants(TextFieldVariant.SMALL);

        ThemeList themeNames = field.getThemeNames();
        Assertions.assertTrue(
                themeNames.contains(TextFieldVariant.SMALL.getVariantName()));
    }

    @Test
    void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        field.addThemeVariants(TextFieldVariant.SMALL);
        field.removeThemeVariants(TextFieldVariant.SMALL);

        ThemeList themeNames = field.getThemeNames();
        Assertions.assertFalse(
                themeNames.contains(TextFieldVariant.SMALL.getVariantName()));
    }

    @Test
    void implementsHasTooltip() {
        Assertions.assertTrue(field instanceof HasTooltip);
    }

    private void assertValueFormatting(BigDecimal bigDecimal,
            String expectedValueProp) {
        field.setValue(bigDecimal);
        Assertions.assertEquals(expectedValueProp,
                field.getElement().getProperty("value"));
    }

    @Test
    void implementHasAriaLabel() {
        BigDecimalField field = new BigDecimalField();
        Assertions.assertTrue(field instanceof HasAriaLabel);
    }

    @Test
    void setAriaLabel() {
        BigDecimalField field = new BigDecimalField();

        field.setAriaLabel("aria-label");
        Assertions.assertTrue(field.getAriaLabel().isPresent());
        Assertions.assertEquals("aria-label", field.getAriaLabel().get());

        field.setAriaLabel(null);
        Assertions.assertTrue(field.getAriaLabel().isEmpty());
    }

    @Test
    void setAriaLabelledBy() {
        BigDecimalField field = new BigDecimalField();

        field.setAriaLabelledBy("aria-labelledby");
        Assertions.assertTrue(field.getAriaLabelledBy().isPresent());
        Assertions.assertEquals("aria-labelledby",
                field.getAriaLabelledBy().get());

        field.setAriaLabelledBy(null);
        Assertions.assertTrue(field.getAriaLabelledBy().isEmpty());
    }

    @Test
    @Override
    void implementsInputField() {
        BigDecimalField field = new BigDecimalField();
        Assertions.assertTrue(
                field instanceof InputField<AbstractField.ComponentValueChangeEvent<BigDecimalField, BigDecimal>, BigDecimal>);
    }
}
