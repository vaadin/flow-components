/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.radiobutton.tests;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-radio-button/radio-button-group-required-binder")
public class RequiredValidationIT extends AbstractComponentIT {

    @Test
    public void requiredValidation_disabledWithBinder_enabledViaExpicitCall()
            throws InterruptedException {
        open();

        WebElement group = findElement(By.id("gender"));
        $("vaadin-radio-button").first().sendKeys(Keys.TAB);
        Assert.assertFalse("Radio button group should be valid.",
                Boolean.parseBoolean(group.getAttribute("invalid")));

        findElement(By.id("hide")).click();

        Assert.assertTrue("Radio button group should be invalid",
                Boolean.parseBoolean(group.getAttribute("invalid")));
    }

    @Test
    public void groupWithInvalidOption() {
        open();

        WebElement group = findElement(
                By.id("radio-button-with-invalid-option"));
        WebElement radioButton = group
                .findElements(By.tagName("vaadin-radio-button")).get(2);

        Assert.assertFalse("Radio button group should be valid.",
                Boolean.parseBoolean(group.getAttribute("invalid")));
        radioButton.click();

        Assert.assertTrue("Radio button group should be invalid.",
                Boolean.parseBoolean(group.getAttribute("invalid")));

        radioButton.sendKeys(Keys.TAB);
        Assert.assertTrue("Radio button group should keep invalid.",
                Boolean.parseBoolean(group.getAttribute("invalid")));
    }

    @Test
    public void groupInvalidOnAttach() {
        open();

        WebElement group = findElement(By.id("radio-button-invalid-on-attach"));

        Assert.assertTrue("Radio button group should be invalid.",
                Boolean.parseBoolean(group.getAttribute("invalid")));
    }

}
