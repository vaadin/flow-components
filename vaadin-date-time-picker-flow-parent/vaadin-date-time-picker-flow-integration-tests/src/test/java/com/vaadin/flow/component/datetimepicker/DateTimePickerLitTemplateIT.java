package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests for the {@link DateTimePickerLitTemplatePage}.
 */
@TestPath("vaadin-date-time-picker/lit-template")
public class DateTimePickerLitTemplateIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    // Regression test for: https://github.com/vaadin/web-components/issues/3149
    public void testUsingLitTemplatesShouldCorrectlyReplaceSlotContents() {
        TestBenchElement wrapper = $("date-time-picker-lit-template").first();
        DateTimePickerElement dateTimePickerElement = wrapper
                .$(DateTimePickerElement.class).first();

        // should have correctly removed the web component's default slotted
        // children, and added custom slotted children, resulting in one date
        // picker and one time picker
        int numDatePickers = dateTimePickerElement
                .$("vaadin-date-time-picker-date-picker").all().size();
        int numTimePickers = dateTimePickerElement
                .$("vaadin-date-time-picker-time-picker").all().size();

        Assert.assertEquals(1, numDatePickers);
        Assert.assertEquals(1, numTimePickers);
    }
}
