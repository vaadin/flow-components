package com.vaadin.flow.component.datepicker.validation;

import com.vaadin.flow.testutil.TestPath;
import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBinderPage.MIN_INPUT;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBinderPage.MAX_INPUT;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBinderPage.EXPECTED_VALUE_INPUT;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBinderPage.REQUIRED_ERROR_MESSAGE;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBinderPage.UNEXPECTED_VALUE_ERROR_MESSAGE;

@TestPath("vaadin-date-picker/validation/binder")
public class DatePickerValidationBinderIT extends AbstractValidationIT {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid(true);
        assertServerValid(true);
        assertErrorMessage(null);
    }

    @Test
    public void required_triggerInputBlur_assertValidity() {
        field.sendKeys(Keys.TAB);
        assertServerValid(false);
        assertClientValid(false);
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void required_changeInputValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2022-01-01", Keys.ENTER);

        field.setInputValue("1/1/2022");
        assertServerValid(true);
        assertClientValid(true);

        field.setInputValue("");
        assertServerValid(false);
        assertClientValid(false);
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void min_changeInputValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("2022-03-01", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2022-04-01", Keys.ENTER);

        // Constraint validation fails:
        field.setInputValue("2/1/2022");
        assertClientValid(false);
        assertServerValid(false);
        assertErrorMessage("");

        // Binder validation fails:
        field.setInputValue("3/1/2022");
        assertClientValid(false);
        assertServerValid(false);
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        field.setInputValue("4/1/2022");
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void max_changeInputValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("2022-03-01", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2022-02-01", Keys.ENTER);

        // Constraint validation fails:
        field.setInputValue("4/1/2022");
        assertClientValid(false);
        assertServerValid(false);
        assertErrorMessage("");

        // Binder validation fails:
        field.setInputValue("3/1/2022");
        assertClientValid(false);
        assertServerValid(false);
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        field.setInputValue("2/1/2022");
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void badInput_changeInputValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2022-01-01", Keys.ENTER);

        field.setInputValue("INVALID");
        assertServerValid(false);
        assertClientValid(false);
        assertErrorMessage("");

        field.setInputValue("1/1/2022");
        assertServerValid(true);
        assertClientValid(true);

        field.setInputValue("INVALID");
        assertServerValid(false);
        assertClientValid(false);
        assertErrorMessage("");
    }
}
