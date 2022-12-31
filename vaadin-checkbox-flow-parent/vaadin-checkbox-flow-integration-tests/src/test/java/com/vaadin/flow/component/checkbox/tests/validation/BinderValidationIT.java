package com.vaadin.flow.component.checkbox.tests.validation;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.component.checkbox.testbench.CheckboxGroupElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;
import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.checkbox.tests.validation.BinderValidationPage.REQUIRED_ERROR_MESSAGE;

@TestPath("vaadin-checkbox-group/validation/binder")
public class BinderValidationIT
        extends AbstractValidationIT<CheckboxGroupElement> {

    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        testField.$(CheckboxElement.class).last().sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void required_changeValue_assertValidity() {
        testField.selectByText("foo");
        assertServerValid();
        assertClientValid();

        testField.deselectByText("foo");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Override
    protected CheckboxGroupElement getTestField() {
        return $(CheckboxGroupElement.class).first();
    }
}
