package com.vaadin.flow.component.select.tests.validation;

import com.vaadin.flow.component.select.testbench.SelectElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;
import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.select.tests.validation.BasicValidationPage.ATTACH_FIELD_BUTTON;
import static com.vaadin.flow.component.select.tests.validation.BasicValidationPage.DETACH_FIELD_BUTTON;
import static com.vaadin.flow.component.select.tests.validation.BasicValidationPage.REQUIRED_BUTTON;

@TestPath("vaadin-select/validation/basic")
public class BasicValidationIT extends AbstractValidationIT<SelectElement> {

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
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.selectByText("foo");
        assertServerValid();
        assertClientValid();

        testField.selectByText("");
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void detach_attach_preservesInvalidState() {
        // Make field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.sendKeys(Keys.TAB);

        $("button").id(DETACH_FIELD_BUTTON).click();
        $("button").id(ATTACH_FIELD_BUTTON).click();
        testField = getTestField();

        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void preventWebComponentFromChangingInvalidState() {
        assertWebComponentCanNotModifyInvalidState();

        $("button").id(DETACH_FIELD_BUTTON).click();
        $("button").id(ATTACH_FIELD_BUTTON).click();
        testField = getTestField();

        assertWebComponentCanNotModifyInvalidState();
    }

    @Override
    protected SelectElement getTestField() {
        return $(SelectElement.class).first();
    }
}
