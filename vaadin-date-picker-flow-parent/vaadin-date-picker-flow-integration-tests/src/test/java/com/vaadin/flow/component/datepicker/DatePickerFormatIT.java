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

import java.util.logging.Level;

@TestPath("vaadin-date-picker/date-picker-format")
public class DatePickerFormatIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void testWithPrimaryFormatShouldFormatWithPrimaryFormat() {
        Assert.assertEquals("2018-05-13",
                getInputValue(DatePickerFormatPage.PRIMARY_FORMAT_DATE_PICKER));
    }

    @Test
    public void testWithPrimaryFormatShouldParseWithPrimaryFormat() {
        submitValue(DatePickerFormatPage.PRIMARY_FORMAT_DATE_PICKER,
                "2020-10-23");

        TestBenchElement output = $("span")
                .id(DatePickerFormatPage.PRIMARY_FORMAT_OUTPUT);
        Assert.assertEquals("2020-10-23", output.getText());
    }

    @Test
    public void testWithMultipleFormatsShouldFormatWithPrimaryFormat() {
        Assert.assertEquals("2018-05-13", getInputValue(
                DatePickerFormatPage.MULTIPLE_FORMAT_DATE_PICKER));
    }

    @Test
    public void testWithMultipleFormatsShouldParseWithPrimaryFormat() {
        submitValue(DatePickerFormatPage.MULTIPLE_FORMAT_DATE_PICKER,
                "2020-10-23");

        TestBenchElement output = $("span")
                .id(DatePickerFormatPage.MULTIPLE_FORMAT_OUTPUT);
        Assert.assertEquals("2020-10-23", output.getText());
    }

    @Test
    public void testWithMultipleFormatsShouldParseWithAdditionalParsingFormats() {
        TestBenchElement output = $("span")
                .id(DatePickerFormatPage.MULTIPLE_FORMAT_OUTPUT);

        submitValue(DatePickerFormatPage.MULTIPLE_FORMAT_DATE_PICKER,
                "23.10.2020");
        Assert.assertEquals("2020-10-23", output.getText());

        submitValue(DatePickerFormatPage.MULTIPLE_FORMAT_DATE_PICKER,
                "02/27/1999");
        Assert.assertEquals("1999-02-27", output.getText());
    }

    @Test
    public void testChangeBetweenFormatsShouldFormatWithNewFormat() {
        String id = DatePickerFormatPage.CHANGE_BETWEEN_FORMATS_DATE_PICKER;

        Assert.assertEquals("13.2018.05", getInputValue(id));
        $("button").id(DatePickerFormatPage.CHANGE_BETWEEN_FORMATS_BUTTON)
                .click();
        Assert.assertEquals("5/13/18", getInputValue(id));
    }

    @Test
    public void testChangeBetweenFormatsShouldParseInNewFormat() {
        $("button").id(DatePickerFormatPage.CHANGE_BETWEEN_FORMATS_BUTTON)
                .click();

        submitValue(DatePickerFormatPage.CHANGE_BETWEEN_FORMATS_DATE_PICKER,
                "2/27/21");

        TestBenchElement output = $("span")
                .id(DatePickerFormatPage.CHANGE_BETWEEN_FORMATS_OUTPUT);

        Assert.assertEquals("2021-02-27", output.getText());
    }

    @Test
    public void testRemovingDateFormatShouldFormatWithLocaleFormat() {
        String id = DatePickerFormatPage.REMOVE_DATE_FORMAT_DATE_PICKER;

        Assert.assertEquals("13 2018 05", getInputValue(id));
        $("button").id(DatePickerFormatPage.REMOVE_DATE_FORMAT_BUTTON).click();
        Assert.assertEquals("13.5.2018", getInputValue(id));
    }

    @Test
    public void testLocaleWithTwoDigitsYearBasedParsingShouldUseReferenceDate() {
        String id = DatePickerFormatPage.REFERENCE_DATE_AND_LOCALE_WITH_TWO_DIGITS_YEAR_DATE_PICKER;
        TestBenchElement output = $("span").id(
                DatePickerFormatPage.REFERENCE_DATE_AND_LOCALE_WITH_TWO_DIGITS_YEAR_OUTPUT);

        submitValue(id, "02/27/2031");
        Assert.assertEquals("2031-02-27", output.getText());

        submitValue(id, "02/27/31");
        Assert.assertEquals("1931-02-27", output.getText());

        submitValue(id, "02/27/29");
        Assert.assertEquals("2029-02-27", output.getText());

        submitValue(id, "02/27/0030");
        Assert.assertEquals("0030-02-27", output.getText());
    }

    @Test
    public void testLocaleWithFourDigitsYearBasedParsingShouldUseReferenceDate() {
        String id = DatePickerFormatPage.REFERENCE_DATE_AND_LOCALE_WITH_FOUR_DIGITS_YEAR_DATE_PICKER;
        TestBenchElement output = $("span").id(
                DatePickerFormatPage.REFERENCE_DATE_AND_LOCALE_WITH_FOUR_DIGITS_YEAR_OUTPUT);

        submitValue(id, "27/02/2031");
        Assert.assertEquals("2031-02-27", output.getText());

        submitValue(id, "27/02/31");
        Assert.assertEquals("1931-02-27", output.getText());

        submitValue(id, "27/02/29");
        Assert.assertEquals("2029-02-27", output.getText());

        submitValue(id, "27/02/0030");
        Assert.assertEquals("0030-02-27", output.getText());
    }

    @Test
    public void testTwoDigitsYearFormatBasedParsingShouldUseReferenceDate() {
        String id = DatePickerFormatPage.REFERENCE_DATE_AND_FORMAT_WITH_TWO_DIGITS_YEAR_DATE_PICKER;
        TestBenchElement output = $("span").id(
                DatePickerFormatPage.REFERENCE_DATE_AND_FORMAT_WITH_TWO_DIGITS_YEAR_OUTPUT);

        submitValue(id, "31-02-27");
        Assert.assertEquals("1931-02-27", output.getText());

        submitValue(id, "29-02-27");
        Assert.assertEquals("2029-02-27", output.getText());
    }

    @Test
    public void testFourDigitsYearFormatBasedParsingShouldUseReferenceDate() {
        String id = DatePickerFormatPage.REFERENCE_DATE_AND_FORMAT_WITH_FOUR_DIGITS_YEAR_DATE_PICKER;
        TestBenchElement output = $("span").id(
                DatePickerFormatPage.REFERENCE_DATE_AND_FORMAT_WITH_FOUR_DIGITS_YEAR_OUTPUT);

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
        String id = DatePickerFormatPage.REFERENCE_DATE_AND_MULTIPLE_FORMATS_YEAR_DATE_PICKER;
        TestBenchElement output = $("span").id(
                DatePickerFormatPage.REFERENCE_DATE_AND_MULTIPLE_FORMATS_YEAR_OUTPUT);

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
        $("button").id(DatePickerFormatPage.REMOVE_DATE_FORMAT_BUTTON).click();

        submitValue(DatePickerFormatPage.REMOVE_DATE_FORMAT_DATE_PICKER,
                "15.07.1999");

        TestBenchElement output = $("span")
                .id(DatePickerFormatPage.REMOVE_DATE_FORMAT_OUTPUT);

        Assert.assertEquals("1999-07-15", output.getText());
    }

    @Test
    public void testRemovingDateFormatShouldNotLogBrowserError() {
        $("button").id(DatePickerFormatPage.REMOVE_DATE_FORMAT_BUTTON).click();

        // Verify datePickerConnector.setLocale is not called with null
        // parameter which would throw and log an error
        Assert.assertFalse(
                getLogEntries(Level.SEVERE).stream().findAny().isPresent());
    }

    @Test
    public void testSetLocaleAfterFormatShouldFormatWithCustomFormat() {
        Assert.assertEquals(getInputValue(
                DatePickerFormatPage.SET_LOCALE_AFTER_FORMAT_DATE_PICKER),
                "2018/05/13");
    }

    @Test
    public void testSetLocaleAfterFormatShouldParseWithCustomFormat() {
        submitValue(DatePickerFormatPage.SET_LOCALE_AFTER_FORMAT_DATE_PICKER,
                "1999/07/15");

        TestBenchElement output = $("span")
                .id(DatePickerFormatPage.SET_LOCALE_AFTER_FORMAT_OUTPUT);

        Assert.assertEquals("1999-07-15", output.getText());
    }

    @Test
    public void testSetFormatAfterSetLocaleShouldFormatWithCustomFormat() {
        Assert.assertEquals(getInputValue(
                DatePickerFormatPage.SET_DATE_FORMAT_AFTER_LOCALE_DATE_PICKER),
                "2018/05/13");
    }

    @Test
    public void testSetFormatAfterSetLocaleShouldParseWithCustomFormat() {
        submitValue(
                DatePickerFormatPage.SET_DATE_FORMAT_AFTER_LOCALE_DATE_PICKER,
                "1999/07/15");

        TestBenchElement output = $("span")
                .id(DatePickerFormatPage.SET_DATE_FORMAT_AFTER_LOCALE_OUTPUT);

        Assert.assertEquals("1999-07-15", output.getText());
    }

    @Test
    public void testServerSideValueChangeShouldFormatWithCustomFormat() {
        String id = DatePickerFormatPage.SERVER_SIDE_VALUE_CHANGE_DATE_PICKER;

        Assert.assertEquals("", getInputValue(id));
        $("button").id(DatePickerFormatPage.SERVER_SIDE_VALUE_CHANGE_BUTTON)
                .click();
        Assert.assertEquals("13.5.2018", getInputValue(id));
    }

    @Test
    public void testEnteringInvalidValueShouldKeepInvalidValue() {
        String id = DatePickerFormatPage.MULTIPLE_FORMAT_DATE_PICKER;

        Assert.assertEquals("2018-05-13", getInputValue(id));

        submitValue(id, "foobar");

        Assert.assertEquals("foobar", getInputValue(id));
    }

    @Test
    public void testEnteringInvalidValueShouldNotSubmitToServer() {
        TestBenchElement output = $("span")
                .id(DatePickerFormatPage.MULTIPLE_FORMAT_OUTPUT);
        String id = DatePickerFormatPage.MULTIPLE_FORMAT_DATE_PICKER;

        Assert.assertEquals("2018-05-13", getInputValue(id));

        submitValue(id, "foobar");

        Assert.assertEquals("", output.getText());
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
