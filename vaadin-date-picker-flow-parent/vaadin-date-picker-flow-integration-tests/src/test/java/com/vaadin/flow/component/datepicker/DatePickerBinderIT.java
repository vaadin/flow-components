
package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Collections;

/**
 * Integration tests for the {@link BinderValidationView}.
 */
@TestPath("vaadin-date-picker/binder-validation")
public class DatePickerBinderIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void selectDateOnSimpleDatePicker() {
        final DatePickerElement element = $(DatePickerElement.class)
                .waitForFirst();
        Assert.assertFalse(element.getPropertyBoolean("invalid"));
    }

    private void setInternalValidBinderInvalidValue(DatePickerElement field) {
        field.setDate(LocalDate.of(2019, 1, 2));
        field.dispatchEvent("change",
                Collections.singletonMap("bubbles", true));
        field.dispatchEvent("blur");
    }

    @Test
    public void dateField_internalValidationPass_binderValidationFail_fieldInvalid() {
        DatePickerElement field = $(DatePickerElement.class).first();
        setInternalValidBinderInvalidValue(field);
        assertInvalid(field);
    }

    @Test
    public void dateField_internalValidationPass_binderValidationFail_validateClient_fieldInvalid() {
        DatePickerElement field = $(DatePickerElement.class).first();

        setInternalValidBinderInvalidValue(field);

        field.getCommandExecutor().executeScript(
                "arguments[0].validate(); arguments[0].immediateInvalid = arguments[0].invalid;",
                field);

        assertInvalid(field);
        // State before server roundtrip (avoid flash of valid
        // state)
        Assert.assertTrue("Unexpected immediateInvalid state",
                field.getPropertyBoolean("immediateInvalid"));
    }

    @Test
    public void dateField_internalValidationPass_binderValidationFail_setClientValid_serverFieldInvalid() {
        DatePickerElement field = $(DatePickerElement.class).first();

        setInternalValidBinderInvalidValue(field);

        field.getCommandExecutor().executeScript("arguments[0].invalid = false",
                field);

        Assert.assertEquals(field.getPropertyString("label"), "invalid");
    }

    private void assertInvalid(DatePickerElement field) {
        Assert.assertTrue("Unexpected invalid state",
                field.getPropertyBoolean("invalid"));
        Assert.assertEquals(
                "Expected to have error message configured in the Binder Validator",
                BinderValidationView.BINDER_ERROR_MSG,
                field.getPropertyString("errorMessage"));
    }
}
