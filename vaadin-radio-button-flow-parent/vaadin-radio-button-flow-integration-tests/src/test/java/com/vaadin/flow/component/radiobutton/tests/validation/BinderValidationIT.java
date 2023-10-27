package com.vaadin.flow.component.radiobutton.tests.validation;

import com.vaadin.flow.component.radiobutton.testbench.RadioButtonElement;
import com.vaadin.flow.component.radiobutton.testbench.RadioButtonGroupElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;
import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.radiobutton.tests.validation.BinderValidationPage.REQUIRED_ERROR_MESSAGE;

@TestPath("vaadin-radio-button-group/validation/binder")
public class BinderValidationIT
        extends AbstractValidationIT<RadioButtonGroupElement> {

    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        testField.$(RadioButtonElement.class).last().sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_changeValue_assertValidity() {
        testField.selectByText("foo");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();
    }

    @Override
    protected RadioButtonGroupElement getTestField() {
        return $(RadioButtonGroupElement.class).first();
    }
}
