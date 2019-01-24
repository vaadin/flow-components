/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import java.util.logging.Level;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.google.common.base.Strings;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

import static org.junit.Assert.assertTrue;

/**
 * Integration tests for {@link DatePicker} validation.
 */
@TestPath("date-picker-validation")
public class DatePickerValidationPageIT extends AbstractComponentIT {

    private WebElement field;
    private WebElement invalidate;
    private WebElement validate;
    private WebElement open;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.id("field"));
        field = findElement(By.id("field"));
        invalidate = findElement(By.id("invalidate"));
        validate = findElement(By.id("validate"));
        open = findElement(By.id("open"));
    }

    @Test
    public void invalidateWhenEmpty() {
        scrollIntoViewAndClick(invalidate);
        assertInvalid();
    }

    @Test
    public void invalidateWhenNotEmpty() {
        setValue("not-empty");
        scrollIntoViewAndClick(invalidate);
        assertServerValueField("null");
        assertInvalid();
    }

    @Test
    public void invalidateAndValidateAgain() {
        scrollIntoViewAndClick(invalidate);
        assertInvalid();
        scrollIntoViewAndClick(validate);
        assertValid();
        scrollIntoViewAndClick(invalidate);
        assertInvalid();
    }

    @Test
    public void validateAndInvalidateAgainWithValues() {
        setValue("1/1/2018");
        assertValid();
        assertServerValueField("2018-01-01");
        setValue("asfda");
        assertInvalid();
        assertServerValueField("null");
        setValue("1/2/2018");
        assertValid();
        assertServerValueField("2018-01-02");
    }

    @Test
    public void invalidatewhenEmptyAndThenInputValidValue() {
        scrollIntoViewAndClick(invalidate);
        setValue("1/1/2018");
        assertValid();
    }

    @Test
    public void openFromServer_overlayVisible() {
        scrollIntoViewAndClick(open);
        waitForElementVisible(By.tagName("vaadin-date-picker-overlay"));
    }

    @Test
    public void invalidLocale() {
        String logList = getLogEntries(Level.WARNING).toString();
        Assert.assertFalse(logList.contains(
                "The locale is not supported, use default locale setting(en-US)."));

        WebElement changeLocale = findElement(By.id("change-locale"));
        scrollIntoViewAndClick(changeLocale);

        waitUntil(driver -> getLogEntries(Level.WARNING).toString().contains(
                "The locale is not supported, use default locale setting(en-US)."));
    }

    @Test
    public void disabledDatePicker() {
        WebElement disabledPicker = findElement(By.id("picker-inside-div"));
        Assert.assertFalse(
                "The date picker should be disabled, when the parent component is disabled.",
                disabledPicker.isEnabled());

        findElement(By.id("set-enabled")).click();
        Assert.assertTrue(
                "The date picker should be enabled after parent component is enabled.",
                disabledPicker.isEnabled());
    }

    @Test
    public void testDifferentLocales() {
        WebElement localePicker = findElement(By.id("locale-picker"));
        WebElement displayText = findInShadowRoot(localePicker, By.id("input"))
                .get(0);
        findElement(By.id("polish-locale")).click();

        executeScript("arguments[0].value = '2018-03-26'", localePicker);
        Assert.assertEquals("Polish Locale is using DD.MM.YYYY format ", true,
                executeScript("return arguments[0].value === '26.03.2018'",
                        displayText));

        findElement(By.id("swedish-locale")).click();
        executeScript("arguments[0].value = '2018-03-25'", localePicker);
        Assert.assertEquals("Swedish Locale is using YYYY-MM-DD format ", true,
                executeScript("return arguments[0].value === '2018-03-25'",
                        displayText));
    }

    @Test
    public void testPickerWithValueAndLocaleFromServerSide() {
        WebElement localePicker = findElement(By.id("locale-picker-server"));
        WebElement displayText = findInShadowRoot(localePicker, By.id("input"))
                .get(0);

        Assert.assertEquals("Initial date is 5/23/2018", true, executeScript(
                "return arguments[0].value === '5/23/2018'", displayText));

        findElement(By.id("polish-locale-server")).click();
        Assert.assertEquals("Polish locale date is 23.05.2018", true,
                executeScript("return arguments[0].value === '23.05.2018'",
                        displayText));

        findElement(By.id("swedish-locale-server")).click();
        Assert.assertEquals("Swedish locale date is 2018-05-23", true,
                executeScript("return arguments[0].value === '2018-05-23'",
                        displayText));
    }

    private void assertInvalid() {
        String invalid = field.getAttribute("invalid");
        Assert.assertTrue("The element should be in invalid state",
                Boolean.parseBoolean(invalid));

        String errorMessage = field.getAttribute("errorMessage");
        Assert.assertEquals("Invalidated from server", errorMessage);
    }

    private void assertValid() {
        String invalid = field.getAttribute("invalid");
        Assert.assertFalse("The element should be in valid state",
                Boolean.parseBoolean(invalid));

        String errorMessage = field.getAttribute("errorMessage");
        Assert.assertTrue("Expected no value for errorMessage",
                Strings.isNullOrEmpty(errorMessage));
    }

    private void assertServerValueField(String text) {
        assertTrue(findElement(By.id("server-side-value")).getText()
                .contains(text));
    }

    private void setValue(String value) {
        IntStream.range(0, 10).forEach(i -> field.sendKeys(Keys.BACK_SPACE));

        field.sendKeys(value);
        executeScript("document.body.click()");

    }
}
