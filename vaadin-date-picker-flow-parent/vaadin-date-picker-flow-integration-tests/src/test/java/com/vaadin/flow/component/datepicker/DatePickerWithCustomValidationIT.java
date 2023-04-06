
package com.vaadin.flow.component.datepicker;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.Keys;

@TestPath("vaadin-date-picker/required-field-custom-validator")
public class DatePickerWithCustomValidationIT extends AbstractComponentIT {

    @Test
    public void requiredAndCustomValidationOnServerSide_initialStateIsInvalid_changingToValidValueResetsInvalidFlag()
            throws Exception {
        open();

        TestBenchElement dateField = $("vaadin-date-picker").first();
        TestBenchElement input = dateField.$("input").first();

        Assert.assertEquals(Boolean.TRUE.toString(),
                dateField.getAttribute("invalid"));
        Assert.assertEquals("2019-01-02", dateField.getAttribute("value"));

        while (!input.getAttribute("value").isEmpty()) {
            input.sendKeys(Keys.BACK_SPACE);
        }
        input.sendKeys(Keys.ENTER);
        Assert.assertEquals("", dateField.getAttribute("value"));

        input.sendKeys("01/01/2019");
        input.sendKeys(Keys.ENTER);

        Assert.assertEquals("2019-01-01", dateField.getAttribute("value"));
        Assert.assertEquals(Boolean.FALSE.toString(),
                dateField.getAttribute("invalid"));
    }
}
