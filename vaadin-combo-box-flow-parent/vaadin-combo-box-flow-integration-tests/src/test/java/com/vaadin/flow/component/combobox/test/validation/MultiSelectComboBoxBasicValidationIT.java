package com.vaadin.flow.component.combobox.test.validation;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;
import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.combobox.test.validation.MultiSelectComboBoxBasicValidationPage.ENABLE_CUSTOM_VALUE_BUTTON;
import static com.vaadin.flow.component.combobox.test.validation.MultiSelectComboBoxBasicValidationPage.REQUIRED_BUTTON;

@TestPath("vaadin-multi-select-combo-box/validation/basic")
public class MultiSelectComboBoxBasicValidationIT
        extends AbstractValidationIT<MultiSelectComboBoxElement> {

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

        testField.selectByText("foo");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();

        testField.deselectAll();
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void required_enterCustomValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.sendKeys("custom", Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_customValuesAllowed_enterCustomValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();
        $("button").id(ENABLE_CUSTOM_VALUE_BUTTON).click();

        testField.sendKeys("custom", Keys.TAB);
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();

        testField.deselectAll();
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void detach_attach_preservesInvalidState() {
        // Make field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.selectByText("foo");
        testField.deselectAll();

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
        testField.selectByText("foo");
        testField.deselectAll();

        executeScript("arguments[0].invalid = false", testField);

        assertServerInvalid();
    }

    @Override
    protected MultiSelectComboBoxElement getTestField() {
        return $(MultiSelectComboBoxElement.class).first();
    }
}
