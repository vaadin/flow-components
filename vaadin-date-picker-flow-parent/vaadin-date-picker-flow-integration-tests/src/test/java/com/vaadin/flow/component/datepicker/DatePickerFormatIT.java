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
    public void testWithPrimaryFormatShouldFormatWithPrimaryFormat() {
        Assert.assertEquals("2018-05-13",
                getInputValue(PRIMARY_FORMAT_DATE_PICKER));
    }

    @Test
    public void testWithPrimaryFormatShouldParseWithPrimaryFormat() {
        submitValue(PRIMARY_FORMAT_DATE_PICKER, "2020-10-23");

        TestBenchElement output = $("span").id(PRIMARY_FORMAT_OUTPUT);
        Assert.assertEquals("2020-10-23", output.getText());
    }

    @Test
    public void testWithMultipleFormatsShouldFormatWithPrimaryFormat() {
        Assert.assertEquals("2018-05-13",
                getInputValue(MULTIPLE_FORMAT_DATE_PICKER));
    }

    @Test
    public void testWithMultipleFormatsShouldParseWithPrimaryFormat() {
        submitValue(MULTIPLE_FORMAT_DATE_PICKER, "2020-10-23");

        TestBenchElement output = $("span").id(MULTIPLE_FORMAT_OUTPUT);
        Assert.assertEquals("2020-10-23", output.getText());
    }

    @Test
    public void testWithMultipleFormatsShouldParseWithAdditionalParsingFormats() {
        TestBenchElement output = $("span").id(MULTIPLE_FORMAT_OUTPUT);

        submitValue(MULTIPLE_FORMAT_DATE_PICKER, "23.10.2020");
        Assert.assertEquals("2020-10-23", output.getText());

        submitValue(MULTIPLE_FORMAT_DATE_PICKER, "02/27/1999");
        Assert.assertEquals("1999-02-27", output.getText());
    }

    @Test
    public void testChangeBetweenFormatsShouldFormatWithNewFormat() {
        String id = CHANGE_BETWEEN_FORMATS_DATE_PICKER;

        Assert.assertEquals("13.2018.05", getInputValue(id));
        $("button").id(CHANGE_BETWEEN_FORMATS_BUTTON).click();
        Assert.assertEquals("5/13/18", getInputValue(id));
    }

    @Test
    public void testChangeBetweenFormatsShouldParseInNewFormat() {
        $("button").id(CHANGE_BETWEEN_FORMATS_BUTTON).click();

        submitValue(CHANGE_BETWEEN_FORMATS_DATE_PICKER, "2/27/21");

        TestBenchElement output = $("span").id(CHANGE_BETWEEN_FORMATS_OUTPUT);

        Assert.assertEquals("2021-02-27", output.getText());
    }

    @Test
    public void testRemovingDateFormatShouldFormatWithLocaleFormat() {
        String id = REMOVE_DATE_FORMAT_DATE_PICKER;

        Assert.assertEquals("13 2018 05", getInputValue(id));
        $("button").id(REMOVE_DATE_FORMAT_BUTTON).click();
        Assert.assertEquals("13.5.2018", getInputValue(id));
    }

    @Test
    public void testRemovingDateFormatShouldParseWithLocaleFormat() {
        $("button").id(REMOVE_DATE_FORMAT_BUTTON).click();

        submitValue(REMOVE_DATE_FORMAT_DATE_PICKER, "15.07.1999");

        TestBenchElement output = $("span").id(REMOVE_DATE_FORMAT_OUTPUT);

        Assert.assertEquals("1999-07-15", output.getText());
    }

    @Test
    public void testRemovingDateFormatShouldNotLogBrowserError() {
        $("button").id(REMOVE_DATE_FORMAT_BUTTON).click();

        // Verify datePickerConnector.setLocale is not called with null
        // parameter which would throw and log an error
        Assert.assertFalse(
                getLogEntries(Level.SEVERE).stream().findAny().isPresent());
    }

    @Test
    public void testSetLocaleAfterFormatShouldFormatWithCustomFormat() {
        Assert.assertEquals(getInputValue(SET_LOCALE_AFTER_FORMAT_DATE_PICKER),
                "2018/05/13");
    }

    @Test
    public void testSetLocaleAfterFormatShouldParseWithCustomFormat() {
        submitValue(SET_LOCALE_AFTER_FORMAT_DATE_PICKER, "1999/07/15");

        TestBenchElement output = $("span").id(SET_LOCALE_AFTER_FORMAT_OUTPUT);

        Assert.assertEquals("1999-07-15", output.getText());
    }

    @Test
    public void testSetFormatAfterSetLocaleShouldFormatWithCustomFormat() {
        Assert.assertEquals(
                getInputValue(SET_DATE_FORMAT_AFTER_LOCALE_DATE_PICKER),
                "2018/05/13");
    }

    @Test
    public void testSetFormatAfterSetLocaleShouldParseWithCustomFormat() {
        submitValue(SET_DATE_FORMAT_AFTER_LOCALE_DATE_PICKER, "1999/07/15");

        TestBenchElement output = $("span")
                .id(SET_DATE_FORMAT_AFTER_LOCALE_OUTPUT);

        Assert.assertEquals("1999-07-15", output.getText());
    }

    @Test
    public void testServerSideValueChangeShouldFormatWithCustomFormat() {
        String id = SERVER_SIDE_VALUE_CHANGE_DATE_PICKER;

        Assert.assertEquals("", getInputValue(id));
        $("button").id(SERVER_SIDE_VALUE_CHANGE_BUTTON).click();
        Assert.assertEquals("13.5.2018", getInputValue(id));
    }

    @Test
    public void testEnteringInvalidValueShouldKeepInvalidValue() {
        String id = MULTIPLE_FORMAT_DATE_PICKER;

        Assert.assertEquals("2018-05-13", getInputValue(id));

        submitValue(id, "foobar");

        Assert.assertEquals("foobar", getInputValue(id));
    }

    @Test
    public void testEnteringInvalidValueShouldNotSubmitToServer() {
        TestBenchElement output = $("span").id(MULTIPLE_FORMAT_OUTPUT);
        String id = MULTIPLE_FORMAT_DATE_PICKER;

        Assert.assertEquals("2018-05-13", getInputValue(id));

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
