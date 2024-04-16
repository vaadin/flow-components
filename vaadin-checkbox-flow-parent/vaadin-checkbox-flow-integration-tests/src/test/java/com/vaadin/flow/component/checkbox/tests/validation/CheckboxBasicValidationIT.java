package com.vaadin.flow.component.checkbox.tests.validation;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;
import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.checkbox.tests.validation.BasicValidationPage.REQUIRED_BUTTON;

@TestPath("vaadin-checkbox/validation/basic")
public class CheckboxBasicValidationIT
        extends AbstractValidationIT<CheckboxElement> {

    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void triggerBlur_assertValidity() {
        testField.sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.setChecked(true);
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();

        testField.setChecked(false);
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void detach_attach_preservesInvalidState() {
        // Make the field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.setChecked(true);
        testField.setChecked(false);

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
        testField.setChecked(true);
        testField.setChecked(false);

        executeScript("arguments[0].invalid = false", testField);

        assertServerInvalid();
    }

    @Override
    protected CheckboxElement getTestField() {
        return $(CheckboxElement.class).first();
    }
}
