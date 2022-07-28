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
package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.vaadin.flow.component.datetimepicker.DateTimePickerDetachAttachPage.SERVER_VALIDITY_STATE;
import static com.vaadin.flow.component.datetimepicker.DateTimePickerDetachAttachPage.SERVER_VALIDITY_STATE_BUTTON;

/**
 * Integration tests for attaching / detaching date time picker.
 */
@TestPath("vaadin-date-time-picker/date-time-picker-detach-attach")
public class DateTimePickerDetachAttachPageIT extends AbstractComponentIT {

    private DateTimePickerElement field;
    private TestBenchElement toggleAttach;

    @Before
    public void init() {
        open();
        field = $(DateTimePickerElement.class).waitForFirst();
        toggleAttach = $("button").id("toggle-attached");
    }

    @Test
    public void onlyServerCanSetFieldToValid() {
        executeScript("arguments[0].validate()", field);
        assertClientValid(false);

        var inputDate = field.$("vaadin-date-time-picker-date-picker input")
                .first();
        inputDate.setProperty("value", "01/01/2022");
        inputDate.dispatchEvent("input");
        var inputTime = field.$("vaadin-date-time-picker-time-picker input")
                .first();
        inputTime.setProperty("value", "11.00");
        inputTime.dispatchEvent("input");
        executeScript("arguments[0].validate()", field);
        assertClientValid(false);

        inputDate.dispatchEvent("change");
        inputTime.dispatchEvent("change");
        executeScript("document.body.click()");
        executeScript("document.body.click()");
        assertClientValid(true);
        assertServerValid(true);
    }

    protected void assertClientValid(boolean expected) {
        Assert.assertEquals(expected, !field.getPropertyBoolean("invalid"));
    }

    protected void assertServerValid(boolean expected) {
        $("button").id(SERVER_VALIDITY_STATE_BUTTON).click();

        var actual = $("div").id(SERVER_VALIDITY_STATE).getText();
        Assert.assertEquals(String.valueOf(expected), actual);
    }

    @Test
    public void detach_attach_onlyServerCanSetFieldToValid() {
        toggleAttach.click();
        toggleAttach.click();

        field = $(DateTimePickerElement.class).waitForFirst();

        onlyServerCanSetFieldToValid();
    }
}
