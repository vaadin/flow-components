/*
 * Copyright 2000-2017 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.textfield.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

/**
 * Integration tests for {@link TextField}.
 */
@TestPath("text-field-test")
public class TextFieldPageIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void assertReadOnly() {
        WebElement webComponent = findElement(By.tagName("vaadin-text-field"));

        Assert.assertNull(webComponent.getAttribute("readonly"));

        WebElement button = findElement(By.id("read-only"));
        button.click();

        waitUntil(
                driver -> "true".equals(getProperty(webComponent, "readonly")));

        button.click();

        waitUntil(driver -> "false"
                .equals(getProperty(webComponent, "readonly")));
    }

    @Test
    public void assertRequired() {
        WebElement webComponent = findElement(By.tagName("vaadin-text-field"));

        Assert.assertNull(webComponent.getAttribute("required"));

        WebElement button = findElement(By.id("required"));
        button.click();
        waitUntil(
                driver -> "true".equals(getProperty(webComponent, "required")));

        button.click();
        waitUntil(driver -> "false"
                .equals(getProperty(webComponent, "required")));
    }

    @Test
    public void assertValueWithoutListener() throws InterruptedException {
        WebElement field = findElement(By.id("value-change"));

        WebElement input = getInShadowRoot(field, By.cssSelector("input"));
        input.sendKeys("foo");
        blur();

        WebElement button = findElement(By.id("get-value"));
        new Actions(getDriver())
                .moveToElement(button, button.getSize().getWidth() / 2,
                        button.getSize().getHeight() / 2)
                .click().build().perform();

        String value = findElement(By.className("text-field-value")).getText();
        Assert.assertEquals("foo", value);
    }
}
