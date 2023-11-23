/*
 * Copyright 2000-2023 Vaadin Ltd.
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
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

@TestPath("vaadin-date-picker/date-picker-custom-format")
public class DatePickerCustomFormatIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void testWithPrimaryFormatShouldFormatWithPrimaryFormat() {
        Assert.assertEquals("2018-05-13", getInputValue(
                DatePickerCustomFormatPage.PRIMARY_FORMAT_DATE_PICKER));
    }

    @Test
    public void testWithPrimaryFormatShouldParseWithPrimaryFormat() {
        submitValue(DatePickerCustomFormatPage.PRIMARY_FORMAT_DATE_PICKER,
                "2020-10-23");

        TestBenchElement output = $("span")
                .id(DatePickerCustomFormatPage.PRIMARY_FORMAT_OUTPUT);
        Assert.assertEquals("2020-10-23", output.getText());
    }

    @Test
    public void testWithMultipleFormatsShouldFormatWithPrimaryFormat() {
        Assert.assertEquals("2018-05-13", getInputValue(
                DatePickerCustomFormatPage.MULTIPLE_FORMAT_DATE_PICKER));
    }

    @Test
    public void testWithMultipleFormatsShouldParseWithPrimaryFormat() {
        submitValue(DatePickerCustomFormatPage.MULTIPLE_FORMAT_DATE_PICKER,
                "2020-10-23");

        TestBenchElement output = $("span")
                .id(DatePickerCustomFormatPage.MULTIPLE_FORMAT_OUTPUT);
        Assert.assertEquals("2020-10-23", output.getText());
    }

    @Test
    public void testWithMultipleFormatsShouldParseWithAdditionalParsingFormats() {
        TestBenchElement output = $("span")
                .id(DatePickerCustomFormatPage.MULTIPLE_FORMAT_OUTPUT);

        submitValue(DatePickerCustomFormatPage.MULTIPLE_FORMAT_DATE_PICKER,
                "23.10.2020");
        Assert.assertEquals("2020-10-23", output.getText());

        submitValue(DatePickerCustomFormatPage.MULTIPLE_FORMAT_DATE_PICKER,
                "02/27/1999");
        Assert.assertEquals("1999-02-27", output.getText());
    }

    @Test
    public void testChangeBetweenFormatsShouldFormatWithNewFormat() {
        String id = DatePickerCustomFormatPage.CHANGE_BETWEEN_FORMATS_DATE_PICKER;

        Assert.assertEquals("13.2018.05", getInputValue(id));
        $("button").id(DatePickerCustomFormatPage.CHANGE_BETWEEN_FORMATS_BUTTON)
                .click();
        Assert.assertEquals("5/13/18", getInputValue(id));
    }

    @Test
    public void testChangeBetweenFormatsShouldParseInNewFormat() {
        $("button").id(DatePickerCustomFormatPage.CHANGE_BETWEEN_FORMATS_BUTTON)
                .click();

        submitValue(
                DatePickerCustomFormatPage.CHANGE_BETWEEN_FORMATS_DATE_PICKER,
                "2/27/21");

        TestBenchElement output = $("span")
                .id(DatePickerCustomFormatPage.CHANGE_BETWEEN_FORMATS_OUTPUT);

        Assert.assertEquals("2021-02-27", output.getText());
    }

    @Test
    public void testRemovingDateFormatShouldFormatWithLocaleFormat() {
        String id = DatePickerCustomFormatPage.REMOVE_DATE_FORMAT_DATE_PICKER;

        Assert.assertEquals("13 2018 05", getInputValue(id));
        $("button").id(DatePickerCustomFormatPage.REMOVE_DATE_FORMAT_BUTTON)
                .click();
        Assert.assertEquals("13.5.2018", getInputValue(id));
    }

    @Test
    public void testTwoDigitsYearFormatBasedParsingShouldUseReferenceDate() {
        String id = DatePickerCustomFormatPage.CUSTOM_REFERENCE_DATE_AND_FORMAT_OPTIONS_DATE_PICKER;
        TestBenchElement output = $("span").id(
                DatePickerCustomFormatPage.CUSTOM_REFERENCE_DATE_AND_FORMAT_OPTIONS_OUTPUT);

        $("button").id("set-short-format").click();

        submitValue(id, "31-02-27");
        Assert.assertEquals("1931-02-27", output.getText());

        submitValue(id, "29-02-27");
        Assert.assertEquals("2029-02-27", output.getText());
    }

    @Test
    public void testFourDigitsYearFormatBasedParsingShouldUseReferenceDate() {
        String id = DatePickerCustomFormatPage.CUSTOM_REFERENCE_DATE_AND_FORMAT_OPTIONS_DATE_PICKER;
        TestBenchElement output = $("span").id(
                DatePickerCustomFormatPage.CUSTOM_REFERENCE_DATE_AND_FORMAT_OPTIONS_OUTPUT);

        $("button").id("set-long-format").click();

        submitValue(id, "2031-02-27");
        Assert.assertEquals("2031-02-27", output.getText());

        submitValue(id, "31-02-27");
        Assert.assertEquals("1931-02-27", output.getText());

        submitValue(id, "29-02-27");
        Assert.assertEquals("2029-02-27", output.getText());

        submitValue(id, "0030-02-27");
        Assert.assertEquals("0030-02-27", output.getText());
    }

    @Test
    public void testMultipleFormatParsingShouldUseReferenceDate() {
        String id = DatePickerCustomFormatPage.CUSTOM_REFERENCE_DATE_AND_FORMAT_OPTIONS_DATE_PICKER;
        TestBenchElement output = $("span").id(
                DatePickerCustomFormatPage.CUSTOM_REFERENCE_DATE_AND_FORMAT_OPTIONS_OUTPUT);

        $("button").id("set-multiple-formats").click();

        submitValue(id, "2031-02-27");
        Assert.assertEquals("2031-02-27", output.getText());

        submitValue(id, "31-02-27");
        Assert.assertEquals("1931-02-27", output.getText());

        submitValue(id, "29-02-27");
        Assert.assertEquals("2029-02-27", output.getText());

        submitValue(id, "0030-02-27");
        Assert.assertEquals("0030-02-27", output.getText());
    }

    @Test
    public void testRemovingDateFormatShouldParseWithLocaleFormat() {
        $("button").id(DatePickerCustomFormatPage.REMOVE_DATE_FORMAT_BUTTON)
                .click();

        submitValue(DatePickerCustomFormatPage.REMOVE_DATE_FORMAT_DATE_PICKER,
                "15.07.1999");

        TestBenchElement output = $("span")
                .id(DatePickerCustomFormatPage.REMOVE_DATE_FORMAT_OUTPUT);

        Assert.assertEquals("1999-07-15", output.getText());
    }

    @Test
    public void testRemovingDateFormatShouldNotLogBrowserError() {
        $("button").id(DatePickerCustomFormatPage.REMOVE_DATE_FORMAT_BUTTON)
                .click();

        // Verify datePickerConnector.setLocale is not called with null
        // parameter which would throw and log an error
        Assert.assertFalse(
                getLogEntries(Level.SEVERE).stream().findAny().isPresent());
    }

    @Test
    public void testSetLocaleAfterFormatShouldFormatWithCustomFormat() {
        Assert.assertEquals(getInputValue(
                DatePickerCustomFormatPage.SET_LOCALE_AFTER_FORMAT_DATE_PICKER),
                "2018/05/13");
    }

    @Test
    public void testSetLocaleAfterFormatShouldParseWithCustomFormat() {
        submitValue(
                DatePickerCustomFormatPage.SET_LOCALE_AFTER_FORMAT_DATE_PICKER,
                "1999/07/15");

        TestBenchElement output = $("span")
                .id(DatePickerCustomFormatPage.SET_LOCALE_AFTER_FORMAT_OUTPUT);

        Assert.assertEquals("1999-07-15", output.getText());
    }

    @Test
    public void testSetFormatAfterSetLocaleShouldFormatWithCustomFormat() {
        Assert.assertEquals(getInputValue(
                DatePickerCustomFormatPage.SET_DATE_FORMAT_AFTER_LOCALE_DATE_PICKER),
                "2018/05/13");
    }

    @Test
    public void testSetFormatAfterSetLocaleShouldParseWithCustomFormat() {
        submitValue(
                DatePickerCustomFormatPage.SET_DATE_FORMAT_AFTER_LOCALE_DATE_PICKER,
                "1999/07/15");

        TestBenchElement output = $("span").id(
                DatePickerCustomFormatPage.SET_DATE_FORMAT_AFTER_LOCALE_OUTPUT);

        Assert.assertEquals("1999-07-15", output.getText());
    }

    @Test
    public void testServerSideValueChangeShouldFormatWithCustomFormat() {
        String id = DatePickerCustomFormatPage.SERVER_SIDE_VALUE_CHANGE_DATE_PICKER;

        Assert.assertEquals("", getInputValue(id));
        $("button")
                .id(DatePickerCustomFormatPage.SERVER_SIDE_VALUE_CHANGE_BUTTON)
                .click();
        Assert.assertEquals("13.5.2018", getInputValue(id));
    }

    @Test
    public void testEnteringInvalidValueShouldKeepInvalidValue() {
        String id = DatePickerCustomFormatPage.MULTIPLE_FORMAT_DATE_PICKER;

        Assert.assertEquals("2018-05-13", getInputValue(id));

        submitValue(id, "foobar");

        Assert.assertEquals("foobar", getInputValue(id));
    }

    @Test
    public void testEnteringInvalidValueShouldNotSubmitToServer() {
        TestBenchElement output = $("span")
                .id(DatePickerCustomFormatPage.MULTIPLE_FORMAT_OUTPUT);
        String id = DatePickerCustomFormatPage.MULTIPLE_FORMAT_DATE_PICKER;

        Assert.assertEquals("2018-05-13", getInputValue(id));

        submitValue(id, "foobar");

        Assert.assertEquals("", output.getText());
    }

    @Test
    public void pickerWithOldReferenceDateAndShortFormat_yearIsRetainedOnOverlayOpenClose() {
        String id = DatePickerCustomFormatPage.OLD_REFERENCE_DATE_WITH_SHORT_FORMAT_DATE_PICKER;
        TestBenchElement output = $("span").id(
                DatePickerCustomFormatPage.OLD_REFERENCE_DATE_WITH_SHORT_FORMAT_OUTPUT);

        String todayString = LocalDate.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
        Assert.assertEquals(todayString, output.getText());

        $(DatePickerElement.class).id(id).click();
        waitForElementPresent(By.tagName("vaadin-date-picker-overlay"));
        $(DatePickerElement.class).id(id).sendKeys(Keys.ESCAPE);
        waitForElementNotPresent(By.tagName("vaadin-date-picker-overlay"));

        Assert.assertEquals(todayString, output.getText());
    }

    private void submitValue(String id, String value) {
        TestBenchElement input = $(DatePickerElement.class).id(id)
                .findElement(By.tagName("input"));

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
