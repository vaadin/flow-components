package com.vaadin.flow.component.datepicker.validation;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBasicPage.DETACH_FIELD_BUTTON;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBasicPage.ATTACH_FIELD_BUTTON;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBasicPage.MIN_INPUT;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBasicPage.MAX_INPUT;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBasicPage.REQUIRED_BUTTON;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBasicPage.SERVER_VALIDITY_STATE;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBasicPage.SERVER_VALIDITY_STATE_BUTTON;

@TestPath("vaadin-date-picker/validation/basic")
public class DatePickerValidationBasicIT extends AbstractComponentIT {
    private DatePickerElement field;

    @Before
    public void init() {
        open();
        field = $(DatePickerElement.class).first();
    }

    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void onlyServerCanSetFieldToValid() {
        $("button").id(REQUIRED_BUTTON).click();

        executeScript("arguments[0].validate()", field);
        assertClientValid(false);

        TestBenchElement input = field.$("input").first();
        input.setProperty("value", "1/1/2022");
        input.dispatchEvent("input");
        executeScript("arguments[0].validate()", field);
        assertClientValid(false);

        input.dispatchEvent("change");
        assertServerValid(true);
        assertClientValid(true);
    }

    @Test
    public void detach_attach_onlyServerCanSetFieldToValid() {
        $("button").id(DETACH_FIELD_BUTTON).click();
        $("button").id(ATTACH_FIELD_BUTTON).click();

        field = $(DatePickerElement.class).first();

        onlyServerCanSetFieldToValid();
    }

    @Test
    public void required_triggerInputBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        field.sendKeys(Keys.TAB);
        assertServerValid(false);
        assertClientValid(false);
    }

    @Test
    public void required_changeInputValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        field.setInputValue("1/1/2022");
        assertServerValid(true);
        assertClientValid(true);

        field.setInputValue("");
        assertServerValid(false);
        assertClientValid(false);
    }

    @Test
    public void min_changeInputValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("2022-03-01", Keys.ENTER);

        field.setInputValue("2/1/2022");
        assertClientValid(false);
        assertServerValid(false);

        field.setInputValue("3/1/2022");
        assertClientValid(true);
        assertServerValid(true);

        field.setInputValue("4/1/2022");
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void max_changeInputValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("2022-03-01", Keys.ENTER);

        field.setInputValue("4/1/2022");
        assertClientValid(false);
        assertServerValid(false);

        field.setInputValue("3/1/2022");
        assertClientValid(true);
        assertServerValid(true);

        field.setInputValue("2/1/2022");
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void badInput_changeInputValue_assertValidity() {
        field.setInputValue("INVALID");
        assertServerValid(false);
        assertClientValid(false);

        field.setInputValue("1/1/2022");
        assertServerValid(true);
        assertClientValid(true);

        field.setInputValue("INVALID");
        assertServerValid(false);
        assertClientValid(false);
    }

    protected void assertClientValid(boolean expected) {
        Assert.assertEquals(expected, !field.getPropertyBoolean("invalid"));
    }

    protected void assertServerValid(boolean expected) {
        $("button").id(SERVER_VALIDITY_STATE_BUTTON).click();

        var actual = $("div").id(SERVER_VALIDITY_STATE).getText();
        Assert.assertEquals(String.valueOf(expected), actual);
    }
}
