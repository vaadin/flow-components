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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.shared.HasAllowedCharPattern;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for the {@link NumberField}.
 */
public class NumberFieldTest extends TextFieldTest {

    private NumberField field;

    @Before
    public void setup() {
        field = new NumberField();
    }

    @Override
    @Test
    public void setValueNull() {
        assertNull("Value should be null", field.getValue());
        field.setValue(null);
    }

    @Override
    @Test
    public void initialValueIsNotSpecified_valuePropertyHasEmptyString() {
        NumberField numberField = new NumberField();
        Assert.assertNull(numberField.getValue());
        Assert.assertEquals("", numberField.getElement().getProperty("value"));
    }

    @Override
    @Test
    public void initialValueIsNull_valuePropertyHasEmptyString() {
    }

    @Override
    @Test
    public void createElementWithValue_createComponentInstanceFromElement_valuePropertyMatchesValue() {
        Element element = new Element("vaadin-number-field");
        element.setProperty("value", "1");
        UI ui = new UI();
        UI.setCurrent(ui);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        ui.getInternals().setSession(session);
        VaadinService service = Mockito.mock(VaadinService.class);
        Mockito.when(session.getService()).thenReturn(service);

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(service.getInstantiator()).thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(NumberField.class))
                .thenAnswer(invocation -> new NumberField());

        NumberField numberField = Component.from(element, NumberField.class);
        Assert.assertEquals("1", numberField.getElement().getProperty("value"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertStepIsNotNegative() {
        field.setStep(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertStepGreaterThanZero() {
        field.setStep(0);
    }

    @Test
    public void assertDefaultValuesForMinMaxStep() {
        Assert.assertEquals(
                "The default max of NumberField should be the largest possible double value",
                Double.POSITIVE_INFINITY, field.getMax(), 0);
        Assert.assertEquals(
                "The default min of NumberField should be the smallest possible double value",
                Double.NEGATIVE_INFINITY, field.getMin(), 0);
        Assert.assertEquals("The default step of NumberField should be 1.0",
                1.0, field.getStep(), 0);
    }

    @Test
    public void setInitialMinMaxRequired_shouldNotInvalidateField() {
        field.setRequiredIndicatorVisible(true);
        field.setMin(3);
        Assert.assertFalse(field.isInvalid());
        field.setMin(-5);
        field.setMax(-1);
        Assert.assertFalse(field.isInvalid());
    }

    @Test
    public void assertMinValidation() {
        field.setValue(-10.5);
        Assert.assertFalse(field.isInvalid());

        field.setMin(-10.3);
        field.setValue(-10.4); // need to update value to run validation
        Assert.assertTrue(field.isInvalid());

        field.setValue(-10.3);
        Assert.assertFalse(field.isInvalid());
    }

    @Test
    public void assertMaxValidation() {
        field.setValue(100.0);
        Assert.assertFalse(field.isInvalid());

        field.setMax(99.999);
        field.setValue(99.9991); // need to update value to run validation
        Assert.assertTrue(field.isInvalid());

        field.setValue(99.999);
        Assert.assertFalse(field.isInvalid());
    }

    @Test
    public void stepValidation_doesntValidateWhenPropertyNotExplicitlySet() {
        Assert.assertEquals(1.0, field.getStep(), 0.0);

        field.setValue(0.3);
        Assert.assertFalse(field.isInvalid());

        field.setMin(0.0);
        field.setMax(10.0);
        field.setValue(0.4);
        Assert.assertFalse(field.isInvalid());
    }

    @Test
    public void stepValidation_minNotDefined() {
        field.setStep(1.5);

        assertValidValues(-6.0, -1.5, 0.0, 1.5, 4.5);
        assertInvalidValues(-3.5, -1.0, 2.0, 2.5);
    }

    @Test
    public void stepValidation_positiveMin_minUsedAsStepBasis() {
        field.setMin(1.0);
        field.setStep(1.5);

        assertValidValues(1.0, 2.5, 4.0, 5.5);
        assertInvalidValues(1.5, 2.0, 3.5, 6.0);
    }

    @Test
    public void stepValidation_negativeMin_minUsedAsStepBasis() {
        field.setMin(-5.0);
        field.setStep(4.5);

        assertValidValues(-5.0, -0.5, 4.0);
        assertInvalidValues(-4.5, 0.0, 1.0, 4.5);
    }

    @Override
    @Test
    public void elementHasValue_wrapIntoTextField_propertyIsNotSetToInitialValue() {
        ComponentFromTest
                .elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue(
                        1.1d, NumberField.class);
    }

    @Test
    public void addThemeVariant_themeAttributeContainsThemeVariant() {
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);

        ThemeList themeNames = field.getThemeNames();
        Assert.assertTrue(themeNames
                .contains(TextFieldVariant.LUMO_SMALL.getVariantName()));
    }

    @Test
    public void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        field.removeThemeVariants(TextFieldVariant.LUMO_SMALL);

        ThemeList themeNames = field.getThemeNames();
        Assert.assertFalse(themeNames
                .contains(TextFieldVariant.LUMO_SMALL.getVariantName()));
    }

    @Test
    public void implementsHasTooltip() {
        Assert.assertTrue(field instanceof HasTooltip);
    }

    private void assertValidValues(Double... values) {
        Arrays.asList(values).forEach(v -> {
            field.setValue(v);
            Assert.assertFalse("Expected field to be valid with value " + v,
                    field.isInvalid());
        });
    }

    private void assertInvalidValues(Double... values) {
        Arrays.asList(values).forEach(v -> {
            field.setValue(v);
            Assert.assertTrue("Expected field to be invalid with value " + v,
                    field.isInvalid());
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
    public void implementsHasAllowedCharPattern() {
        Assert.assertTrue("NumberField should support char pattern",
                HasAllowedCharPattern.class
                        .isAssignableFrom(new NumberField().getClass()));
    }

    @Test
    public void implementHasAriaLabel() {
        NumberField field = new NumberField();
        Assert.assertTrue(field instanceof HasAriaLabel);
    }

    @Test
    public void setAriaLabel() {
        NumberField field = new NumberField();

        field.setAriaLabel("aria-label");
        Assert.assertTrue(field.getAriaLabel().isPresent());
        Assert.assertEquals("aria-label", field.getAriaLabel().get());

        field.setAriaLabel(null);
        Assert.assertTrue(field.getAriaLabel().isEmpty());
    }

    @Test
    public void setAriaLabelledBy() {
        NumberField field = new NumberField();

        field.setAriaLabelledBy("aria-labelledby");
        Assert.assertTrue(field.getAriaLabelledBy().isPresent());
        Assert.assertEquals("aria-labelledby", field.getAriaLabelledBy().get());

        field.setAriaLabelledBy(null);
        Assert.assertTrue(field.getAriaLabelledBy().isEmpty());
    }
}
