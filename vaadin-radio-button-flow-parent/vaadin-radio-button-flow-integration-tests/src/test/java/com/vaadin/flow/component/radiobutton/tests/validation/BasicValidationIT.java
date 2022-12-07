package com.vaadin.flow.component.radiobutton.tests.validation;

import com.vaadin.flow.component.radiobutton.testbench.RadioButtonElement;
import com.vaadin.flow.component.radiobutton.testbench.RadioButtonGroupElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;
import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.radiobutton.tests.validation.BasicValidationPage.ATTACH_FIELD_BUTTON;
import static com.vaadin.flow.component.radiobutton.tests.validation.BasicValidationPage.DETACH_FIELD_BUTTON;
import static com.vaadin.flow.component.radiobutton.tests.validation.BasicValidationPage.REQUIRED_BUTTON;

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
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.$(RadioButtonElement.class).last().sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.selectByText("foo");
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void detach_attach_preservesInvalidState() {
        // Make field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.$(RadioButtonElement.class).last().sendKeys(Keys.TAB);

        $("button").id(DETACH_FIELD_BUTTON).click();
        $("button").id(ATTACH_FIELD_BUTTON).click();
        testField = getTestField();

        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void webComponentCanNotModifyInvalidState() {
        assertWebComponentCanNotModifyInvalidState();

        $("button").id(DETACH_FIELD_BUTTON).click();
        $("button").id(ATTACH_FIELD_BUTTON).click();
        testField = getTestField();

        assertWebComponentCanNotModifyInvalidState();
    }

    @Override
    protected RadioButtonGroupElement getTestField() {
        return $(RadioButtonGroupElement.class).first();
    }
}
