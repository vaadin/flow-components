package com.vaadin.flow.component.select.tests.validation;

import com.vaadin.flow.component.select.testbench.SelectElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;
import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.select.tests.validation.BinderValidationPage.REQUIRED_ERROR_MESSAGE;

@TestPath("vaadin-select/validation/binder")
public class BinderValidationIT extends AbstractValidationIT<SelectElement> {

    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        testField.sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void required_changeValue_assertValidity() {
        testField.selectByText("foo");
        assertServerValid();
        assertClientValid();

        testField.selectByText("");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Override
    protected SelectElement getTestField() {
        return $(SelectElement.class).first();
    }
}
