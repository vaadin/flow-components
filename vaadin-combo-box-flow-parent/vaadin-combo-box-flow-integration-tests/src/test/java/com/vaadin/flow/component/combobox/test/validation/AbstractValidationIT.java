/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test.validation;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

import org.junit.Assert;
import org.junit.Before;

import static com.vaadin.flow.component.combobox.test.validation.AbstractValidationPage.SERVER_VALIDITY_STATE;
import static com.vaadin.flow.component.combobox.test.validation.AbstractValidationPage.SERVER_VALIDITY_STATE_BUTTON;

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
}
