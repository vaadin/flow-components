package com.vaadin.flow.component.datetimepicker.validation;

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.datetimepicker.validation.DateTimePickerValidationBasicPage.DETACH_FIELD_BUTTON;
import static com.vaadin.flow.component.datetimepicker.validation.DateTimePickerValidationBasicPage.ATTACH_FIELD_BUTTON;
import static com.vaadin.flow.component.datetimepicker.validation.DateTimePickerValidationBasicPage.MIN_INPUT;
import static com.vaadin.flow.component.datetimepicker.validation.DateTimePickerValidationBasicPage.MAX_INPUT;
import static com.vaadin.flow.component.datetimepicker.validation.DateTimePickerValidationBasicPage.REQUIRED_BUTTON;

@TestPath("vaadin-date-time-picker/validation/basic")
public class DateTimePickerValidationBasicIT extends AbstractValidationIT {
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

        TestBenchElement dateInput = field.$("input").first();
        TestBenchElement timeInput = field.$("input").last();
        dateInput.setProperty("value", "1/1/2000");
        timeInput.setProperty("value", "10:00");
        dateInput.dispatchEvent("input");
        timeInput.dispatchEvent("input");
        executeScript("arguments[0].validate()", field);
        assertClientValid(false);

        dateInput.dispatchEvent("change");
        timeInput.dispatchEvent("change");
        assertServerValid(true);
        assertClientValid(true);
    }

    @Test
    public void detach_attach_onlyServerCanSetFieldToValid() {
        $("button").id(DETACH_FIELD_BUTTON).click();
        $("button").id(ATTACH_FIELD_BUTTON).click();

        field = $(DateTimePickerElement.class).first();

        onlyServerCanSetFieldToValid();
    }

    @Test
    public void required_triggerDateInputBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        TestBenchElement dateInput = field.$("input").first();
        dateInput.sendKeys(Keys.TAB);
        assertServerValid(false);
        assertClientValid(false);
    }

    @Test
    public void required_triggerTimeInputBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        TestBenchElement timeInput = field.$("input").first();
        timeInput.sendKeys(Keys.TAB);
        assertServerValid(false);
        assertClientValid(false);
    }

    @Test
    public void required_changeInputValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        field.setDateInputValue("1/1/2000");
        field.setTimeInputValue("12:00");
        assertServerValid(true);
        assertClientValid(true);

        field.setDateInputValue("");
        assertServerValid(false);
        assertClientValid(false);

        field.setTimeInputValue("");
        assertServerValid(false);
        assertClientValid(false);
    }

    @Test
    public void min_changeInputValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("02-02-2000T12:00", Keys.ENTER);

        field.setDateInputValue("1/1/2000");
        field.setTimeInputValue("11:00");
        assertClientValid(false);
        assertServerValid(false);

        field.setDateInputValue("2/2/2000");
        field.setTimeInputValue("12:00");
        assertClientValid(true);
        assertServerValid(true);

        field.setDateInputValue("3/3/2000");
        field.setTimeInputValue("13:00");
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void max_changeInputValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("02-02-2000T12:00", Keys.ENTER);

        field.setDateInputValue("3/3/2000");
        field.setTimeInputValue("13:00");
        assertClientValid(false);
        assertServerValid(false);

        field.setDateInputValue("2/2/2000");
        field.setTimeInputValue("12:00");
        assertClientValid(true);
        assertServerValid(true);

        field.setDateInputValue("1/1/2000");
        field.setTimeInputValue("11:00");
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void badInput_changeInputValue_assertValidity() {
        field.setDateInputValue("INVALID");
        field.setTimeInputValue("INVALID");
        assertServerValid(false);
        assertClientValid(false);

        field.setDateInputValue("1/1/2000");
        field.setTimeInputValue("10:00");
        assertServerValid(true);
        assertClientValid(true);

        field.setDateInputValue("INVALID");
        field.setTimeInputValue("INVALID");
        assertServerValid(false);
        assertClientValid(false);
    }
}
