package com.vaadin.flow.component.radiobutton.tests.validation;

import com.vaadin.flow.component.radiobutton.testbench.RadioButtonElement;
import com.vaadin.flow.component.radiobutton.testbench.RadioButtonGroupElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;
import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.radiobutton.tests.validation.BasicValidationPage.REQUIRED_BUTTON;
import static com.vaadin.flow.component.radiobutton.tests.validation.BasicValidationPage.SET_INVALID_BUTTON;

@TestPath("vaadin-radio-button-group/validation/basic")
public class BasicValidationIT
        extends AbstractValidationIT<RadioButtonGroupElement> {

    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void triggerBlur_assertValidity() {
        testField.$(RadioButtonElement.class).last().sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.$(RadioButtonElement.class).last().sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.selectByText("foo");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void detach_attach_preservesInvalidState() {
        // Make field invalid
        $("button").id(SET_INVALID_BUTTON).click();

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
        // Make field invalid
        $("button").id(SET_INVALID_BUTTON).click();

        executeScript("arguments[0].invalid = false", testField);

        assertServerInvalid();
    }

    @Override
    protected RadioButtonGroupElement getTestField() {
        return $(RadioButtonGroupElement.class).first();
    }
}
