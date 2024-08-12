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
package com.vaadin.tests.validation;

import static com.vaadin.tests.validation.AbstractValidationPage.SERVER_VALIDITY_STATE;
import static com.vaadin.tests.validation.AbstractValidationPage.SERVER_VALIDITY_STATE_BUTTON;

import org.junit.Assert;
import org.junit.Before;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

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
