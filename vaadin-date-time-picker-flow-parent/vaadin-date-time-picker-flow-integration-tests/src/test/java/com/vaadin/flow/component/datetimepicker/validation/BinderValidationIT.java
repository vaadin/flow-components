package com.vaadin.flow.component.datetimepicker.validation;

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.validation.AbstractValidationIT;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.datetimepicker.validation.BinderValidationPage.MIN_INPUT;
import static com.vaadin.flow.component.datetimepicker.validation.BinderValidationPage.MAX_INPUT;
import static com.vaadin.flow.component.datetimepicker.validation.BinderValidationPage.EXPECTED_VALUE_INPUT;
import static com.vaadin.flow.component.datetimepicker.validation.BinderValidationPage.CLEAR_VALUE_BUTTON;
import static com.vaadin.flow.component.datetimepicker.validation.BinderValidationPage.REQUIRED_ERROR_MESSAGE;
import static com.vaadin.flow.component.datetimepicker.validation.BinderValidationPage.UNEXPECTED_VALUE_ERROR_MESSAGE;

@TestPath("vaadin-date-time-picker/validation/binder")
public class BinderValidationIT
        extends AbstractValidationIT<DateTimePickerElement> {
    private TestBenchElement dateInput;
    private TestBenchElement timeInput;

    @Before
    public void init() {
        super.init();
        dateInput = testField.$("vaadin-date-time-picker-date-picker")
                .attribute("slot", "date-picker").first()
                .$("vaadin-date-time-picker-date-text-field").first().$("input")
                .first();
        timeInput = testField.$("vaadin-date-time-picker-time-picker")
                .attribute("slot", "time-picker").first()
                .$("vaadin-combo-box-light").first()
                .$("vaadin-date-time-picker-time-text-field").first().$("input")
                .first();
    }

    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_triggerDateInputBlur_assertValidity() {
        dateInput.sendKeys(Keys.TAB);
        timeInput.sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void required_triggerTimeInputBlur_assertValidity() {
        timeInput.sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2000-01-01T12:00",
                Keys.ENTER);

        setInputValue(dateInput, "1/1/2000");
        setInputValue(timeInput, "12:00");
        assertServerValid();
        assertClientValid();

        setInputValue(dateInput, "");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);

        setInputValue(timeInput, "");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void min_changeValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("2000-02-02T12:00", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2000-03-03T11:00",
                Keys.ENTER);

        setInputValue(dateInput, "1/1/2000");
        setInputValue(timeInput, "11:00");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        setInputValue(dateInput, "2/2/2000");
        setInputValue(timeInput, "11:00");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        setInputValue(dateInput, "2/2/2000");
        setInputValue(timeInput, "12:00");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        setInputValue(dateInput, "2/2/2000");
        setInputValue(timeInput, "13:00");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        setInputValue(dateInput, "3/3/2000");
        setInputValue(timeInput, "11:00");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void max_changeValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("2000-02-02T12:00", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2000-01-01T13:00",
                Keys.ENTER);

        setInputValue(dateInput, "3/3/2000");
        setInputValue(timeInput, "13:00");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        setInputValue(dateInput, "2/2/2000");
        setInputValue(timeInput, "13:00");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        setInputValue(dateInput, "2/2/2000");
        setInputValue(timeInput, "12:00");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        setInputValue(dateInput, "2/2/2000");
        setInputValue(timeInput, "11:00");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        setInputValue(dateInput, "1/1/2000");
        setInputValue(timeInput, "13:00");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void badInput_changeValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2000-01-01T10:00",
                Keys.ENTER);

        setInputValue(dateInput, "INVALID");
        setInputValue(timeInput, "INVALID");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage("");

        setInputValue(dateInput, "1/1/2000");
        setInputValue(timeInput, "10:00");
        assertServerValid();
        assertClientValid();

        setInputValue(dateInput, "INVALID");
        setInputValue(timeInput, "INVALID");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage("");
    }

    @Test
    public void badInput_setValue_clearValue_assertValidity() {
        setInputValue(dateInput, "INVALID");
        setInputValue(timeInput, "INVALID");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage("");

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    protected DateTimePickerElement getTestField() {
        return $(DateTimePickerElement.class).first();
    }

    private void setInputValue(TestBenchElement input, String value) {
        input.sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        input.sendKeys(value, Keys.ENTER);
    }
}
