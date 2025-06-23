/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for {@link DatePicker} validation.
 */
@TestPath("vaadin-date-picker/date-picker-validation")
public class DatePickerValidationPageIT extends AbstractComponentIT {

    private WebElement field;
    private WebElement invalidate;
    private WebElement validate;

    @Before
    public void init() {

        open();
        waitForElementPresent(By.id("field"));
        field = findElement(By.id("field"));
        invalidate = findElement(By.id("invalidate"));
        validate = findElement(By.id("validate"));
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
        assertTrue($("label").id("server-side-value").getText().contains(text));
    }

    private void setValue(String value) {
        IntStream.range(0, 10).forEach(i -> field.sendKeys(Keys.BACK_SPACE));

        field.sendKeys(value);
        executeScript("document.body.click()");
    }
}
