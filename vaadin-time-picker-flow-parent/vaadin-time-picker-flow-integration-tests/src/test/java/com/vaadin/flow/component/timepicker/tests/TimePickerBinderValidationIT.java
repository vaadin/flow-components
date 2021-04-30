/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.timepicker.testbench.TimePickerElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

/**
 * Integration tests for the {@link BinderValidationPage}.
 */
@TestPath("vaadin-time-picker/binder-validation")
public class TimePickerBinderValidationIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    private void setInternalValidBinderInvalidValue(TimePickerElement field) {
        field.setValue("14:00");
        field.dispatchEvent("change",
                Collections.singletonMap("bubbles", true));
        field.dispatchEvent("blur");
    }

    @Test
    public void timeField_internalValidationPass_binderValidationFail_fieldInvalid() {
        TimePickerElement field = $(TimePickerElement.class).first();
        setInternalValidBinderInvalidValue(field);
        assertInvalid(field);
    }

    @Test
    public void timeField_internalValidationPass_binderValidationFail_validateClient_fieldInvalid() {
        TimePickerElement field = $(TimePickerElement.class).first();

        setInternalValidBinderInvalidValue(field);

        field.getCommandExecutor().executeScript(
                "arguments[0].validate(); arguments[0].immediateInvalid = arguments[0].invalid;",
                field);

        assertInvalid(field);
        // State before server roundtrip (avoid flash of valid
        // state)
        Assert.assertTrue("Unexpected immediateInvalid state",
                field.getPropertyBoolean("immediateInvalid"));
    }

    @Test
    public void timeField_internalValidationPass_binderValidationFail_setClientValid_serverFieldInvalid() {
        TimePickerElement field = $(TimePickerElement.class).first();

        setInternalValidBinderInvalidValue(field);

        field.getCommandExecutor()
                .executeScript("arguments[0].invalid = false", field);

        Assert.assertEquals(field.getPropertyString("label"), "invalid");
    }

    @Test
    public void timeField_internalValidationPass_binderValidationFail_checkValidity() {
        TimePickerElement field = $(TimePickerElement.class).first();

        setInternalValidBinderInvalidValue(field);

        field.getCommandExecutor().executeScript(
                "arguments[0].checkedValidity = !!arguments[0].checkValidity()",
                field);

        // Ensure checkValidity still works
        Assert.assertTrue("Unexpected checkedValidity state",
                field.getPropertyBoolean("checkedValidity"));
    }

    private void assertInvalid(TimePickerElement field) {
        Assert.assertTrue("Unexpected invalid state",
                field.getPropertyBoolean("invalid"));
        Assert.assertEquals(
                "Expected to have error message configured in the Binder Validator",
                BinderValidationPage.BINDER_ERROR_MSG,
                field.getPropertyString("errorMessage"));
    }
}
