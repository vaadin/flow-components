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
package com.vaadin.flow.component.timepicker.tests;

import static com.vaadin.flow.component.timepicker.tests.ClearValuePage.CLEAR_AND_SET_VALUE_BUTTON;
import static com.vaadin.flow.component.timepicker.tests.ClearValuePage.CLEAR_BUTTON;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.timepicker.testbench.TimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-time-picker/clear-value")
public class ClearValueIT extends AbstractComponentIT {
    private TimePickerElement timePicker;

    @Before
    public void init() {
        open();
        timePicker = $(TimePickerElement.class).first();
    }

    @Test
    public void setInputValue_clearValue_inputValueIsEmpty() {
        timePicker.selectByText("12:00 PM");
        Assert.assertEquals("12:00 PM", timePicker.getTimePickerInputValue());

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", timePicker.getTimePickerInputValue());
    }

    @Test
    public void setBadInputValue_clearValue_inputValueIsEmpty() {
        timePicker.selectByText("INVALID");
        Assert.assertEquals("INVALID", timePicker.getTimePickerInputValue());

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", timePicker.getTimePickerInputValue());
    }

    @Test
    public void badInput_setInputValue_clearAndSetValue_inputValueIsPresent() {
        timePicker.selectByText("INVALID");
        $("button").id(CLEAR_AND_SET_VALUE_BUTTON).click();
        Assert.assertEquals("12:00 PM", timePicker.getTimePickerInputValue());
    }
}
