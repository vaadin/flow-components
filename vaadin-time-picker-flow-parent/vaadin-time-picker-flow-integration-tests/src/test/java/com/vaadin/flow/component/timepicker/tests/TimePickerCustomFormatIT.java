/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import static com.vaadin.flow.component.timepicker.tests.TimePickerCustomFormatView.CLEAR_FORMATS_BUTTON;
import static com.vaadin.flow.component.timepicker.tests.TimePickerCustomFormatView.SERVER_VALUE;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.timepicker.testbench.TimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-time-picker/custom-format")
public class TimePickerCustomFormatIT extends AbstractComponentIT {
    private TimePickerElement timePicker;

    @Before
    public void init() {
        open();
        timePicker = $(TimePickerElement.class).first();
    }

    @Test
    public void customFormat_formatsParsesAndRevertsToLocale() {
        Assert.assertEquals("13.05", timePicker.getTimePickerInputValue());

        timePicker.selectByText("930");
        Assert.assertEquals("09.30", timePicker.getTimePickerInputValue());
        Assert.assertEquals("09:30", $("span").id(SERVER_VALUE).getText());

        $("button").id(CLEAR_FORMATS_BUTTON).click();
        Assert.assertEquals("9:30 AM", timePicker.getTimePickerInputValue());
    }
}
