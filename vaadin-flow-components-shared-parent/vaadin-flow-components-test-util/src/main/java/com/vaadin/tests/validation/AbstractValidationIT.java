/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.tests.validation;

import static com.vaadin.tests.validation.AbstractValidationPage.SERVER_VALIDITY_STATE;
import static com.vaadin.tests.validation.AbstractValidationPage.SERVER_VALIDITY_STATE_BUTTON;

import org.junit.Assert;
import org.junit.Before;

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
}
