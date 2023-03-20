
package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.Keys;

/**
 * Integration tests for attaching / detaching date time picker.
 */
@TestPath("vaadin-date-time-picker/date-time-picker-detach-attach")
public class DateTimePickerDetachAttachPageIT extends AbstractComponentIT {

    @Test
    public void clientSideValidationIsOverriddenOnAttach() {
        open();

        assertDateTimePickerIsValidOnTab();

        // Detaching and attaching date time picker
        TestBenchElement toggleAttach = $("button").id("toggle-attached");
        toggleAttach.click();
        toggleAttach.click();

        assertDateTimePickerIsValidOnTab();
    }

    private void assertDateTimePickerIsValidOnTab() {
        DateTimePickerElement dateTimePicker = $(DateTimePickerElement.class)
                .first();
        dateTimePicker.sendKeys(Keys.TAB);
        Assert.assertFalse("Date time picker should be valid after Tab",
                dateTimePicker.getPropertyBoolean("invalid"));
    }
}
