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
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.component.textfield.IntegerField;
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

import static org.junit.Assert.assertNull;

public class IntegerFieldTest extends TextFieldTest {

    private IntegerField field;

    @Before
    public void setup() {
        field = new IntegerField();
    }

    @Override
    @Test
    public void setValueNull() {
        assertNull("Value should be null", field.getValue());
        field.setValue(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertStepIsNotNegative() {
        field.setStep(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertStepGreaterThanZero() {
        field.setStep(0);
    }

    @Override
    @Test
    public void initialValueIsNotSpecified_valuePropertyHasEmptyString() {
        IntegerField integerField = new IntegerField();
        Assert.assertNull(integerField.getValue());
        Assert.assertEquals("", integerField.getElement().getProperty("value"));
    }

    @Override
    @Test
    public void initialValueIsNull_valuePropertyHasEmptyString() {
    }

    @Override
    @Test
    public void createElementWithValue_createComponentInstanceFromElement_valuePropertyMatchesValue() {
        Element element = new Element("vaadin-integer-field");
        element.setProperty("value", "1");
        UI ui = new UI();
        UI.setCurrent(ui);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        ui.getInternals().setSession(session);
        VaadinService service = Mockito.mock(VaadinService.class);
        Mockito.when(session.getService()).thenReturn(service);

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(service.getInstantiator()).thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(IntegerField.class))
                .thenAnswer(invocation -> new IntegerField());

        IntegerField integerField = Component.from(element, IntegerField.class);
        Assert.assertEquals("1",
                integerField.getElement().getProperty("value"));
    }

    @Test
    public void assertDefaultValuesForMinMaxStep() {
        Assert.assertEquals(
                "The default max of IntegerField should be the largest possible int value",
                2147483647, field.getMax());
        Assert.assertEquals(
                "The default min of IntegerField should be the smallest possible int value",
                -2147483648, field.getMin());
        Assert.assertEquals("The default step of IntegerField should be 1", 1,
                field.getStep());
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
        field.setValue(-10);
        Assert.assertFalse(field.isInvalid());

        field.setMin(-8);
        field.setValue(-9); // need to update value to run validation
        Assert.assertTrue(field.isInvalid());

        field.setValue(-8);
        Assert.assertFalse(field.isInvalid());
    }

    @Test
    public void assertMaxValidation() {
        field.setValue(100);
        Assert.assertFalse(field.isInvalid());

        field.setMax(98);
        field.setValue(99); // need to update value to run validation
        Assert.assertTrue(field.isInvalid());

        field.setValue(98);
        Assert.assertFalse(field.isInvalid());
    }

    @Test
    public void stepValidation_minNotDefined() {
        field.setStep(3);

        assertValidValues(-9, -6, -3, 0, 3, 6, 30);
        assertInvalidValues(-10, -1, 2, 32);
    }

    @Test
    public void stepValidation_positiveMin_minUsedAsStepBasis() {
        field.setMin(1);
        field.setStep(3);

        assertValidValues(1, 4, 7);
        assertInvalidValues(2, 3, 5, 6);
    }

    @Test
    public void stepValidation_negativeMin_minUsedAsStepBasis() {
        field.setMin(-5);
        field.setStep(4);

        assertValidValues(-5, -1, 3, 7);
        assertInvalidValues(0, 4, -4);
    }

    @Override
    @Test
    public void elementHasValue_wrapIntoTextField_propertyIsNotSetToInitialValue() {
        ComponentFromTest
                .elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue(1,
                        IntegerField.class);
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

    private void assertValidValues(Integer... values) {
        Arrays.asList(values).forEach(v -> {
            field.setValue(v);
            Assert.assertFalse("Expected field to be valid with value " + v,
                    field.isInvalid());
        });
    }

    private void assertInvalidValues(Integer... values) {
        Arrays.asList(values).forEach(v -> {
            field.setValue(v);
            Assert.assertTrue("Expected field to be invalid with value " + v,
                    field.isInvalid());
        });
    }

    @Test
    public void implementHasAriaLabel() {
        IntegerField field = new IntegerField();
        Assert.assertTrue(field instanceof HasAriaLabel);
    }

    @Test
    public void setAriaLabel() {
        IntegerField field = new IntegerField();

        field.setAriaLabel("aria-label");
        Assert.assertTrue(field.getAriaLabel().isPresent());
        Assert.assertEquals("aria-label", field.getAriaLabel().get());

        field.setAriaLabel(null);
        Assert.assertTrue(field.getAriaLabel().isEmpty());
    }

    @Test
    public void setAriaLabelledBy() {
        IntegerField field = new IntegerField();

        field.setAriaLabelledBy("aria-labelledby");
        Assert.assertTrue(field.getAriaLabelledBy().isPresent());
        Assert.assertEquals("aria-labelledby", field.getAriaLabelledBy().get());

        field.setAriaLabelledBy(null);
        Assert.assertTrue(field.getAriaLabelledBy().isEmpty());
    }

    @Test
    @Override
    public void implementsInputField() {
        IntegerField field = new IntegerField();
        Assert.assertTrue(
                field instanceof InputField<AbstractField.ComponentValueChangeEvent<IntegerField, Integer>, Integer>);
    }
}
