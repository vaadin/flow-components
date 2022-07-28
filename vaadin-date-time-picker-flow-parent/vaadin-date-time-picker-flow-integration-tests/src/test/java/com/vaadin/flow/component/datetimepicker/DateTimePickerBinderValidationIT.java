package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import java.time.LocalDateTime;
import java.util.Collections;

import static com.vaadin.flow.component.datetimepicker.DateTimePickerBinderValidationPage.ATTACH_BINDER_BUTTON;
import static com.vaadin.flow.component.datetimepicker.DateTimePickerBinderValidationPage.MAX_VALUE_BUTTON;
import static com.vaadin.flow.component.datetimepicker.DateTimePickerBinderValidationPage.MIN_VALUE_BUTTON;
import static com.vaadin.flow.component.datetimepicker.DateTimePickerBinderValidationPage.REQUIRED_ERROR_MESSAGE;
import static com.vaadin.flow.component.datetimepicker.DateTimePickerBinderValidationPage.SERVER_VALIDITY_STATE;
import static com.vaadin.flow.component.datetimepicker.DateTimePickerBinderValidationPage.SERVER_VALIDITY_STATE_BUTTON;
import static com.vaadin.flow.component.datetimepicker.DateTimePickerBinderValidationPage.UNEXPECTED_VALUE_ERROR_MESSAGE;

@TestPath("vaadin-date-time-picker/binder-validation")
public class DateTimePickerBinderValidationIT extends AbstractComponentIT {

    private DateTimePickerElement field;

    @Before
    public void init() {
        open();
        field = $(DateTimePickerElement.class).waitForFirst();
        findElement(By.id(ATTACH_BINDER_BUTTON)).click();
    }

    @Test
    public void required_fieldIsInitiallyValid() {
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
    public void emptyField_invalidTime_assertValidity() {
        field.sendKeys("INVALID");
        field.sendKeys(Keys.TAB);
        assertClientValid(false);
        assertServerValid(false);
        assertErrorMessage("");
    }

    @Test
    public void required_changeInputValue_assertValidity() {
        field.setDateTime(LocalDateTime.of(2022, 1, 1, 12, 0));
        assertServerValid(true);
        assertClientValid(true);

        field.setDateTime(null);
        assertServerValid(false);
        assertClientValid(false);
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void minTime_changeInputValue_assertValidity() {
        findElement(By.id(MIN_VALUE_BUTTON)).click();

        // MIN CONSTRAINT FAILS
        field.setDateTime(LocalDateTime.of(2022, 1, 1, 9, 0));
        assertClientValid(false);
        assertServerValid(false);
        assertErrorMessage("");

        // BINDER VALIDATION FAILS
        field.setDateTime(LocalDateTime.of(2022, 1, 1, 11, 0));
        assertClientValid(false);
        assertServerValid(false);
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // BOTH VALIDATIONS PASS
        field.setDateTime(LocalDateTime.of(2022, 1, 1, 12, 0));
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void maxTime_changeInputValue_assertValidity() {
        findElement(By.id(MAX_VALUE_BUTTON)).click();

        // MAX CONSTRAINT FAILS
        field.setDateTime(LocalDateTime.of(2022, 1, 1, 15, 0));
        assertClientValid(false);
        assertServerValid(false);
        assertErrorMessage("");

        // BINDER VALIDATION FAILS
        field.setDateTime(LocalDateTime.of(2022, 1, 1, 13, 0));
        assertClientValid(false);
        assertServerValid(false);
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // BOTH VALIDATIONS PASS
        field.setDateTime(LocalDateTime.of(2022, 1, 1, 12, 0));
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void dateTimeField_internalValidationPass_binderValidationFail_fieldInvalid() {
        setInternalValidBinderInvalidValue(field);

        assertClientValid(false);
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);
    }

    @Test
    public void dateTimeField_internalValidationPass_binderValidationFail_validateClient_fieldInvalid() {
        setInternalValidBinderInvalidValue(field);
        field.getCommandExecutor().executeScript(
                "arguments[0].validate(); arguments[0].immediateInvalid = arguments[0].invalid;",
                field);

        assertClientValid(false);
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);
        // State before server round trip (avoid flash of valid
        // state)
        Assert.assertTrue("Unexpected immediateInvalid state",
                field.getPropertyBoolean("immediateInvalid"));
    }

    @Test
    public void dateTimeField_internalValidationPass_binderValidationFail_setClientValid_serverFieldInvalid() {
        setInternalValidBinderInvalidValue(field);
        field.getCommandExecutor().executeScript("arguments[0].invalid = false",
                field);

        assertServerValid(false);
    }

    @Test
    public void dateTimeField_internalValidationPass_binderValidationFail_checkValidity() {
        setInternalValidBinderInvalidValue(field);
        field.getCommandExecutor().executeScript(
                "arguments[0].checkedValidity = !!arguments[0].checkValidity()",
                field);

        // Ensure checkValidity still works
        Assert.assertTrue("Unexpected checkedValidity state",
                field.getPropertyBoolean("checkedValidity"));
    }

    private void assertErrorMessage(String expected) {
        Assert.assertEquals(expected, field.getPropertyString("errorMessage"));
    }

    private void assertClientValid(boolean expected) {
        Assert.assertEquals(expected, !field.getPropertyBoolean("invalid"));
    }

    private void assertServerValid(boolean expected) {
        $("button").id(SERVER_VALIDITY_STATE_BUTTON).click();

        var actual = $("div").id(SERVER_VALIDITY_STATE).getText();
        Assert.assertEquals(String.valueOf(expected), actual);
    }

    private void setInternalValidBinderInvalidValue(
            DateTimePickerElement field) {
        field.setDateTime(LocalDateTime.of(2020, 6, 7, 1, 30));
        field.dispatchEvent("change",
                Collections.singletonMap("bubbles", true));
        field.dispatchEvent("blur");
    }
}
