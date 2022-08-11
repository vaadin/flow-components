package com.vaadin.flow.component.datetimepicker.validation;

import com.vaadin.flow.testutil.TestPath;
import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.datetimepicker.validation.DateTimePickerValidationBinderPage.MIN_INPUT;
import static com.vaadin.flow.component.datetimepicker.validation.DateTimePickerValidationBinderPage.MAX_INPUT;
import static com.vaadin.flow.component.datetimepicker.validation.DateTimePickerValidationBinderPage.EXPECTED_VALUE_INPUT;
import static com.vaadin.flow.component.datetimepicker.validation.DateTimePickerValidationBinderPage.REQUIRED_ERROR_MESSAGE;
import static com.vaadin.flow.component.datetimepicker.validation.DateTimePickerValidationBinderPage.UNEXPECTED_VALUE_ERROR_MESSAGE;

@TestPath("vaadin-date-time-picker/validation/binder")
public class DateTimePickerValidationBinderIT extends AbstractValidationIT {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid(true);
        assertServerValid(true);
        assertErrorMessage(null);
    }

    @Test
    public void required_triggerDateInputBlur_assertValidity() {
        getDateInputElement().sendKeys(Keys.TAB);
        assertServerValid(false);
        assertClientValid(false);
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void required_triggerTimeInputBlur_assertValidity() {
        getTimeInputElement().sendKeys(Keys.TAB);
        assertServerValid(false);
        assertClientValid(false);
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void required_changeInputValue_assertValidity() {
        setDateInputValue("1/1/2000");
        setTimeInputValue("12:00");
        assertServerValid(true);
        assertClientValid(true);

        setDateInputValue("");
        assertServerValid(false);
        assertClientValid(false);
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);

        setTimeInputValue("");
        assertServerValid(false);
        assertClientValid(false);
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void min_changeInputValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("2000-02-02T12:00", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2000-03-03T13:00",
                Keys.ENTER);

        // Constraint validation fails:
        setDateInputValue("1/1/2000");
        setTimeInputValue("11:00");
        assertClientValid(false);
        assertServerValid(false);

        // Binder validation fails:
        setDateInputValue("2/2/2000");
        setTimeInputValue("12:00");
        assertClientValid(true);
        assertServerValid(true);
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        setDateInputValue("3/3/2000");
        setTimeInputValue("13:00");
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void max_changeInputValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("2000-02-02T12:00", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2000-03-03T13:00",
                Keys.ENTER);

        // Constraint validation fails:
        setDateInputValue("3/3/2000");
        setTimeInputValue("13:00");
        assertClientValid(false);
        assertServerValid(false);

        // Binder validation fails:
        setDateInputValue("2/2/2000");
        setTimeInputValue("12:00");
        assertClientValid(true);
        assertServerValid(true);
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        setDateInputValue("1/1/2000");
        setTimeInputValue("11:00");
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void badInput_changeInputValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2000-01-01T10:00",
                Keys.ENTER);

        setDateInputValue("INVALID");
        setTimeInputValue("INVALID");
        assertServerValid(false);
        assertClientValid(false);
        assertErrorMessage("");

        setDateInputValue("1/1/2000");
        setTimeInputValue("10:00");
        assertServerValid(true);
        assertClientValid(true);

        setDateInputValue("INVALID");
        setTimeInputValue("INVALID");
        assertServerValid(false);
        assertClientValid(false);
        assertErrorMessage("");
    }
}
