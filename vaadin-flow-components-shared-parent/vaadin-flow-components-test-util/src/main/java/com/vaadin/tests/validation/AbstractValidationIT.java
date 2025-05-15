/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.tests.validation;

import static com.vaadin.tests.validation.AbstractValidationPage.ATTACH_AND_INVALIDATE_FIELD_BUTTON;
import static com.vaadin.tests.validation.AbstractValidationPage.ATTACH_FIELD_BUTTON;
import static com.vaadin.tests.validation.AbstractValidationPage.DETACH_FIELD_BUTTON;
import static com.vaadin.tests.validation.AbstractValidationPage.HIDE_FIELD_BUTTON;
import static com.vaadin.tests.validation.AbstractValidationPage.SERVER_VALIDATION_COUNTER;
import static com.vaadin.tests.validation.AbstractValidationPage.SERVER_VALIDATION_COUNTER_RESET_BUTTON;
import static com.vaadin.tests.validation.AbstractValidationPage.SERVER_VALIDITY_STATE;
import static com.vaadin.tests.validation.AbstractValidationPage.SERVER_VALIDITY_STATE_BUTTON;
import static com.vaadin.tests.validation.AbstractValidationPage.SHOW_AND_INVALIDATE_FIELD_BUTTON;

import org.junit.Assert;
import org.junit.Before;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.vaadin.testbench.HasValidation;
import com.vaadin.tests.AbstractComponentIT;

public abstract class AbstractValidationIT<T extends HasValidation>
        extends AbstractComponentIT {
    protected T testField;

    @Before
    public void init() {
        open();
        testField = getTestField();
    }

    protected abstract T getTestField();

    protected void assertValidationCount(int expected) {
        int actual = Integer
                .parseInt($("div").id(SERVER_VALIDATION_COUNTER).getText());
        Assert.assertEquals("The field should have validated " + expected
                + " times on the server-side", expected, actual);
        resetValidationCount();
    }

    protected void resetValidationCount() {
        $("button").id(SERVER_VALIDATION_COUNTER_RESET_BUTTON).click();
    }

    protected void assertErrorMessage(String expected) {
        expected = expected == null ? "" : expected;
        Assert.assertEquals(expected, testField.getErrorMessage());
    }

    protected void assertClientValid() {
        Assert.assertTrue("The field should be valid on the client-side",
                isClientValid());
    }

    protected void assertClientInvalid() {
        Assert.assertFalse("The field should be invalid on the client-side",
                isClientValid());
    }

    protected void assertServerValid() {
        Assert.assertTrue("The field should be valid on the server-side",
                isServerValid());
    }

    protected void assertServerInvalid() {
        Assert.assertFalse("The field should be invalid on the server-side",
                isServerValid());
    }

    private boolean isServerValid() {
        $("button").id(SERVER_VALIDITY_STATE_BUTTON).click();

        String actual = $("div").id(SERVER_VALIDITY_STATE).getText();
        return actual.equals("true");
    }

    private boolean isClientValid() {
        return !testField.isInvalid();
    }

    protected void detachAndReattachField() {
        detachField();
        attachField();
    }

    protected void detachField() {
        $("button").id(DETACH_FIELD_BUTTON).click();
        // Verify element has been removed
        waitUntil(ExpectedConditions.stalenessOf(testField));
    }

    protected void attachField() {
        $("button").id(ATTACH_FIELD_BUTTON).click();
        // Retrieve new element instance
        testField = getTestField();
    }

    protected void attachAndInvalidateField() {
        $("button").id(ATTACH_AND_INVALIDATE_FIELD_BUTTON).click();
        // Retrieve new element instance
        testField = getTestField();
    }

    protected void hideField() {
        $("button").id(HIDE_FIELD_BUTTON).click();
    }

    protected void showAndInvalidateField() {
        $("button").id(SHOW_AND_INVALIDATE_FIELD_BUTTON).click();
    }
}
