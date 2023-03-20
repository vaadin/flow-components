package com.vaadin.flow.component.textfield.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.textfield.testbench.BigDecimalFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

import static com.vaadin.flow.component.textfield.tests.BigDecimalFieldClearValuePage.CLEAR_BUTTON;
import static com.vaadin.flow.component.textfield.tests.BigDecimalFieldClearValuePage.CLEAR_AND_SET_VALUE_BUTTON;

@TestPath("vaadin-big-decimal-field/clear-value")
public class BigDecimalFieldClearValueIT extends AbstractComponentIT {
    private BigDecimalFieldElement bigDecimalField;

    private TestBenchElement input;

    @Before
    public void init() {
        open();
        bigDecimalField = $(BigDecimalFieldElement.class).first();
        input = bigDecimalField.$("input").first();
    }

    @Test
    public void setInputValue_clearValue_inputValueIsEmpty() {
        bigDecimalField.sendKeys("1234", Keys.ENTER);
        Assert.assertEquals("1234", input.getPropertyString("value"));

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", input.getPropertyString("value"));
    }

    @Test
    public void badInput_setInputValue_clearValue_inputValueIsEmpty() {
        bigDecimalField.sendKeys("--2", Keys.ENTER);
        Assert.assertEquals("--2", input.getPropertyString("value"));

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", input.getPropertyString("value"));
    }

    @Test
    public void badInput_setInputValue_clearAndSetValue_inputValueIsPresent() {
        bigDecimalField.sendKeys("--2", Keys.ENTER);
        $("button").id(CLEAR_AND_SET_VALUE_BUTTON).click();
        Assert.assertEquals("1234", input.getPropertyString("value"));
    }
}
