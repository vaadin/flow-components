package com.vaadin.flow.component.timepicker.tests.validation;

import com.vaadin.flow.component.timepicker.testbench.TimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBasicPage.DETACH_FIELD_BUTTON;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBasicPage.ATTACH_FIELD_BUTTON;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBasicPage.MIN_INPUT;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBasicPage.MAX_INPUT;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBasicPage.REQUIRED_BUTTON;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBasicPage.CLEAR_VALUE_BUTTON;

@TestPath("vaadin-time-picker/validation/basic")
public class TimePickerValidationBasicIT
        extends AbstractValidationIT<TimePickerElement> {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void onlyServerCanSetFieldToValid() {
        $("button").id(REQUIRED_BUTTON).click();

        executeScript("arguments[0].validate()", testField);
        assertClientInvalid();

        testField.sendKeys("10:00");
        executeScript("arguments[0].validate()", testField);
        assertClientInvalid();

        testField.sendKeys(Keys.ENTER);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void detach_attach_onlyServerCanSetFieldToValid() {
        $("button").id(DETACH_FIELD_BUTTON).click();
        $("button").id(ATTACH_FIELD_BUTTON).click();

        testField = getTestField();

        onlyServerCanSetFieldToValid();
    }

    @Test
    public void required_triggerInputBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void required_changeInputValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.selectByText("12:00");
        assertServerValid();
        assertClientValid();

        testField.selectByText("");
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void min_changeInputValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("11:00", Keys.ENTER);

        testField.selectByText("10:00");
        assertClientInvalid();
        assertServerInvalid();

        testField.selectByText("11:00");
        assertClientValid();
        assertServerValid();

        testField.selectByText("12:00");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void max_changeInputValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("11:00", Keys.ENTER);

        testField.selectByText("12:00");
        assertClientInvalid();
        assertServerInvalid();

        testField.selectByText("11:00");
        assertClientValid();
        assertServerValid();

        testField.selectByText("10:00");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void badInput_changeInputValue_assertValidity() {
        testField.selectByText("INVALID");
        assertServerInvalid();
        assertClientInvalid();

        testField.selectByText("10:00");
        assertServerValid();
        assertClientValid();

        testField.selectByText("INVALID");
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void badInput_setValue_clearValue_assertValidity() {
        testField.selectByText("INVALID");
        assertServerInvalid();
        assertClientInvalid();

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerValid();
        assertClientValid();
    }

    protected TimePickerElement getTestField() {
        return $(TimePickerElement.class).first();
    }
}
