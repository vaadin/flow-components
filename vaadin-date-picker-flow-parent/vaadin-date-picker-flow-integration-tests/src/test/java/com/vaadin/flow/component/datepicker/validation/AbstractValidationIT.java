package com.vaadin.flow.component.datepicker.validation;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.tests.AbstractComponentIT;

import org.junit.Assert;
import org.junit.Before;

import static com.vaadin.flow.component.datepicker.validation.AbstractValidationPage.SERVER_VALIDITY_STATE;
import static com.vaadin.flow.component.datepicker.validation.AbstractValidationPage.SERVER_VALIDITY_STATE_BUTTON;

public class AbstractValidationIT extends AbstractComponentIT {
    protected DatePickerElement field;

    @Before
    public void init() {
        open();
        field = $(DatePickerElement.class).first();
    }

    protected void assertErrorMessage(String expected) {
        Assert.assertEquals(expected, field.getPropertyString("errorMessage"));
    }

    protected void assertClientValid(boolean expected) {
        Assert.assertEquals(expected, !field.getPropertyBoolean("invalid"));
    }

    protected void assertServerValid(boolean expected) {
        $("button").id(SERVER_VALIDITY_STATE_BUTTON).click();

        var actual = $("div").id(SERVER_VALIDITY_STATE).getText();
        Assert.assertEquals(String.valueOf(expected), actual);
    }
}
