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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.textfield.testbench.BigDecimalFieldElement;
import com.vaadin.flow.component.textfield.testbench.EmailFieldElement;
import com.vaadin.flow.component.textfield.testbench.IntegerFieldElement;
import com.vaadin.flow.component.textfield.testbench.NumberFieldElement;
import com.vaadin.flow.component.textfield.testbench.PasswordFieldElement;
import com.vaadin.flow.component.textfield.testbench.TextAreaElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.HasStringValueProperty;
import com.vaadin.testbench.TestBenchElement;

/**
 * Verify that when component's internal validation passes, but Binder
 * validation fails, Binder validation status is effective.
 */
@TestPath("vaadin-text-field/binder-validation")
public class BinderValidationPageIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    private void setInternalValidBinderInvalidValue(TestBenchElement field) {
        if (field instanceof EmailFieldElement) {
            ((HasStringValueProperty) field).setValue("foo@bar.com");
        } else {
            ((HasStringValueProperty) field).setValue("1");
        }

        field.dispatchEvent("change",
                Collections.singletonMap("bubbles", true));
        field.dispatchEvent("blur");
    }

    private List<Class<? extends TestBenchElement>> fieldClasses = Arrays
            .asList(TextFieldElement.class, TextAreaElement.class,
                    PasswordFieldElement.class, EmailFieldElement.class,
                    BigDecimalFieldElement.class, IntegerFieldElement.class,
                    NumberFieldElement.class);

    @Test
    public void fields_internalValidationPass_binderValidationFail_fieldInvalid() {
        fieldClasses.forEach(clazz -> {
            TestBenchElement field = $(clazz).first();
            setInternalValidBinderInvalidValue(field);
            assertInvalid(field);
        });
    }

    @Test
    public void fields_internalValidationPass_binderValidationFail_validateClient_fieldInvalid() {
        fieldClasses.forEach(clazz -> {
            TestBenchElement field = $(clazz).first();

            setInternalValidBinderInvalidValue(field);

            field.getCommandExecutor().executeScript(
                    "arguments[0].validate(); arguments[0].immediateInvalid = arguments[0].invalid;",
                    field);

            assertInvalid(field);
            // State before server roundtrip (avoid flash of valid
            // state)
            Assert.assertTrue("Unexpected immediateInvalid state",
                    field.getPropertyBoolean("immediateInvalid"));
        });
    }

    @Test
    public void fields_internalValidationPass_binderValidationFail_setClientValid_serverFieldInvalid() {
        fieldClasses.forEach(clazz -> {
            TestBenchElement field = $(clazz).first();

            setInternalValidBinderInvalidValue(field);

            field.getCommandExecutor()
                    .executeScript("arguments[0].invalid = false", field);

            Assert.assertEquals(field.getPropertyString("label"), "invalid");
        });
    }

    @Test
    public void fields_internalValidationPass_binderValidationFail_checkValidity() {
        fieldClasses.forEach(clazz -> {
            TestBenchElement field = $(clazz).first();

            setInternalValidBinderInvalidValue(field);

            field.getCommandExecutor().executeScript(
                    "arguments[0].checkedValidity = arguments[0].checkValidity()",
                    field);

            // Ensure checkValidity still works (used by
            // preventinvalidinput)
            Assert.assertTrue("Unexpected checkedValidity state",
                    field.getPropertyBoolean("checkedValidity"));
        });
    }

    private void assertInvalid(TestBenchElement field) {
        Assert.assertTrue("Unexpected invalid state",
                field.getPropertyBoolean("invalid"));
        Assert.assertEquals(
                "Expected to have error message configured in the Binder Validator",
                BinderValidationPage.BINDER_ERROR_MSG,
                field.getPropertyString("errorMessage"));
    }
}
