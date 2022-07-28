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

import static com.vaadin.flow.component.datetimepicker.DateTimePickerBinderValidationPage.MAX_VALUE_BUTTON;
import static com.vaadin.flow.component.datetimepicker.DateTimePickerBinderValidationPage.MIN_VALUE_BUTTON;
import static com.vaadin.flow.component.datetimepicker.DateTimePickerBinderValidationPage.REQUIRED_BUTTON;
import static com.vaadin.flow.component.datetimepicker.DateTimePickerBinderValidationPage.SERVER_VALIDITY_STATE;
import static com.vaadin.flow.component.datetimepicker.DateTimePickerBinderValidationPage.SERVER_VALIDITY_STATE_BUTTON;

@TestPath("vaadin-date-time-picker/validation-binder")
public class DateTimePickerConstraintValidationIT extends AbstractComponentIT {

    private DateTimePickerElement field;

    @Before
    public void init() {
        open();
        field = $(DateTimePickerElement.class).waitForFirst();
    }

    @Test
    public void required_fieldIsInitiallyValid() {
        assertClientValid(true);
        assertServerValid(true);
        assertErrorMessage(null);
    }

    @Test
    public void required_triggerInputBlur_assertValidity() {
        findElement(By.id(REQUIRED_BUTTON)).click();
        field.sendKeys(Keys.TAB);
        assertServerValid(false);
        assertClientValid(false);
    }

    @Test
    public void emptyField_invalidTime_assertValidity() {
        field.sendKeys("INVALID");
        field.sendKeys(Keys.TAB);
        assertClientValid(false);
        assertServerValid(false);
    }

    @Test
    public void required_changeInputValue_assertValidity() {
        findElement(By.id(REQUIRED_BUTTON)).click();
        field.setDateTime(LocalDateTime.of(2022, 1, 1, 12, 0));
        assertServerValid(true);
        assertClientValid(true);

        field.setDateTime(null);
        assertServerValid(false);
        assertClientValid(false);
    }

    @Test
    public void minTime_changeInputValue_assertValidity() {
        findElement(By.id(MIN_VALUE_BUTTON)).click();

        // MIN CONSTRAINT FAILS
        field.setDateTime(LocalDateTime.of(2022, 1, 1, 9, 0));
        assertClientValid(false);
        assertServerValid(false);

        // VALIDATIONS PASSES
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

        // VALIDATIONS PASSES
        field.setDateTime(LocalDateTime.of(2022, 1, 1, 12, 0));
        assertClientValid(true);
        assertServerValid(true);
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
}
