package com.vaadin.flow.component.timepicker.tests.validation;

import com.vaadin.flow.component.timepicker.testbench.TimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBasicPage.DETACH_FIELD_BUTTON;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBasicPage.ATTACH_FIELD_BUTTON;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBasicPage.MIN_INPUT;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBasicPage.MAX_INPUT;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBasicPage.REQUIRED_BUTTON;

@TestPath("vaadin-time-picker/validation/basic")
public class TimePickerValidationBasicIT extends AbstractValidationIT {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void onlyServerCanSetFieldToValid() {
        $("button").id(REQUIRED_BUTTON).click();

        executeScript("arguments[0].validate()", field);
        assertClientValid(false);

        field.sendKeys("10:00");
        executeScript("arguments[0].validate()", field);
        assertClientValid(false);

        field.sendKeys(Keys.ENTER);
        assertServerValid(true);
        assertClientValid(true);
    }

    @Test
    public void detach_attach_onlyServerCanSetFieldToValid() {
        $("button").id(DETACH_FIELD_BUTTON).click();
        $("button").id(ATTACH_FIELD_BUTTON).click();

        field = $(TimePickerElement.class).first();

        onlyServerCanSetFieldToValid();
    }

    @Test
    public void required_triggerInputBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        field.sendKeys(Keys.TAB);
        assertServerValid(false);
        assertClientValid(false);
    }

    @Test
    public void required_changeInputValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        field.selectByText("12:00");
        assertServerValid(true);
        assertClientValid(true);

        field.selectByText("");
        assertServerValid(false);
        assertClientValid(false);
    }

    @Test
    public void min_changeInputValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("11:00", Keys.ENTER);

        field.selectByText("10:00");
        assertClientValid(false);
        assertServerValid(false);

        field.selectByText("11:00");
        assertClientValid(true);
        assertServerValid(true);

        field.selectByText("12:00");
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void max_changeInputValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("11:00", Keys.ENTER);

        field.selectByText("12:00");
        assertClientValid(false);
        assertServerValid(false);

        field.selectByText("11:00");
        assertClientValid(true);
        assertServerValid(true);

        field.selectByText("10:00");
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void badInput_changeInputValue_assertValidity() {
        field.selectByText("INVALID");
        assertServerValid(false);
        assertClientValid(false);

        field.selectByText("10:00");
        assertServerValid(true);
        assertClientValid(true);

        field.selectByText("INVALID");
        assertServerValid(false);
        assertClientValid(false);
    }
}
