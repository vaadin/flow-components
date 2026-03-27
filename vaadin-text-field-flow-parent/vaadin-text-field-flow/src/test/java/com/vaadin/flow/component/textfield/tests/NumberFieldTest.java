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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.shared.HasAllowedCharPattern;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.tests.MockUIExtension;

/**
 * Tests for the {@link NumberField}.
 */
class NumberFieldTest extends TextFieldTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private NumberField field;

    @BeforeEach
    void setup() {
        field = new NumberField();
    }

    @Override
    @Test
    void setValueNull() {
        assertNull(field.getValue(), "Value should be null");
        field.setValue(null);
    }

    @Override
    @Test
    void initialValueIsNotSpecified_valuePropertyHasEmptyString() {
        NumberField numberField = new NumberField();
        Assertions.assertNull(numberField.getValue());
        Assertions.assertEquals("",
                numberField.getElement().getProperty("value"));
    }

    @Override
    @Test
    void initialValueIsNull_valuePropertyHasEmptyString() {
    }

    @Override
    @Test
    void createElementWithValue_createComponentInstanceFromElement_valuePropertyMatchesValue() {
        Element element = new Element("vaadin-number-field");
        element.setProperty("value", "1");

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(ui.getService().getInstantiator())
                .thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(NumberField.class))
                .thenAnswer(invocation -> new NumberField());

        NumberField numberField = Component.from(element, NumberField.class);
        Assertions.assertEquals("1",
                numberField.getElement().getProperty("value"));
    }

    @Test
    void assertStepIsNotNegative() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> field.setStep(-1));
    }

    @Test
    void assertStepGreaterThanZero() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> field.setStep(0));
    }

    @Test
    void assertDefaultValuesForMinMaxStep() {
        Assertions.assertEquals(Double.POSITIVE_INFINITY, field.getMax(), 0,
                "The default max of NumberField should be the largest possible double value");
        Assertions.assertEquals(Double.NEGATIVE_INFINITY, field.getMin(), 0,
                "The default min of NumberField should be the smallest possible double value");
        Assertions.assertEquals(1.0, field.getStep(), 0,
                "The default step of NumberField should be 1.0");
    }

    @Test
    void setInitialMinMaxRequired_shouldNotInvalidateField() {
        field.setRequiredIndicatorVisible(true);
        field.setMin(3);
        Assertions.assertFalse(field.isInvalid());
        field.setMin(-5);
        field.setMax(-1);
        Assertions.assertFalse(field.isInvalid());
    }

    @Test
    void assertMinValidation() {
        field.setValue(-10.5);
        Assertions.assertFalse(field.isInvalid());

        field.setMin(-10.3);
        field.setValue(-10.4); // need to update value to run validation
        Assertions.assertTrue(field.isInvalid());

        field.setValue(-10.3);
        Assertions.assertFalse(field.isInvalid());
    }

    @Test
    void assertMaxValidation() {
        field.setValue(100.0);
        Assertions.assertFalse(field.isInvalid());

        field.setMax(99.999);
        field.setValue(99.9991); // need to update value to run validation
        Assertions.assertTrue(field.isInvalid());

        field.setValue(99.999);
        Assertions.assertFalse(field.isInvalid());
    }

    @Test
    void stepValidation_doesntValidateWhenPropertyNotExplicitlySet() {
        Assertions.assertEquals(1.0, field.getStep(), 0.0);

        field.setValue(0.3);
        Assertions.assertFalse(field.isInvalid());

        field.setMin(0.0);
        field.setMax(10.0);
        field.setValue(0.4);
        Assertions.assertFalse(field.isInvalid());
    }

    @Test
    void stepValidation_minNotDefined() {
        field.setStep(1.5);

        assertValidValues(-6.0, -1.5, 0.0, 1.5, 4.5);
        assertInvalidValues(-3.5, -1.0, 2.0, 2.5);
    }

    @Test
    void stepValidation_positiveMin_minUsedAsStepBasis() {
        field.setMin(1.0);
        field.setStep(1.5);

        assertValidValues(1.0, 2.5, 4.0, 5.5);
        assertInvalidValues(1.5, 2.0, 3.5, 6.0);
    }

    @Test
    void stepValidation_negativeMin_minUsedAsStepBasis() {
        field.setMin(-5.0);
        field.setStep(4.5);

        assertValidValues(-5.0, -0.5, 4.0);
        assertInvalidValues(-4.5, 0.0, 1.0, 4.5);
    }

    @Test
    void setMinNegativeInfinity_doesNotThrow() {
        field.setStep(1.0);
        field.setMin(Double.NEGATIVE_INFINITY);
        field.setValue(6.0);
    }

    @Override
    @Test
    void elementHasValue_wrapIntoTextField_propertyIsNotSetToInitialValue() {
        ComponentFromTest
                .elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue(
                        1.1d, NumberField.class, ui);
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

    private void assertValidValues(Double... values) {
        Arrays.asList(values).forEach(v -> {
            field.setValue(v);
            Assertions.assertFalse(field.isInvalid(),
                    "Expected field to be valid with value " + v);
        });
    }

    private void assertInvalidValues(Double... values) {
        Arrays.asList(values).forEach(v -> {
            field.setValue(v);
            Assertions.assertTrue(field.isInvalid(),
                    "Expected field to be invalid with value " + v);
        });
    }

    public void setValue_valuePropertyFormatted() {
        testValuePropertyFormatting(1.0d, "1");
        testValuePropertyFormatting(2.0d, "2");
        testValuePropertyFormatting(5.0d, "5");
        testValuePropertyFormatting(9.0d, "9");
        testValuePropertyFormatting(0.3d, "0.3");
        testValuePropertyFormatting(0.5d, "0.5");
        testValuePropertyFormatting(0.7d, "0.7");
        testValuePropertyFormatting(21.4d, "21.4");
        testValuePropertyFormatting(123456789.01d, "123456789.01");
        testValuePropertyFormatting(-1.050d, "-1.05");
    }

    private void testValuePropertyFormatting(double value, String expected) {
        final NumberField numberField = new NumberField();
        numberField.setValue(value);
        assertEquals(expected, numberField.getElement().getProperty("value"));
    }

    @Test
    void implementsHasAllowedCharPattern() {
        Assertions.assertTrue(
                HasAllowedCharPattern.class
                        .isAssignableFrom(new NumberField().getClass()),
                "NumberField should support char pattern");
    }

    @Test
    void implementHasAriaLabel() {
        NumberField field = new NumberField();
        Assertions.assertTrue(field instanceof HasAriaLabel);
    }

    @Test
    void setAriaLabel() {
        NumberField field = new NumberField();

        field.setAriaLabel("aria-label");
        Assertions.assertTrue(field.getAriaLabel().isPresent());
        Assertions.assertEquals("aria-label", field.getAriaLabel().get());

        field.setAriaLabel(null);
        Assertions.assertTrue(field.getAriaLabel().isEmpty());
    }

    @Test
    void setAriaLabelledBy() {
        NumberField field = new NumberField();

        field.setAriaLabelledBy("aria-labelledby");
        Assertions.assertTrue(field.getAriaLabelledBy().isPresent());
        Assertions.assertEquals("aria-labelledby",
                field.getAriaLabelledBy().get());

        field.setAriaLabelledBy(null);
        Assertions.assertTrue(field.getAriaLabelledBy().isEmpty());
    }

    @Test
    void setI18n_getI18n() {
        NumberField textField = new NumberField();
        NumberField.NumberFieldI18n i18n = new NumberField.NumberFieldI18n()
                .setBadInputErrorMessage("Bad input error")
                .setRequiredErrorMessage("Required error")
                .setMinErrorMessage("Min error").setMaxErrorMessage("Max error")
                .setStepErrorMessage("Step error");
        textField.setI18n(i18n);
        Assertions.assertEquals(i18n, textField.getI18n());
    }
}
