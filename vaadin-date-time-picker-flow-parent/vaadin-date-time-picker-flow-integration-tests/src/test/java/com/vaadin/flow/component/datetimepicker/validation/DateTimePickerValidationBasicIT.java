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

import java.time.LocalDateTime;

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

        TestBenchElement dateInput = getDateInputElement();
        TestBenchElement timeInput = getTimeInputElement();
        dateInput.sendKeys("1/1/2000");
        timeInput.sendKeys("10:00");
        executeScript("arguments[0].validate()", field);
        assertClientValid(false);

        dateInput.sendKeys(Keys.ENTER);
        timeInput.sendKeys(Keys.ENTER);
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

        getDateInputElement().sendKeys(Keys.TAB);
        assertServerValid(false);
        assertClientValid(false);
    }

    @Test
    public void required_triggerTimeInputBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        getTimeInputElement().sendKeys(Keys.TAB);
        assertServerValid(false);
        assertClientValid(false);
    }

    @Test
    public void required_changeInputValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        setDateInputValue("1/1/2000");
        setTimeInputValue("12:00");
        assertServerValid(true);
        assertClientValid(true);

        setDateInputValue("");
        assertServerValid(false);
        assertClientValid(false);

        setTimeInputValue("");
        assertServerValid(false);
        assertClientValid(false);
    }

    @Test
    public void min_changeInputValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("2000-02-02T12:00", Keys.ENTER);

        setDateInputValue("1/1/2000");
        setTimeInputValue("11:00");
        assertClientValid(false);
        assertServerValid(false);

        setDateInputValue("2/2/2000");
        setTimeInputValue("12:00");
        assertClientValid(true);
        assertServerValid(true);

        setDateInputValue("3/3/2000");
        setTimeInputValue("13:00");
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void max_changeInputValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("2000-02-02T12:00", Keys.ENTER);

        setDateInputValue("3/3/2000");
        setTimeInputValue("13:00");
        assertClientValid(false);
        assertServerValid(false);

        setDateInputValue("2/2/2000");
        setTimeInputValue("12:00");
        assertClientValid(true);
        assertServerValid(true);

        setDateInputValue("1/1/2000");
        setTimeInputValue("11:00");
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void badInput_changeInputValue_assertValidity() {
        setDateInputValue("INVALID");
        setTimeInputValue("INVALID");
        assertServerValid(false);
        assertClientValid(false);

        setDateInputValue("1/1/2000");
        setTimeInputValue("10:00");
        assertServerValid(true);
        assertClientValid(true);

        setDateInputValue("INVALID");
        setTimeInputValue("INVALID");
        assertServerValid(false);
        assertClientValid(false);
    }
}
