/*
 * Copyright 2000-2021 Vaadin Ltd.
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
 *
 */

package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.vaadin.flow.component.datepicker.DatePickerFormatPage.*;

@TestPath("vaadin-date-picker/date-picker-format")
public class DatePickerFormatIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void testWithPrimaryFormat() {
        Assert.assertEquals(getInputValue(WITH_PRIMARY_FORMAT), "2018-05-13");
    }

    @Test
    public void testWithMultipleFormats() {
        Assert.assertEquals(getInputValue(WITH_MULTIPLE_FORMAT), "2018.05.13");
    }

    @Test
    public void testChangeBetweenFormats() {
        String id = WITH_CHANGE_BETWEEN_FORMATS;

        Assert.assertEquals(getInputValue(id), "13.2018.05");
        $("button").id(CHANGE_TO_FORMAT_BTN).click();
        Assert.assertEquals(getInputValue(id), "5/13/18");
    }

    @Test
    public void testUsingFallbackFormats() {
        String id = WITH_FALLBACK_FORMAT;

        Assert.assertEquals(getInputValue(id), "2018-05-13");
        $("button").id(CHANGE_FORMAT_BUTTON).click();
        Assert.assertEquals(getInputValue(id), "13§05§2018");
    }

    @Test
    public void testFormatsWithSetLocal() {
        Assert.assertEquals(getInputValue(WITH_FORMAT_AND_SET_LOCALE), "2018/05/13");
    }

    @Test
    public void testNullFormat() {
        String id = WITH_NULL_FORMAT;

        Assert.assertEquals(getInputValue(id), "13 2018 05");
        $("button").id(SET_FORMAT_TO_NULL_BUTTON).click();
        Assert.assertEquals(getInputValue(id), "13.5.2018");
    }

    private void setInputValue(String datePickerId, String value) {
        $(DatePickerElement.class)
                .id(datePickerId)
                .setInputValue(value);
    }

    private String getInputValue(String datePickerId) {
        return $(DatePickerElement.class)
                .id(datePickerId)
                .getInputValue();
    }

}
