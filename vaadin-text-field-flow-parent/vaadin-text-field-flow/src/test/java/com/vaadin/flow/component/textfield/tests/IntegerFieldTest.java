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

import java.util.Arrays;

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
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.tests.MockUIExtension;

class IntegerFieldTest extends TextFieldTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private IntegerField field;

    @BeforeEach
    void setup() {
        field = new IntegerField();
    }

    @Override
    @Test
    void setValueNull() {
        assertNull(field.getValue(), "Value should be null");
        field.setValue(null);
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

    @Override
    @Test
    void initialValueIsNotSpecified_valuePropertyHasEmptyString() {
        IntegerField integerField = new IntegerField();
        Assertions.assertNull(integerField.getValue());
        Assertions.assertEquals("",
                integerField.getElement().getProperty("value"));
    }

    @Override
    @Test
    void initialValueIsNull_valuePropertyHasEmptyString() {
    }

    @Override
    @Test
    void createElementWithValue_createComponentInstanceFromElement_valuePropertyMatchesValue() {
        Element element = new Element("vaadin-integer-field");
        element.setProperty("value", "1");

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(ui.getService().getInstantiator())
                .thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(IntegerField.class))
                .thenAnswer(invocation -> new IntegerField());

        IntegerField integerField = Component.from(element, IntegerField.class);
        Assertions.assertEquals("1",
                integerField.getElement().getProperty("value"));
    }

    @Test
    void assertDefaultValuesForMinMaxStep() {
        Assertions.assertEquals(2147483647, field.getMax(),
                "The default max of IntegerField should be the largest possible int value");
        Assertions.assertEquals(-2147483648, field.getMin(),
                "The default min of IntegerField should be the smallest possible int value");
        Assertions.assertEquals(1, field.getStep(),
                "The default step of IntegerField should be 1");
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
        field.setValue(-10);
        Assertions.assertFalse(field.isInvalid());

        field.setMin(-8);
        field.setValue(-9); // need to update value to run validation
        Assertions.assertTrue(field.isInvalid());

        field.setValue(-8);
        Assertions.assertFalse(field.isInvalid());
    }

    @Test
    void assertMaxValidation() {
        field.setValue(100);
        Assertions.assertFalse(field.isInvalid());

        field.setMax(98);
        field.setValue(99); // need to update value to run validation
        Assertions.assertTrue(field.isInvalid());

        field.setValue(98);
        Assertions.assertFalse(field.isInvalid());
    }

    @Test
    void stepValidation_minNotDefined() {
        field.setStep(3);

        assertValidValues(-9, -6, -3, 0, 3, 6, 30);
        assertInvalidValues(-10, -1, 2, 32);
    }

    @Test
    void stepValidation_positiveMin_minUsedAsStepBasis() {
        field.setMin(1);
        field.setStep(3);

        assertValidValues(1, 4, 7);
        assertInvalidValues(2, 3, 5, 6);
    }

    @Test
    void stepValidation_negativeMin_minUsedAsStepBasis() {
        field.setMin(-5);
        field.setStep(4);

        assertValidValues(-5, -1, 3, 7);
        assertInvalidValues(0, 4, -4);
    }

    @Override
    @Test
    void elementHasValue_wrapIntoTextField_propertyIsNotSetToInitialValue() {
        ComponentFromTest
                .elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue(1,
                        IntegerField.class, ui);
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

    private void assertValidValues(Integer... values) {
        Arrays.asList(values).forEach(v -> {
            field.setValue(v);
            Assertions.assertFalse(field.isInvalid(),
                    "Expected field to be valid with value " + v);
        });
    }

    private void assertInvalidValues(Integer... values) {
        Arrays.asList(values).forEach(v -> {
            field.setValue(v);
            Assertions.assertTrue(field.isInvalid(),
                    "Expected field to be invalid with value " + v);
        });
    }

    @Test
    void implementHasAriaLabel() {
        IntegerField field = new IntegerField();
        Assertions.assertTrue(field instanceof HasAriaLabel);
    }

    @Test
    void setAriaLabel() {
        IntegerField field = new IntegerField();

        field.setAriaLabel("aria-label");
        Assertions.assertTrue(field.getAriaLabel().isPresent());
        Assertions.assertEquals("aria-label", field.getAriaLabel().get());

        field.setAriaLabel(null);
        Assertions.assertTrue(field.getAriaLabel().isEmpty());
    }

    @Test
    void setAriaLabelledBy() {
        IntegerField field = new IntegerField();

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
        IntegerField field = new IntegerField();
        Assertions.assertTrue(
                field instanceof InputField<AbstractField.ComponentValueChangeEvent<IntegerField, Integer>, Integer>);
    }
}
