/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests.validation;

import static com.vaadin.flow.component.textfield.tests.validation.TextFieldValidationBasicPage.ATTACH_FIELD_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.TextFieldValidationBasicPage.DETACH_FIELD_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.TextFieldValidationBasicPage.MAX_LENGTH_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.TextFieldValidationBasicPage.MIN_LENGTH_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.TextFieldValidationBasicPage.PATTERN_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.TextFieldValidationBasicPage.REQUIRED_BUTTON;

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

@TestPath("vaadin-text-field/validation/basic")
public class TextFieldValidationBasicIT
        extends AbstractValidationIT<TextFieldElement> {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void onlyServerCanSetFieldToValid() {
        $("button").id(REQUIRED_BUTTON).click();

        executeScript("arguments[0].validate()", testField);
        assertClientInvalid();

        testField.sendKeys("Value");
        executeScript("arguments[0].validate()", testField);
        assertClientInvalid();

        testField.sendKeys(Keys.ENTER);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void detach_attach_onlyServerCanSetFieldToValid() {
        $("button").id(DETACH_FIELD_BUTTON).click();
        $("button").id(ATTACH_FIELD_BUTTON).click();

        testField = getTestField();

        onlyServerCanSetFieldToValid();
    }

    @Test
    public void required_triggerInputBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void required_changeInputValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.setValue("Value");
        assertServerValid();
        assertClientValid();

        testField.setValue("");
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void minLength_changeInputValue_assertValidity() {
        $("input").id(MIN_LENGTH_INPUT).sendKeys("2", Keys.ENTER);

        testField.setValue("A");
        assertClientInvalid();
        assertServerInvalid();

        testField.setValue("AA");
        assertClientValid();
        assertServerValid();

        testField.setValue("AAA");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void maxLength_changeInputValue_assertValidity() {
        $("input").id(MAX_LENGTH_INPUT).sendKeys("2", Keys.ENTER);

        testField.setValue("AAA");
        assertClientInvalid();
        assertServerInvalid();

        testField.setValue("AA");
        assertClientValid();
        assertServerValid();

        testField.setValue("A");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void pattern_changeInputValue_assertValidity() {
        $("input").id(PATTERN_INPUT).sendKeys("^\\d+$", Keys.ENTER);

        testField.setValue("Word");
        assertClientInvalid();
        assertServerInvalid();

        testField.setValue("1234");
        assertClientValid();
        assertServerValid();
    }

    protected TextFieldElement getTestField() {
        return $(TextFieldElement.class).first();
    }
}
