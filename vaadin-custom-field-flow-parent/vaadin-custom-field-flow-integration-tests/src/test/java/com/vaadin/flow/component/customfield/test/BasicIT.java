package com.vaadin.flow.component.customfield.test;

import com.vaadin.flow.component.customfield.testbench.CustomFieldElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class BasicIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    @Test
    @Ignore
    public void valueIsUpdated() {
        final CustomFieldElement customField = $(CustomFieldElement.class)
            .waitForFirst();
        Assert.assertEquals("",
            customField.findElements(By.tagName("div")).get(0).getText());
        TextFieldElement field1 = getById(customField, "field1");
        field1.setValue("1");
        $("button").waitForFirst().click();
        Assert.assertEquals("0",
            customField.findElements(By.tagName("div")).get(0).getText());
        TextFieldElement field2 = getById(customField, "field2");
        field2.setValue("2");
        Assert.assertEquals("3",
            customField.findElements(By.tagName("div")).get(0).getText());
    }

    private TextFieldElement getById(CustomFieldElement customField,
        String id) {
        return customField.$(TextFieldElement.class).attribute("id", id)
            .waitForFirst();
    }
}
