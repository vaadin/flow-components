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
package com.vaadin.flow.component.textfield.tests;

import static com.vaadin.flow.component.textfield.tests.IntegerFieldClearValuePage.CLEAR_AND_SET_VALUE_BUTTON;
import static com.vaadin.flow.component.textfield.tests.IntegerFieldClearValuePage.CLEAR_BUTTON;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.textfield.testbench.IntegerFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-integer-field/clear-value")
public class IntegerFieldClearValueIT extends AbstractComponentIT {
    private IntegerFieldElement integerField;

    private TestBenchElement input;

    @Before
    public void init() {
        open();
        integerField = $(IntegerFieldElement.class).first();
        input = integerField.$("input").first();
    }

    @Test
    public void setInputValue_clearValue_inputValueIsEmpty() {
        integerField.sendKeys("1234", Keys.ENTER);
        Assert.assertEquals("1234", input.getPropertyString("value"));

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", input.getPropertyString("value"));
    }

    @Test
    public void setInputValue_clearAndSetSameValue_inputValueIsPresent() {
        integerField.sendKeys("1234", Keys.ENTER);
        Assert.assertEquals("1234", input.getPropertyString("value"));

        $("button").id(CLEAR_AND_SET_VALUE_BUTTON).click();
        Assert.assertEquals("1234", input.getPropertyString("value"));
    }

    @Test
    public void badInput_setInputValue_clearValue_inputValueIsEmpty() {
        // Native [type=number] inputs don't update their value
        // when you are entering input that the browser is unable to parse,
        // so we have to rely on HTML constraints to determine
        // whether the input element is empty or not.
        integerField.sendKeys("--2", Keys.ENTER);
        Assert.assertEquals("", input.getPropertyString("value"));
        Assert.assertTrue((Boolean) executeScript(
                "return arguments[0].validity.badInput", input));

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", input.getPropertyString("value"));
        Assert.assertFalse((Boolean) executeScript(
                "return arguments[0].validity.badInput", input));
    }

    @Test
    public void badInput_setInputValue_clearAndSetValue_inputValueIsPresent() {
        integerField.sendKeys("--2", Keys.ENTER);
        $("button").id(CLEAR_AND_SET_VALUE_BUTTON).click();
        Assert.assertEquals("1234", input.getPropertyString("value"));
    }
}
