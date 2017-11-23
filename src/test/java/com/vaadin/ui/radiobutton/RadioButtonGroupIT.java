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
package com.vaadin.ui.radiobutton;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.demo.ComponentDemoTest;
import com.vaadin.testbench.By;

public class RadioButtonGroupIT extends ComponentDemoTest {

    @Override
    protected String getTestPath() {
        return "/vaadin-radio-button";
    }

    @Test
    public void valueChange() {
        WebElement valueDiv = layout.findElement(By.id("button-group-value"));
        WebElement group = layout
                .findElement(By.id("button-group-with-value-change-listener"));

        List<WebElement> buttons = group
                .findElements(By.tagName("vaadin-radio-button"));

        buttons.get(1).click();

        waitUntil(
                driver -> "Radio button group value changed from 'null' to 'bar'"
                        .equals(valueDiv.getText()));

        buttons.get(0).click();

        waitUntil(
                driver -> "Radio button group value changed from 'bar' to 'foo'"
                        .equals(valueDiv.getText()));

        buttons.get(0).click();

        waitUntil(
                driver -> "Radio button group value changed from 'bar' to 'foo'"
                        .equals(valueDiv.getText()));
    }

    @Test
    public void itemGenerator() {
        WebElement valueDiv = layout
                .findElement(By.id("button-group-gen-value"));
        WebElement group = layout
                .findElement(By.id("button-group-with-item-generator"));

        List<WebElement> buttons = group
                .findElements(By.tagName("vaadin-radio-button"));

        executeScript("arguments[0].scrollIntoView(true);", group);
        buttons.get(1).click();

        waitUntil(
                driver -> "Radio button group value changed from 'null' to 'John'"
                        .equals(valueDiv.getText()));
    }

    @Test
    public void disabledGroup() {
        WebElement group = layout.findElement(By.id("button-group-disabled"));

        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getAttribute("disabled"));
    }

    @Test
    public void itemRenderer() {
        WebElement valueDiv = layout
                .findElement(By.id("button-group-renderer-value"));
        WebElement group = layout.findElement(By.id("button-group-renderer"));

        List<WebElement> buttons = group
                .findElements(By.tagName("vaadin-radio-button"));

        executeScript("arguments[0].scrollIntoView(true);", group);

        buttons.get(1).click();
        WebElement anchor = buttons.get(0).findElement(By.tagName("a"));

        Assert.assertEquals("http://example.com/1",
                anchor.getAttribute("href"));

        Assert.assertEquals("Joe", anchor.getText());

        waitUntil(
                driver -> "Radio button group value changed from 'null' to 'John'"
                        .equals(valueDiv.getText()));
    }

    @Test
    public void iconGenerator() {
        WebElement group = layout
                .findElement(By.id("button-group-icon-generator"));

        List<WebElement> buttons = group
                .findElements(By.tagName("vaadin-radio-button"));

        WebElement anchor = buttons.get(2).findElement(By.tagName("img"));

        Assert.assertEquals("https://vaadin.com/images/vaadin-logo.svg",
                anchor.getAttribute("src"));

        Assert.assertEquals("Bill", buttons.get(2).getText());
    }

    @Test
    public void disabledGroupItems() {
        WebElement group = layout
                .findElement(By.id("button-group-disabled-items"));

        List<WebElement> buttons = group
                .findElements(By.tagName("vaadin-radio-button"));

        Assert.assertEquals(Boolean.TRUE.toString(),
                buttons.get(1).getAttribute("disabled"));
    }

}
