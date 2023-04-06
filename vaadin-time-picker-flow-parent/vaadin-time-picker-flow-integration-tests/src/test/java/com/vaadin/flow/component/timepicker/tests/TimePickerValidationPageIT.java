
package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.timepicker.testbench.TimePickerElement;
import com.vaadin.tests.AbstractValidationTest;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

/**
 * Integration tests for {@link TimePicker} validation.
 */
@TestPath("vaadin-time-picker/time-picker-validation")
public class TimePickerValidationPageIT extends AbstractValidationTest {

    @Test
    public void assertInvalidAfterClientChangeMax() {
        // max is 17:00
        final String invalidTime = "17:01";
        final String validTime = "17:00";
        assertInvalidAfterClientChange("max", invalidTime, validTime);
    }

    @Test
    public void assertInvalidAfterClientChangeMin() {
        // min is 9:30
        final String invalidTime = "9:29";
        final String validTime = "9:30";
        assertInvalidAfterClientChange("min", invalidTime, validTime);
    }

    private void assertInvalidAfterClientChange(String clientPropertyUnderTest,
            String invalidValue, String validValue) {

        final boolean valid = true;
        final TimePickerElement element = $(TimePickerElement.class)
                .id("picker-with-valid-range");
        assertValidStateOfPickerWithValidRange(valid);

        element.setValue(invalidValue);
        assertValidStateOfPickerWithValidRange(!valid);

        // Forcing max to invalid value on the client does not make the field
        // valid
        element.setProperty(clientPropertyUnderTest, invalidValue);
        getCommandExecutor().waitForVaadin();
        assertValidStateOfPickerWithValidRange(!valid);

        // Forcing the field to be valid does not work
        element.setProperty("invalid", false);
        getCommandExecutor().waitForVaadin();
        assertValidStateOfPickerWithValidRange(!valid);

        // Setting a valid value makes the field return to valid mode
        element.setValue(validValue);
        getCommandExecutor().waitForVaadin();
        assertValidStateOfPickerWithValidRange(valid);
    }

    private void assertValidStateOfPickerWithValidRange(boolean valid) {
        final WebElement checkIsInvalid = $("button").id("check-is-invalid");
        checkIsInvalid.click();

        final String expectedValue = !valid ? "invalid" : "valid";
        Assert.assertEquals(expectedValue, $("div").id("is-invalid").getText());
    }

}
