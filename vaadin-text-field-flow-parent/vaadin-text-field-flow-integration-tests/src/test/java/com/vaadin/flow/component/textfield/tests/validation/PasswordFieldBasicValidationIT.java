/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests.validation;

import static com.vaadin.flow.component.textfield.tests.validation.PasswordFieldBasicValidationPage.MAX_LENGTH_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.PasswordFieldBasicValidationPage.MIN_LENGTH_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.PasswordFieldBasicValidationPage.PATTERN_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.PasswordFieldBasicValidationPage.REQUIRED_BUTTON;

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.textfield.testbench.PasswordFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

@TestPath("vaadin-password-field/validation/basic")
public class PasswordFieldBasicValidationIT
        extends AbstractValidationIT<PasswordFieldElement> {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void triggerBlur_assertValidity() {
        // Tab to the show button
        testField.sendKeys(Keys.TAB);
        // Tab out of the field
        testField.sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        // Tab to the show button
        testField.sendKeys(Keys.TAB);
        // Tab out of the field
        testField.sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.setValue("Value");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();

        testField.setValue("");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void minLength_changeValue_assertValidity() {
        $("input").id(MIN_LENGTH_INPUT).sendKeys("2", Keys.ENTER);

        testField.setValue("A");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();

        testField.setValue("AA");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();

        testField.setValue("AAA");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void maxLength_changeValue_assertValidity() {
        $("input").id(MAX_LENGTH_INPUT).sendKeys("2", Keys.ENTER);

        testField.setValue("AAA");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();

        testField.setValue("AA");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();

        testField.setValue("A");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void pattern_changeValue_assertValidity() {
        $("input").id(PATTERN_INPUT).sendKeys("^\\d+$", Keys.ENTER);

        testField.setValue("Word");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();

        testField.setValue("1234");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void detach_attach_preservesInvalidState() {
        // Make field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.setValue("Value");
        testField.setValue("");

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
        testField.setValue("Value");
        testField.setValue("");

        executeScript("arguments[0].invalid = false", testField);

        assertServerInvalid();
    }

    @Override
    protected PasswordFieldElement getTestField() {
        return $(PasswordFieldElement.class).first();
    }
}
