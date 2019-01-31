package com.vaadin.flow.component.customfield.test;

import com.vaadin.flow.component.customfield.testbench.CustomFieldElement;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

public class BasicIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    @Test
    public void valueIsUpdated() {
        final CustomFieldElement customField = $(CustomFieldElement.class)
            .first();
        Assert.assertEquals("",
            customField.findElements(By.tagName("div")).get(0).getText());
        getById(customField, "field1").sendKeys("1\t");
        //getById(customField, "field2").click();
        Assert.assertEquals("0",
            customField.findElements(By.tagName("div")).get(0).getText());
        getById(customField, "field2").sendKeys("2\t");
        //getById(customField, "field1").click();
        Assert.assertEquals("3",
            customField.findElements(By.tagName("div")).get(0).getText());
    }

    private TextFieldElement getById(CustomFieldElement customField,
        String id) {
        return customField.$(TextFieldElement.class).attribute("id", id)
            .first();
    }
}
