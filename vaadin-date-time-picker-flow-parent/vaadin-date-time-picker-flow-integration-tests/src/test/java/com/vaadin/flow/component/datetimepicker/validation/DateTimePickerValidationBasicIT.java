package com.vaadin.flow.component.datetimepicker.validation;

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.validation.AbstractValidationIT;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.datetimepicker.validation.DateTimePickerValidationBasicPage.DETACH_FIELD_BUTTON;
import static com.vaadin.flow.component.datetimepicker.validation.DateTimePickerValidationBasicPage.ATTACH_FIELD_BUTTON;
import static com.vaadin.flow.component.datetimepicker.validation.DateTimePickerValidationBasicPage.MIN_INPUT;
import static com.vaadin.flow.component.datetimepicker.validation.DateTimePickerValidationBasicPage.MAX_INPUT;
import static com.vaadin.flow.component.datetimepicker.validation.DateTimePickerValidationBasicPage.REQUIRED_BUTTON;

@TestPath("vaadin-date-time-picker/validation/basic")
public class DateTimePickerValidationBasicIT
        extends AbstractValidationIT<DateTimePickerElement> {
    private TestBenchElement dateInput;
    private TestBenchElement timeInput;

    @Before
    public void init() {
        super.init();
        dateInput = testField.$("input").first();
        timeInput = testField.$("input").last();
    }

    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void detach_attach_preservesInvalidState() {
        // Make field invalid
        $("button").id(REQUIRED_BUTTON).click();
        dateInput.sendKeys(Keys.TAB);
        timeInput.sendKeys(Keys.TAB);

        detachAndReattachField();

        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void webComponentCanNotModifyInvalidState() {
        assertWebComponentCanNotModifyInvalidState();

        detachAndReattachField();

        assertWebComponentCanNotModifyInvalidState();
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        dateInput.sendKeys(Keys.TAB);
        timeInput.sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void required_changeInputValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        setInputValue(dateInput, "1/1/2000");
        setInputValue(timeInput, "12:00");
        assertServerValid();
        assertClientValid();

        setInputValue(dateInput, "");
        assertServerInvalid();
        assertClientInvalid();

        setInputValue(timeInput, "");
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void badInput_changeInputValue_assertValidity() {
        setInputValue(dateInput, "INVALID");
        setInputValue(timeInput, "INVALID");
        assertServerInvalid();
        assertClientInvalid();

        setInputValue(dateInput, "1/1/2000");
        setInputValue(timeInput, "10:00");
        assertServerValid();
        assertClientValid();

        setInputValue(dateInput, "INVALID");
        setInputValue(timeInput, "INVALID");
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void min_changeInputValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("2000-02-02T12:00", Keys.ENTER);

        setInputValue(dateInput, "1/1/2000");
        setInputValue(timeInput, "11:00");
        assertClientInvalid();
        assertServerInvalid();

        setInputValue(dateInput, "2/2/2000");
        setInputValue(timeInput, "11:00");
        assertClientInvalid();
        assertServerInvalid();

        setInputValue(dateInput, "2/2/2000");
        setInputValue(timeInput, "12:00");
        assertClientValid();
        assertServerValid();

        setInputValue(dateInput, "2/2/2000");
        setInputValue(timeInput, "13:00");
        assertClientValid();
        assertServerValid();

        setInputValue(dateInput, "3/3/2000");
        setInputValue(timeInput, "11:00");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void max_changeDateInputValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("2000-02-02T12:00", Keys.ENTER);

        setInputValue(dateInput, "3/3/2000");
        setInputValue(timeInput, "13:00");
        assertClientInvalid();
        assertServerInvalid();

        setInputValue(dateInput, "2/2/2000");
        setInputValue(timeInput, "13:00");
        assertClientInvalid();
        assertServerInvalid();

        setInputValue(dateInput, "2/2/2000");
        setInputValue(timeInput, "12:00");
        assertClientValid();
        assertServerValid();

        setInputValue(dateInput, "2/2/2000");
        setInputValue(timeInput, "11:00");
        assertClientValid();
        assertServerValid();

        setInputValue(dateInput, "1/1/2000");
        setInputValue(timeInput, "13:00");
        assertClientValid();
        assertServerValid();
    }

    protected DateTimePickerElement getTestField() {
        return $(DateTimePickerElement.class).first();
    }

    private void setInputValue(TestBenchElement input, String value) {
        input.sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        input.sendKeys(value, Keys.ENTER);
    }
}
