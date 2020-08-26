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
package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.tests.ComponentDemoTest;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Integration tests for the {@link CheckboxDemoPage}.
 */
public class CheckboxIT extends ComponentDemoTest {

    @Test
    public void defaultCheckbox() {
        WebElement checkbox = layout.findElement(By.id("default-checkbox"));
        Assert.assertTrue("Default checkbox should be present",
                "vaadin-checkbox".equals(checkbox.getTagName()));
        Assert.assertEquals(
                "Default checkbox label should have text 'Default Checkbox'",
                "Default Checkbox", checkbox.getText());
    }

    @Test
    public void disabledCheckbox() {
        WebElement checkbox = layout.findElement(By.id("disabled-checkbox"));
        Assert.assertEquals("true", checkbox.getAttribute("disabled"));
        WebElement message = layout
                .findElement(By.id("disabled-checkbox-message"));
        Assert.assertEquals("", message.getText());

        executeScript("arguments[0].removeAttribute(\"disabled\");"
                + "arguments[0].click();", checkbox);
        message = layout.findElement(By.id("disabled-checkbox-message"));
        Assert.assertEquals("", message.getText());
    }

    @Test
    public void indeterminateCheckbox() {
        WebElement checkbox = layout
                .findElement(By.id("indeterminate-checkbox"));
        WebElement button = layout.findElement(By.id("reset-indeterminate"));
        Assert.assertEquals("This checkbox should be in indeterminate state",
                "true", checkbox.getAttribute("indeterminate"));

        checkbox.click();
        Assert.assertNotEquals(
                "Checkbox should not be in indeterminate state after clicking it",
                "true", checkbox.getAttribute("indeterminate"));

        clickElementWithJs(button);
        Assert.assertEquals(
                "This checkbox should be in indeterminate state after resetting",
                "true", checkbox.getAttribute("indeterminate"));
    }

    @Test
    public void valueChangeCheckbox() {
        WebElement checkbox = layout
                .findElement(By.id("value-change-checkbox"));
        WebElement message = layout
                .findElement(By.id("value-change-checkbox-message"));
        checkbox.click();
        Assert.assertEquals("Clicking checkbox should update message div",
                "Checkbox value changed from 'false' to 'true'",
                message.getText());
    }

    @Test
    public void accessibleCheckbox() {
        WebElement checkbox = layout.findElement(By.id("accessible-checkbox"));
        Assert.assertEquals(
                "Accessible checkbox should have the aria-label attribute",
                "Click me", checkbox.getAttribute("aria-label"));
    }

    @Test
    public void htmlCheckbox() {
        WebElement checkbox = layout.findElement(By.id("html-checkbox"));
        WebElement anchor = checkbox.findElement(By.tagName("a"));
        Assert.assertEquals(
                "Content should contain a link to vaadin.com",
                "https://vaadin.com/privacy-policy", anchor.getAttribute("href"));
    }

    @Override
    protected String getTestPath() {
        return ("/vaadin-checkbox-test-demo");
    }
}
