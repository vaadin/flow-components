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
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.datepicker.DatePickerFormatPage.*;

@TestPath("vaadin-date-picker/date-picker-format")
public class DatePickerFormatIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void testWithPrimaryFormat() {
        Assert.assertEquals("2018-05-13", getInputValue(PRIMARY_FORMAT));
    }

    @Test
    public void testWithMultipleFormats() {
        Assert.assertEquals("2018.05.13", getInputValue(MULTIPLE_FORMAT));
    }

    @Test
    public void testChangeBetweenFormatsFormatsInNewFormat() {
        String id = CHANGE_BETWEEN_FORMATS;

        Assert.assertEquals("13.2018.05", getInputValue(id));
        $("button").id(CHANGE_TO_FORMAT_BTN).click();
        Assert.assertEquals("5/13/18", getInputValue(id));
    }

    @Test
    public void testChangeBetweenFormatsParsesInNewFormat() {
        $("button").id(CHANGE_TO_FORMAT_BTN).click();

        submitValue(CHANGE_BETWEEN_FORMATS, "2/27/21");

        TestBenchElement output = $("span").id(CHANGE_BETWEEN_FORMATS_OUTPUT);

        Assert.assertEquals("2021-02-27", output.getText());
    }

    @Test
    public void testFormatsWithSetLocalShouldFormatWithCustomFormat() {
        Assert.assertEquals(getInputValue(FORMAT_AND_SET_LOCALE), "2018/05/13");
    }

    @Test
    public void testFormatsWithSetLocalShouldParseWithCustomFormat() {
        submitValue(FORMAT_AND_SET_LOCALE, "1999/07/15");

        TestBenchElement output = $("span").id(FORMAT_AND_SET_LOCALE_OUTPUT);

        Assert.assertEquals("1999-07-15", output.getText());
    }

    @Test
    public void testSetFormatAfterSetLocale() {
        Assert.assertEquals(getInputValue(CHANGE_FROM_SET_LOCALE),
                "2018/05/13");
    }

    @Test
    public void testNullFormatShouldFormatWithLocaleFormat() {
        String id = NULL_FORMAT;

        Assert.assertEquals("13 2018 05", getInputValue(id));
        $("button").id(SET_FORMAT_TO_NULL_BUTTON).click();
        Assert.assertEquals("13.5.2018", getInputValue(id));
    }

    @Test
    public void testNullFormatShouldParseWithLocaleFormat() {
        $("button").id(SET_FORMAT_TO_NULL_BUTTON).click();

        submitValue(NULL_FORMAT, "15.07.1999");

        TestBenchElement output = $("span").id(NULL_FORMAT_OUTPUT);

        Assert.assertEquals("1999-07-15", output.getText());
    }

    @Test
    public void testParsingSingleFormat() {
        submitValue(PARSING_SINGLE_FORMAT, "2020/10/23");

        TestBenchElement output = $("span").id(PARSING_SINGLE_FORMAT_OUTPUT);
        Assert.assertEquals("2020-10-23", output.getText());
    }

    @Test
    public void testWithClientSideChange() {
        submitValue(CLIENT_CHANGE, "23-10-2020");

        TestBenchElement output = $("span").id(CLIENT_CHANGE_OUTPUT);
        Assert.assertEquals("2020-10-23", output.getText());
    }

    @Test
    public void testWithServerSideChange() {
        String id = SERVER_CHANGE;

        Assert.assertEquals("", getInputValue(id));
        $("button").id(CHANGE_DATE_BTN).click();
        Assert.assertEquals("13.5.2018", getInputValue(id));
    }

    @Test
    public void testWithInvalidDateClientSide() {
        String id = INVALID_CLIENT_DATE;

        Assert.assertEquals("2018/05/13", getInputValue(id));

        submitValue(id, "foobar");

        Assert.assertEquals("foobar", getInputValue(id));
    }

    private void submitValue(String id, String value) {
        TestBenchElement input = $(DatePickerElement.class).id(id)
                .$("vaadin-date-picker-text-field").first();

        while (!input.getAttribute("value").isEmpty()) {
            input.sendKeys(Keys.BACK_SPACE);
        }
        input.sendKeys(value);
        input.sendKeys(Keys.ENTER);
        getCommandExecutor().waitForVaadin();
    }

    private String getInputValue(String datePickerId) {
        return $(DatePickerElement.class).id(datePickerId).getInputValue();
    }

}
