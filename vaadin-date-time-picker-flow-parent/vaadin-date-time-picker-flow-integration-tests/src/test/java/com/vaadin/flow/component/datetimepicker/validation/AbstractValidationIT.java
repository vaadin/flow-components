package com.vaadin.flow.component.datetimepicker.validation;

import org.junit.Assert;
import org.junit.Before;

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

import static com.vaadin.flow.component.datetimepicker.validation.AbstractValidationPage.SERVER_VALIDITY_STATE;
import static com.vaadin.flow.component.datetimepicker.validation.AbstractValidationPage.SERVER_VALIDITY_STATE_BUTTON;

public class AbstractValidationIT extends AbstractComponentIT {
    protected DateTimePickerElement field;

    @Before
    public void init() {
        open();
        field = $(DateTimePickerElement.class).first();
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

    protected TestBenchElement getDateInputElement() {
        return field.$("input").first();
    }

    protected TestBenchElement getTimeInputElement() {
        return field.$("input").last();
    }
}
