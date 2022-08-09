package com.vaadin.flow.component.timepicker.tests.validation;

import com.vaadin.flow.testutil.TestPath;
import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBinderPage.MIN_INPUT;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBinderPage.MAX_INPUT;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBinderPage.EXPECTED_VALUE_INPUT;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBinderPage.REQUIRED_ERROR_MESSAGE;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBinderPage.UNEXPECTED_VALUE_ERROR_MESSAGE;

@TestPath("vaadin-time-picker/validation/binder")
public class TimePickerValidationBinderIT extends AbstractValidationIT {
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
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("10:00", Keys.ENTER);

        field.selectByText("10:00");
        assertServerValid(true);
        assertClientValid(true);

        field.selectByText("");
        assertServerValid(false);
        assertClientValid(false);
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void min_changeInputValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("11:00", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("12:00", Keys.ENTER);

        // Constraint validation fails:
        field.selectByText("10:00");
        assertClientValid(false);
        assertServerValid(false);
        assertErrorMessage("");

        // Binder validation fails:
        field.selectByText("11:00");
        assertClientValid(false);
        assertServerValid(false);
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        field.selectByText("12:00");
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void max_changeInputValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("11:00", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("10:00", Keys.ENTER);

        // Constraint validation fails:
        field.selectByText("12:00");
        assertClientValid(false);
        assertServerValid(false);
        assertErrorMessage("");

        // Binder validation fails:
        field.selectByText("11:00");
        assertClientValid(false);
        assertServerValid(false);
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        field.selectByText("10:00");
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void badInput_changeInputValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("10:00", Keys.ENTER);

        field.selectByText("INVALID");
        assertServerValid(false);
        assertClientValid(false);
        assertErrorMessage("");

        field.selectByText("10:00");
        assertServerValid(true);
        assertClientValid(true);

        field.selectByText("INVALID");
        assertServerValid(false);
        assertClientValid(false);
        assertErrorMessage("");
    }
}
