package com.vaadin.flow.component.timepicker.tests.validation;

import org.junit.Assert;
import org.junit.Before;

import com.vaadin.flow.component.timepicker.testbench.TimePickerElement;
import com.vaadin.tests.AbstractComponentIT;

import static com.vaadin.flow.component.timepicker.tests.validation.AbstractValidationPage.SERVER_VALIDITY_STATE;
import static com.vaadin.flow.component.timepicker.tests.validation.AbstractValidationPage.SERVER_VALIDITY_STATE_BUTTON;

public abstract class AbstractValidationIT extends AbstractComponentIT {
    protected TimePickerElement field;

    @Before
    public void init() {
        open();
        field = $(TimePickerElement.class).first();
    }

    protected void assertErrorMessage(String expected) {
        Assert.assertEquals(expected, field.getPropertyString("errorMessage"));
    }

    protected void assertClientValid(boolean expected) {
        Assert.assertEquals(expected, !field.getPropertyBoolean("invalid"));
    }

    protected void assertServerValid(boolean expected) {
        $("button").id(SERVER_VALIDITY_STATE_BUTTON).click();

        String actual = $("div").id(SERVER_VALIDITY_STATE).getText();
        Assert.assertEquals(String.valueOf(expected), actual);
    }
}
