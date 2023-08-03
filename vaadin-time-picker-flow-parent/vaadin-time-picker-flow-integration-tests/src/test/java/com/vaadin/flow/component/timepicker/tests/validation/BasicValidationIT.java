package com.vaadin.flow.component.timepicker.tests.validation;

import com.vaadin.flow.component.timepicker.testbench.TimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.timepicker.tests.validation.BasicValidationPage.MIN_INPUT;
import static com.vaadin.flow.component.timepicker.tests.validation.BasicValidationPage.MAX_INPUT;
import static com.vaadin.flow.component.timepicker.tests.validation.BasicValidationPage.REQUIRED_BUTTON;
import static com.vaadin.flow.component.timepicker.tests.validation.BasicValidationPage.CLEAR_VALUE_BUTTON;

@TestPath("vaadin-time-picker/validation/basic")
public class BasicValidationIT extends AbstractValidationIT<TimePickerElement> {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void triggerBlur_assertValidity() {
        testField.sendKeys(Keys.TAB);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.sendKeys(Keys.TAB);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_markAsDirty_triggerBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();
        markAsDirty();

        testField.sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.selectByText("12:00");
        assertServerValid();
        assertClientValid();

        testField.selectByText("");
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void min_changeValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("11:00", Keys.ENTER);

        testField.selectByText("10:00");
        assertClientInvalid();
        assertServerInvalid();

        testField.selectByText("11:00");
        assertClientValid();
        assertServerValid();

        testField.selectByText("12:00");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void max_changeValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("11:00", Keys.ENTER);

        testField.selectByText("12:00");
        assertClientInvalid();
        assertServerInvalid();

        testField.selectByText("11:00");
        assertClientValid();
        assertServerValid();

        testField.selectByText("10:00");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void badInput_changeValue_assertValidity() {
        testField.selectByText("INVALID");
        assertServerInvalid();
        assertClientInvalid();

        testField.selectByText("10:00");
        assertServerValid();
        assertClientValid();

        testField.selectByText("INVALID");
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void badInput_setValue_clearValue_assertValidity() {
        testField.selectByText("INVALID");
        assertServerInvalid();
        assertClientInvalid();

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void detach_attach_preservesInvalidState() {
        // Make field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.sendKeys(Keys.TAB);

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
        testField.sendKeys(Keys.TAB);

        executeScript("arguments[0].invalid = false", testField);

        assertServerInvalid();
    }

    protected TimePickerElement getTestField() {
        return $(TimePickerElement.class).first();
    }
}
