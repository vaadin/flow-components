/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.tests.validation;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

import org.junit.Assert;
import org.junit.Before;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static com.vaadin.tests.validation.AbstractValidationPage.ATTACH_FIELD_BUTTON;
import static com.vaadin.tests.validation.AbstractValidationPage.DETACH_FIELD_BUTTON;
import static com.vaadin.tests.validation.AbstractValidationPage.SERVER_VALIDITY_STATE;
import static com.vaadin.tests.validation.AbstractValidationPage.SERVER_VALIDITY_STATE_BUTTON;

public abstract class AbstractValidationIT<T extends TestBenchElement>
        extends AbstractComponentIT {
    protected T testField;

    @Before
    public void init() {
        open();
        testField = getTestField();
    }

    protected abstract T getTestField();

    protected void assertErrorMessage(String expected) {
        Assert.assertEquals(expected,
                testField.getPropertyString("errorMessage"));
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
        return !testField.getPropertyBoolean("invalid");
    }

    protected void assertWebComponentCanNotModifyInvalidState() {
        // There is no good integration test for this, as triggering client
        // validation will also trigger server validation, with the same
        // validation constraints as the client validation, making it impossible
        // to test a difference.
        // Instead, we test that the web component has been properly configured
        // to prevent itself from changing the invalid state.
        Assert.assertFalse(shouldSetInvalid(true));
        Assert.assertFalse(shouldSetInvalid(false));
    }

    private boolean shouldSetInvalid(boolean invalid) {
        return (Boolean) getCommandExecutor().executeScript(
                "const field = arguments[0]; const invalid = arguments[1]; return field._shouldSetInvalid(invalid)",
                testField, invalid);
    }

    protected void detachAndReattachField() {
        $("button").id(DETACH_FIELD_BUTTON).click();
        // Verify element has been removed
        waitUntil(ExpectedConditions.stalenessOf(testField));

        $("button").id(ATTACH_FIELD_BUTTON).click();
        // Retrieve new element instance
        testField = getTestField();
    }
}
