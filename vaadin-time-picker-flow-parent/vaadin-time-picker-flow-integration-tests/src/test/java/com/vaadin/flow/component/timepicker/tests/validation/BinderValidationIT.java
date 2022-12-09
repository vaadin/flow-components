package com.vaadin.flow.component.timepicker.tests.validation;

import com.vaadin.flow.component.timepicker.testbench.TimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.timepicker.tests.validation.BinderValidationPage.MIN_INPUT;
import static com.vaadin.flow.component.timepicker.tests.validation.BinderValidationPage.MAX_INPUT;
import static com.vaadin.flow.component.timepicker.tests.validation.BinderValidationPage.EXPECTED_VALUE_INPUT;
import static com.vaadin.flow.component.timepicker.tests.validation.BinderValidationPage.REQUIRED_ERROR_MESSAGE;
import static com.vaadin.flow.component.timepicker.tests.validation.BinderValidationPage.UNEXPECTED_VALUE_ERROR_MESSAGE;

@TestPath("vaadin-time-picker/validation/binder")
public class BinderValidationIT
        extends AbstractValidationIT<TimePickerElement> {
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
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("10:00", Keys.ENTER);

        testField.selectByText("10:00");
        assertServerValid();
        assertClientValid();

        testField.selectByText("");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void min_changeInputValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("11:00", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("12:00", Keys.ENTER);

        // Constraint validation fails:
        testField.selectByText("10:00");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        // Binder validation fails:
        testField.selectByText("11:00");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.selectByText("12:00");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void max_changeInputValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("11:00", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("10:00", Keys.ENTER);

        // Constraint validation fails:
        testField.selectByText("12:00");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        // Binder validation fails:
        testField.selectByText("11:00");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.selectByText("10:00");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void badInput_changeInputValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("10:00", Keys.ENTER);

        testField.selectByText("INVALID");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage("");

        testField.selectByText("10:00");
        assertServerValid();
        assertClientValid();

        testField.selectByText("INVALID");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage("");
    }

    protected TimePickerElement getTestField() {
        return $(TimePickerElement.class).first();
    }
}
