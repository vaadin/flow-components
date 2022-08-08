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
package com.vaadin.flow.component.textfield.tests.validation;

import com.vaadin.testbench.HasStringValueProperty;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.textfield.tests.validation.AbstractValidationPage.DETACH_FIELD_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.AbstractValidationPage.ATTACH_FIELD_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.AbstractValidationPage.SERVER_VALIDITY_STATE;
import static com.vaadin.flow.component.textfield.tests.validation.AbstractValidationPage.SERVER_VALIDITY_STATE_BUTTON;

public abstract class AbstractValidationIT<F extends TestBenchElement & HasStringValueProperty>
        extends AbstractComponentIT {
    protected F field;

    @Before
    public void init() {
        open();
        field = getField();
    }

    @Test
    public void required_fieldIsInitiallyValid() {
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void required_triggerInputBlur_assertValidity() {
        field.sendKeys(Keys.TAB);
        assertServerValid(false);
        assertClientValid(false);
    }

    @Test
    public void required_changeInputValue_assertValidity() {
        field.setValue("Value");
        assertServerValid(true);
        assertClientValid(true);

        field.setValue("");
        assertServerValid(false);
        assertClientValid(false);
    }

    @Test
    public void onlyServerCanSetFieldToValid() {
        executeScript("arguments[0].validate()", field);
        assertClientValid(false);

        TestBenchElement input = field.$("input").first();
        input.setProperty("value", "Not empty value");
        input.dispatchEvent("input");
        executeScript("arguments[0].validate()", field);
        assertClientValid(false);

        input.dispatchEvent("change");
        assertServerValid(true);
        assertClientValid(true);
    }

    @Test
    public void detach_attach_onlyServerCanSetFieldToValid() {
        $("button").id(DETACH_FIELD_BUTTON).click();
        $("button").id(ATTACH_FIELD_BUTTON).click();

        field = getField();

        onlyServerCanSetFieldToValid();
    }

    protected void assertClientValid(boolean expected) {
        Assert.assertEquals(expected, !field.getPropertyBoolean("invalid"));
    }

    protected void assertServerValid(boolean expected) {
        $("button").id(SERVER_VALIDITY_STATE_BUTTON).click();

        var actual = $("div").id(SERVER_VALIDITY_STATE).getText();
        Assert.assertEquals(String.valueOf(expected), actual);
    }

    protected abstract F getField();
}
