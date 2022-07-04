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

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

public abstract class AbstractValidationBasicIT<F extends TestBenchElement>
        extends AbstractComponentIT {
    protected F field;
    protected TestBenchElement input;

    @Before
    public void init() {
        open();
        field = getField();
        input = field.$("input").first();
    }

    @Test
    public void required_fieldIsInitiallyValid() {
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void required_triggerInputBlur_fieldIsValidated() {
        input.sendKeys(Keys.TAB);
        assertServerValid(false);
        assertClientValid(false);
    }

    @Test
    public void required_changeInputValue_fieldIsValidated() {
        input.sendKeys("A", Keys.ENTER);
        assertServerValid(true);
        assertClientValid(true);

        input.sendKeys(Keys.BACK_SPACE, Keys.ENTER);
        assertServerValid(false);
        assertClientValid(false);
    }

    @Test
    public void onlyServerCanSetFieldToValid() {
        executeScript("arguments[0].validate()", field);
        assertClientValid(false);

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
        $("button").id("detach-field").click();
        $("button").id("attach-field").click();

        field = getField();
        input = field.$("input").first();

        executeScript("arguments[0].validate()", field);
        assertClientValid(false);

        input.setProperty("value", "Not empty value");
        input.dispatchEvent("input");
        executeScript("arguments[0].validate()", field);
        assertClientValid(false);

        input.dispatchEvent("change");
        assertServerValid(true);
        assertClientValid(true);
    }

    protected void assertClientValid(boolean expected) {
        Assert.assertEquals(expected, !field.getPropertyBoolean("invalid"));
    }

    protected void assertServerValid(boolean expected) {
        $("button").id("retrieve-validity-state").click();
        Assert.assertEquals(String.valueOf(expected),
                $("div").id("validity-state").getText());
    }

    protected abstract F getField();
}
