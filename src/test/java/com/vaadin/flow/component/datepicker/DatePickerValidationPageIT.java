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

import static org.junit.Assert.assertTrue;

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
