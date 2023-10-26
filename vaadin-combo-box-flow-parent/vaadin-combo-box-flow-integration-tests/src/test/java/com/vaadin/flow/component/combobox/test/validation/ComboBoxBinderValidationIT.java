package com.vaadin.flow.component.combobox.test.validation;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;
import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.combobox.test.validation.ComboBoxBinderValidationPage.ENABLE_CUSTOM_VALUE_BUTTON;
import static com.vaadin.flow.component.combobox.test.validation.ComboBoxBinderValidationPage.EXPECTED_VALUE_INPUT;
import static com.vaadin.flow.component.combobox.test.validation.ComboBoxBinderValidationPage.REQUIRED_ERROR_MESSAGE;
import static com.vaadin.flow.component.combobox.test.validation.ComboBoxBinderValidationPage.UNEXPECTED_VALUE_ERROR_MESSAGE;

@TestPath("vaadin-combo-box/validation/binder")
public class ComboBoxBinderValidationIT
        extends AbstractValidationIT<ComboBoxElement> {

    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        testField.sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("foo", Keys.ENTER);

        // Binder validation fails
        testField.selectByText("bar");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Binder validation passes
        testField.selectByText("foo");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();

        // Required fails
        testField.clear();
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void required_enterCustomValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("foo", Keys.ENTER);

        testField.sendKeys("custom", Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_customValuesAllowed_enterCustomValue_assertValidity() {
        $("button").id(ENABLE_CUSTOM_VALUE_BUTTON).click();
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("custom", Keys.ENTER);

        // Binder validation fails
        testField.sendKeys("invalid", Keys.TAB);
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Binder validation passes
        testField.clear();
        assertValidationCount(1);
        testField.sendKeys("custom", Keys.TAB);
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();

        // Required fails
        testField.clear();
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Override
    protected ComboBoxElement getTestField() {
        return $(ComboBoxElement.class).first();
    }
}
