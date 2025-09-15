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
package com.vaadin.flow.component.datepicker;

import static com.vaadin.flow.component.datepicker.ClearValuePage.CLEAR_AND_SET_VALUE_BUTTON;
import static com.vaadin.flow.component.datepicker.ClearValuePage.CLEAR_BUTTON;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-date-picker/clear-value")
public class ClearValueIT extends AbstractComponentIT {
    private DatePickerElement datePicker;

    @Before
    public void init() {
        open();
        datePicker = $(DatePickerElement.class).first();
    }

    @Test
    public void setInputValue_clearValue_inputValueIsEmpty() {
        datePicker.sendKeys("1/1/2022", Keys.ENTER);
        Assert.assertEquals("1/1/2022", datePicker.getInputValue());

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", datePicker.getInputValue());
    }

    @Test
    public void setInputValue_clearAndSetSameValue_inputValueIsPresent() {
        datePicker.sendKeys("1/1/2022", Keys.ENTER);
        Assert.assertEquals("1/1/2022", datePicker.getInputValue());

        $("button").id(CLEAR_AND_SET_VALUE_BUTTON).click();
        Assert.assertEquals("1/1/2022", datePicker.getInputValue());
    }

    @Test
    public void setBadInputValue_clearValue_inputValueIsEmpty() {
        datePicker.sendKeys("INVALID", Keys.ENTER);
        Assert.assertEquals("INVALID", datePicker.getInputValue());

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", datePicker.getInputValue());
    }

    @Test
    public void badInput_setInputValue_clearAndSetValue_inputValueIsPresent() {
        datePicker.sendKeys("INVALID", Keys.ENTER);
        $("button").id(CLEAR_AND_SET_VALUE_BUTTON).click();
        Assert.assertEquals("1/1/2022", datePicker.getInputValue());
    }
}
