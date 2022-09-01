package com.vaadin.flow.component.datepicker.validation;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBinderPage.MIN_INPUT;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBinderPage.MAX_INPUT;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBinderPage.EXPECTED_VALUE_INPUT;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBinderPage.REQUIRED_ERROR_MESSAGE;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBinderPage.UNEXPECTED_VALUE_ERROR_MESSAGE;

@TestPath("vaadin-date-picker/validation/binder")
public class DatePickerValidationBinderIT
        extends AbstractValidationIT<DatePickerElement> {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_triggerInputBlur_assertValidity() {
        testField.sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void required_changeInputValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2022-01-01", Keys.ENTER);

        testField.setInputValue("1/1/2022");
        assertServerValid();
        assertClientValid();

        testField.setInputValue("");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void min_changeInputValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("2022-03-01", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2022-04-01", Keys.ENTER);

        // Constraint validation fails:
        testField.setInputValue("2/1/2022");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        // Binder validation fails:
        testField.setInputValue("3/1/2022");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setInputValue("4/1/2022");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void max_changeInputValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("2022-03-01", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2022-02-01", Keys.ENTER);

        // Constraint validation fails:
        testField.setInputValue("4/1/2022");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        // Binder validation fails:
        testField.setInputValue("3/1/2022");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setInputValue("2/1/2022");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void badInput_changeInputValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2022-01-01", Keys.ENTER);

        testField.setInputValue("INVALID");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage("");

        testField.setInputValue("1/1/2022");
        assertServerValid();
        assertClientValid();

        testField.setInputValue("INVALID");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage("");
    }

    protected DatePickerElement getTestField() {
        return $(DatePickerElement.class).first();
    }
}
