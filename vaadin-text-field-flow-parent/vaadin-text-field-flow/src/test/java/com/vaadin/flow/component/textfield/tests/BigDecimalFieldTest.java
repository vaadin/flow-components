/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.textfield.BigDecimalField;

public class BigDecimalFieldTest extends TextFieldTest {

    private BigDecimalField field;

    @Before
    public void init() {
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

    private void assertValueFormatting(BigDecimal bigDecimal,
            String expectedValueProp) {
        field.setValue(bigDecimal);
        Assert.assertEquals(expectedValueProp,
                field.getElement().getProperty("value"));
    }

}
