/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.dom.ThemeList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BigDecimalFieldTest extends TextFieldTest {

    private BigDecimalField field;

    @Before
    public void setup() {
        field = new BigDecimalField();
        field.setLocale(Locale.US);
    }

    @Override
    @Test
    public void setValueNull() {
        assertNull("Value should be null", field.getValue());
        field.setValue(new BigDecimal("1"));
        field.setValue(null); // not throwing
    }

    @Override
    @Test
    public void initialValuePropertyValue() {
        assertEquals(field.getEmptyValue(),
                field.getElement().getProperty("value"));
    }

    @Test
    public void valueFormatting_scientificNotationRemoved() {
        assertValueFormatting(new BigDecimal("1e9"), "1000000000");
        assertValueFormatting(new BigDecimal("-1e9"), "-1000000000");

        assertValueFormatting(new BigDecimal("1e-9"), "0.000000001");
        assertValueFormatting(new BigDecimal("-1e-9"), "-0.000000001");
    }

    @Test
    public void valueFormatting_trailingZerosPreserved_leadingZerosRemoved() {
        assertValueFormatting(new BigDecimal("001.100"), "1.100");
    }

    @Test
    public void valueFormatting_scalePreserved() {
        assertValueFormatting(new BigDecimal("1.10").setScale(4), "1.1000");
        assertValueFormatting(new BigDecimal("1.10").setScale(1), "1.1");
    }

    @Test
    public void valueFormatting_scaleWithRounding() {
        assertValueFormatting(
                new BigDecimal("1.01").setScale(1, BigDecimal.ROUND_CEILING),
                "1.1");
        assertValueFormatting(
                new BigDecimal("1.01").setScale(1, BigDecimal.ROUND_FLOOR),
                "1.0");
    }

    @Override
    @Test
    public void elementHasValue_wrapIntoTextField_propertyIsNotSetToInitialValue() {
        ComponentFromTest
                .elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue(
                        BigDecimal.TEN.toString(), BigDecimalField.class);
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

    private void assertValueFormatting(BigDecimal bigDecimal,
            String expectedValueProp) {
        field.setValue(bigDecimal);
        Assert.assertEquals(expectedValueProp,
                field.getElement().getProperty("value"));
    }

    @Test
    public void implementHasAriaLabel() {
        BigDecimalField field = new BigDecimalField();
        Assert.assertTrue(field instanceof HasAriaLabel);
    }

    @Test
    public void setAriaLabel() {
        BigDecimalField field = new BigDecimalField();

        field.setAriaLabel("aria-label");
        Assert.assertTrue(field.getAriaLabel().isPresent());
        Assert.assertEquals("aria-label", field.getAriaLabel().get());

        field.setAriaLabel(null);
        Assert.assertTrue(field.getAriaLabel().isEmpty());
    }

    @Test
    public void setAriaLabelledBy() {
        BigDecimalField field = new BigDecimalField();

        field.setAriaLabelledBy("aria-labelledby");
        Assert.assertTrue(field.getAriaLabelledBy().isPresent());
        Assert.assertEquals("aria-labelledby", field.getAriaLabelledBy().get());

        field.setAriaLabelledBy(null);
        Assert.assertTrue(field.getAriaLabelledBy().isEmpty());
    }

    @Test
    public void implementsInputField() {
        BigDecimalField field = new BigDecimalField();
        Assert.assertTrue(field instanceof InputField);
    }
}
