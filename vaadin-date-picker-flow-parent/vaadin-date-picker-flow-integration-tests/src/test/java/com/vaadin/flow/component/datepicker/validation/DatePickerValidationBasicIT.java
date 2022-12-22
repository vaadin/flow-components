package com.vaadin.flow.component.datepicker.validation;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBasicPage.DETACH_FIELD_BUTTON;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBasicPage.ATTACH_FIELD_BUTTON;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBasicPage.MIN_INPUT;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBasicPage.MAX_INPUT;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBasicPage.REQUIRED_BUTTON;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBasicPage.CLEAR_VALUE_BUTTON;

@TestPath("vaadin-date-picker/validation/basic")
public class DatePickerValidationBasicIT
        extends AbstractValidationIT<DatePickerElement> {
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

        testField.sendKeys("1/1/2022");
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

        testField.setInputValue("1/1/2022");
        assertServerValid();
        assertClientValid();

        testField.setInputValue("");
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void min_changeInputValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("2022-03-01", Keys.ENTER);

        testField.setInputValue("2/1/2022");
        assertClientInvalid();
        assertServerInvalid();

        testField.setInputValue("3/1/2022");
        assertClientValid();
        assertServerValid();

        testField.setInputValue("4/1/2022");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void max_changeInputValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("2022-03-01", Keys.ENTER);

        testField.setInputValue("4/1/2022");
        assertClientInvalid();
        assertServerInvalid();

        testField.setInputValue("3/1/2022");
        assertClientValid();
        assertServerValid();

        testField.setInputValue("2/1/2022");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void badInput_changeInputValue_assertValidity() {
        testField.setInputValue("INVALID");
        assertServerInvalid();
        assertClientInvalid();

        testField.setInputValue("1/1/2022");
        assertServerValid();
        assertClientValid();

        testField.setInputValue("INVALID");
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void badInput_setValue_clearValue_assertValidity() {
        testField.setInputValue("INVALID");
        assertServerInvalid();
        assertClientInvalid();

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerValid();
        assertClientValid();
    }

    protected DatePickerElement getTestField() {
        return $(DatePickerElement.class).first();
    }
}
