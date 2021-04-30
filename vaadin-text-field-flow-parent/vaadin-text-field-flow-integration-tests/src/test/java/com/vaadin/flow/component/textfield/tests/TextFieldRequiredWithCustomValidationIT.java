package com.vaadin.flow.component.textfield.tests;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.Keys;

@TestPath("vaadin-text-field/required-field-custom-validator")
public class TextFieldRequiredWithCustomValidationIT extends AbstractComponentIT {

    @Test
    public void requiredAndCustomValidationOnServerSide_initialStateIsInvalid_changingToValidValueResetsInvalidFlag() throws Exception {
        open();

        TestBenchElement textField = $("vaadin-text-field").first();
        TestBenchElement input = textField.$("input").first();

        Assert.assertEquals(Boolean.TRUE.toString(), textField.getAttribute("invalid"));
        Assert.assertEquals("invalid", textField.getAttribute("value"));

        while (!input.getAttribute("value").isEmpty()) {
            input.sendKeys(Keys.BACK_SPACE);
        }
        Assert.assertEquals("", textField.getAttribute("value"));

        input.sendKeys("Valid");
        input.sendKeys(Keys.ENTER);

        Assert.assertEquals("Valid", textField.getAttribute("value"));
        Assert.assertEquals(Boolean.FALSE.toString(), textField.getAttribute("invalid"));
    }
}
