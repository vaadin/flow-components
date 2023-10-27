package com.vaadin.flow.component.datepicker.validation;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.datepicker.validation.BasicValidationPage.MIN_INPUT;
import static com.vaadin.flow.component.datepicker.validation.BasicValidationPage.MAX_INPUT;
import static com.vaadin.flow.component.datepicker.validation.BasicValidationPage.REQUIRED_BUTTON;
import static com.vaadin.flow.component.datepicker.validation.BasicValidationPage.CLEAR_VALUE_BUTTON;

@TestPath("vaadin-date-picker/validation/basic")
public class BasicValidationIT extends AbstractValidationIT<DatePickerElement> {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void triggerBlur_assertValidity() {
        testField.sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.setInputValue("1/1/2022");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();

        testField.setInputValue("");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void min_changeValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("2022-03-01", Keys.ENTER);

        testField.setInputValue("2/1/2022");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();

        testField.setInputValue("3/1/2022");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();

        testField.setInputValue("4/1/2022");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();

        testField.setInputValue("");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void max_changeValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("2022-03-01", Keys.ENTER);

        testField.setInputValue("4/1/2022");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();

        testField.setInputValue("3/1/2022");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();

        testField.setInputValue("2/1/2022");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();

        testField.setInputValue("");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void badInput_changeValue_assertValidity() {
        testField.setInputValue("INVALID");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();

        testField.setInputValue("1/1/2022");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();

        testField.setInputValue("INVALID");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();

        testField.setInputValue("");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void setValue_clearValue_assertValidity() {
        testField.setInputValue("1/1/2022");
        assertServerValid();
        assertClientValid();

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void badInput_setValue_clearValue_assertValidity() {
        testField.setInputValue("INVALID");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void detach_attach_preservesInvalidState() {
        // Make field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.setInputValue("1/1/2022");
        testField.setInputValue("");

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
    public void clientSideInvalidStateIsNotPropagatedToServer() {
        // Make the field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.setInputValue("1/1/2022");
        testField.setInputValue("");

        executeScript("arguments[0].invalid = false", testField);

        assertServerInvalid();
    }

    @Override
    protected DatePickerElement getTestField() {
        return $(DatePickerElement.class).first();
    }
}
