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

import java.util.logging.Level;

import static com.vaadin.flow.component.datepicker.DatePickerFormatPage.*;

@TestPath("vaadin-date-picker/date-picker-format")
public class DatePickerFormatIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void testWithPrimaryFormat() {
        Assert.assertEquals("2018-05-13",
                getInputValue(PRIMARY_FORMAT_DATE_PICKER));
    }

    @Test
    public void testWithMultipleFormats() {
        Assert.assertEquals("2018.05.13",
                getInputValue(MULTIPLE_FORMAT_DATE_PICKER));
    }

    @Test
    public void testChangeBetweenFormatsShouldFormatInNewFormat() {
        String id = CHANGE_BETWEEN_FORMATS;

        Assert.assertEquals("13.2018.05", getInputValue(id));
        $("button").id(CHANGE_BETWEEN_FORMATS_BUTTON).click();
        Assert.assertEquals("5/13/18", getInputValue(id));
    }

    @Test
    public void testChangeBetweenFormatsShouldParseInNewFormat() {
        $("button").id(CHANGE_BETWEEN_FORMATS_BUTTON).click();

        submitValue(CHANGE_BETWEEN_FORMATS, "2/27/21");

        TestBenchElement output = $("span").id(CHANGE_BETWEEN_FORMATS_OUTPUT);

        Assert.assertEquals("2021-02-27", output.getText());
    }

    @Test
    public void testFormatAndSetLocalShouldFormatWithCustomFormat() {
        Assert.assertEquals(getInputValue(FORMAT_AND_SET_LOCALE), "2018/05/13");
    }

    @Test
    public void testFormatAndSetLocalShouldParseWithCustomFormat() {
        submitValue(FORMAT_AND_SET_LOCALE, "1999/07/15");

        TestBenchElement output = $("span").id(FORMAT_AND_SET_LOCALE_OUTPUT);

        Assert.assertEquals("1999-07-15", output.getText());
    }

    @Test
    public void testSetFormatAfterSetLocaleShouldFormatWithCustomFormat() {
        Assert.assertEquals(getInputValue(DATE_FORMAT_AFTER_SET_LOCALE),
                "2018/05/13");
    }

    @Test
    public void testRemovingDateFormatShouldFormatWithLocaleFormat() {
        String id = REMOVE_DATE_FORMAT;

        Assert.assertEquals("13 2018 05", getInputValue(id));
        $("button").id(REMOVE_DATE_FORMAT_BUTTON).click();
        Assert.assertEquals("13.5.2018", getInputValue(id));
    }

    @Test
    public void testRemovingDateFormatShouldParseWithLocaleFormat() {
        $("button").id(REMOVE_DATE_FORMAT_BUTTON).click();

        submitValue(REMOVE_DATE_FORMAT, "15.07.1999");

        TestBenchElement output = $("span").id(REMOVE_DATE_FORMAT_OUTPUT);

        Assert.assertEquals("1999-07-15", output.getText());
    }

    /**
     * Verify datePickerConnector.setLocale is not called with null parameter
     */
    @Test
    public void testRemovingDateFormatShouldNotLogBrowserError() {
        $("button").id(REMOVE_DATE_FORMAT_BUTTON).click();

        Assert.assertFalse(
                getLogEntries(Level.SEVERE).stream().findAny().isPresent());
    }

    @Test
    public void testParsingSingleFormat() {
        submitValue(PARSING_SINGLE_FORMAT, "2020/10/23");

        TestBenchElement output = $("span").id(PARSING_SINGLE_FORMAT_OUTPUT);
        Assert.assertEquals("2020-10-23", output.getText());
    }

    @Test
    public void testFallbackDateFormatParsers() {
        TestBenchElement output = $("span").id(FALLBACK_PARSERS_OUTPUT);

        submitValue(FALLBACK_PARSERS, "23-10-2020");
        Assert.assertEquals("2020-10-23", output.getText());

        submitValue(FALLBACK_PARSERS, "24.10.2020");
        Assert.assertEquals("2020-10-24", output.getText());
    }

    @Test
    public void testServerSideDatePickerValueChangeShouldFormatWithLocaleFormat() {
        String id = SERVER_CHANGE;

        Assert.assertEquals("", getInputValue(id));
        $("button").id(CHANGE_DATE_BUTTON).click();
        Assert.assertEquals("13.5.2018", getInputValue(id));
    }

    @Test
    public void testInvalidClientSideDateShouldKeepInvalidValue() {
        String id = INVALID_CLIENT_DATE;

        Assert.assertEquals("2018/05/13", getInputValue(id));

        submitValue(id, "foobar");

        Assert.assertEquals("foobar", getInputValue(id));
    }

    @Test
    public void testInvalidClientSideDateShouldNotSubmitToServer() {
        TestBenchElement output = $("span").id(INVALID_CLIENT_DATE_OUTPUT);
        String id = INVALID_CLIENT_DATE;

        Assert.assertEquals("2018/05/13", getInputValue(id));

        submitValue(id, "foobar");

        Assert.assertEquals("", output.getText());
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
