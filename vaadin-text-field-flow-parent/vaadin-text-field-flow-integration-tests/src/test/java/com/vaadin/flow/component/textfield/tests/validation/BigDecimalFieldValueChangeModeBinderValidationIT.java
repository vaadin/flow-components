/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests.validation;

import static com.vaadin.flow.component.textfield.tests.validation.BigDecimalFieldValueChangeModeBasicValidationPage.SET_EAGER_MODE_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.BigDecimalFieldValueChangeModeBasicValidationPage.SET_LAZY_MODE_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.BigDecimalFieldValueChangeModeBasicValidationPage.SET_TIMEOUT_MODE_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.BigDecimalFieldValueChangeModeBasicValidationPage.VALUE_CHANGE_TIMEOUT;

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.textfield.testbench.BigDecimalFieldElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-big-decimal-field/validation/value-change-mode/binder")
public class BigDecimalFieldValueChangeModeBinderValidationIT
        extends AbstractValueChangeModeValidationIT<BigDecimalFieldElement> {
    @Test
    public void eagerMode_enterChars_assertValidity() {
        $("button").id(SET_EAGER_MODE_BUTTON).click();

        // Entered: 2
        testField.sendKeys("2");
        assertValidationResults("valid");

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("valid");

        // Entered: -
        testField.sendKeys("-");
        assertValidationResults("invalid");

        // Entered: -2
        testField.sendKeys("2");
        assertValidationResults("valid");

        // Entered: -
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("invalid");

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("valid");
    }

    @Test
    public void lazyMode_enterOneChar_assertValidityAndTimeout() {
        $("button").id(SET_LAZY_MODE_BUTTON).click();

        // Entered: 2
        startValidationTimeout();
        testField.sendKeys("2");
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid");

        // Entered:
        startValidationTimeout();
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid");

        // Entered: -
        startValidationTimeout();
        testField.sendKeys("-");
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("invalid");

        // Entered: -2
        startValidationTimeout();
        testField.sendKeys("2");
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid");

        // Entered: -
        startValidationTimeout();
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("invalid");

        // Entered:
        startValidationTimeout();
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid");
    }

    @Test
    public void lazyMode_enterMultipleChars_assertValidityAndTimeout() {
        $("button").id(SET_LAZY_MODE_BUTTON).click();

        // Entered: -2
        startValidationTimeout();
        testField.sendKeys("-2");
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid");

        // Entered:
        startValidationTimeout();
        testField.sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE);
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid");

        // Entered: --
        startValidationTimeout();
        testField.sendKeys("--");
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("invalid");

        // Entered: -2
        startValidationTimeout();
        testField.sendKeys(Keys.BACK_SPACE, "2");
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid");

        // Entered: --
        startValidationTimeout();
        testField.sendKeys(Keys.BACK_SPACE, "-");
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("invalid");

        // Entered:
        startValidationTimeout();
        testField.sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE);
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid");
    }

    @Test
    public void timeoutMode_enterOneChar_assertValidity()
            throws InterruptedException {
        $("button").id(SET_TIMEOUT_MODE_BUTTON).click();

        // Entered: 2
        testField.sendKeys("2");
        assertValidationResults("valid");

        Thread.sleep(VALUE_CHANGE_TIMEOUT);

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("valid");

        Thread.sleep(VALUE_CHANGE_TIMEOUT);

        // Entered: -
        testField.sendKeys("-");
        assertValidationResults("invalid");

        Thread.sleep(VALUE_CHANGE_TIMEOUT);

        // Entered: -2
        testField.sendKeys("2");
        assertValidationResults("valid");

        Thread.sleep(VALUE_CHANGE_TIMEOUT);

        // Entered: -
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("invalid");

        Thread.sleep(VALUE_CHANGE_TIMEOUT);

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("valid");
    }

    @Test
    public void timeoutMode_enterMultipleChars_assertValidityAndTimeout()
            throws InterruptedException {
        $("button").id(SET_TIMEOUT_MODE_BUTTON).click();

        // Entered: -2
        startValidationTimeout();
        testField.sendKeys("-");
        assertValidationResults("invalid");
        testField.sendKeys("2");
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid");

        // Entered:
        startValidationTimeout();
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("invalid");
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid");

        // Entered: --
        startValidationTimeout();
        testField.sendKeys("-");
        assertValidationResults("invalid");
        testField.sendKeys("-");
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("invalid");

        // Entered: -2
        startValidationTimeout();
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("invalid");
        testField.sendKeys("2");
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid");

        // Entered: --
        startValidationTimeout();
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("invalid");
        testField.sendKeys("-");
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("invalid");

        // Entered:
        startValidationTimeout();
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("invalid");
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid");
    }

    @Override
    protected BigDecimalFieldElement getTestField() {
        return $(BigDecimalFieldElement.class).first();
    }
}
