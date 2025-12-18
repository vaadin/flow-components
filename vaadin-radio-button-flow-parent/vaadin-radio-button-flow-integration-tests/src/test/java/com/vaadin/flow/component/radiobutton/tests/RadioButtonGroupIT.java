/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.radiobutton.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.radiobutton.testbench.RadioButtonElement;
import com.vaadin.flow.component.radiobutton.testbench.RadioButtonGroupElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-radio-button-group-test-demo")
public class RadioButtonGroupIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void valueChange() {
        WebElement valueDiv = findElement(By.id("button-group-value"));
        WebElement group = findElement(
                By.id("button-group-with-value-change-listener"));

        executeScript("arguments[0].value=2;", group);

        waitUntil(
                driver -> "Radio button group value changed from 'null' to 'bar'"
                        .equals(valueDiv.getText()));

        executeScript("arguments[0].value=1;", group);

        waitUntil(
                driver -> "Radio button group value changed from 'bar' to 'foo'"
                        .equals(valueDiv.getText()));
    }

    @Test
    public void itemGenerator() {
        WebElement valueDiv = findElement(By.id("button-group-gen-value"));
        WebElement group = findElement(
                By.id("button-group-with-item-generator"));

        executeScript("arguments[0].value=2;", group);

        waitUntil(
                driver -> "Radio button group value changed from 'null' to 'John'"
                        .equals(valueDiv.getText()));
    }

    @Test
    public void disabledGroup() {
        WebElement group = findElement(By.id("button-group-disabled"));

        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getDomAttribute("disabled"));
    }

    @Test
    public void itemRenderer() {
        WebElement valueDiv = findElement(By.id("button-group-renderer-value"));
        WebElement group = findElement(By.id("button-group-renderer"));

        List<WebElement> buttons = group
                .findElements(By.tagName("vaadin-radio-button"));

        executeScript("arguments[0].value=2;", group);
        WebElement anchor = buttons.get(0).findElement(By.tagName("a"));

        Assert.assertEquals("http://example.com/1",
                anchor.getDomAttribute("href"));

        Assert.assertEquals("Joe", anchor.getText());

        waitUntil(
                driver -> "Radio button group value changed from 'null' to 'John'"
                        .equals(valueDiv.getText()));
    }

    @Test
    public void iconGenerator() {
        WebElement group = findElement(By.id("button-group-icon-generator"));

        List<WebElement> buttons = group
                .findElements(By.tagName("vaadin-radio-button"));

        WebElement anchor = buttons.get(2).findElement(By.tagName("img"));

        Assert.assertEquals("https://vaadin.com/images/vaadin-logo.svg",
                anchor.getDomAttribute("src"));

        Assert.assertEquals("Bill", buttons.get(2).getText());
    }

    @Test
    public void disabledGroupItems() {
        WebElement group = findElement(By.id("button-group-disabled-items"));

        List<WebElement> buttons = group
                .findElements(By.tagName("vaadin-radio-button"));

        Assert.assertEquals(Boolean.TRUE.toString(),
                buttons.get(1).getDomAttribute("disabled"));

        scrollToElement(group);
        getCommandExecutor().executeScript("window.scrollBy(0,50);");

        buttons.get(0).click();

        WebElement infoLabel = findElement(
                By.id("button-group-disabled-items-info"));

        Assert.assertEquals("'foo' should be selected server-side", "foo",
                infoLabel.getText());

        // Enable 'bar' button on client-side and click it
        executeScript("arguments[0].removeAttribute(\"disabled\");",
                buttons.get(1));
        buttons.get(1).click();

        Assert.assertEquals("Value 'foo' should still be selected server-side",
                "foo", infoLabel.getText());
    }

    @Test
    public void readOnlyGroup() {
        WebElement group = findElement(By.id("button-group-read-only"));

        List<WebElement> buttons = group
                .findElements(By.tagName("vaadin-radio-button"));

        Assert.assertEquals(Boolean.TRUE.toString(),
                buttons.get(1).getDomAttribute("disabled"));
        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getDomAttribute("readonly"));

        scrollToElement(group);
        getCommandExecutor().executeScript("window.scrollBy(0,50);");

        buttons.get(1).click();

        WebElement valueInfo = findElement(By.id("selected-value-info"));
        Assert.assertEquals("", valueInfo.getText());

        // make the group not read-only
        WebElement switchReadOnly = findElement(By.id("switch-read-only"));
        switchReadOnly.click();

        buttons.get(1).click();
        Assert.assertEquals("bar", valueInfo.getText());

        // make it read-only again
        switchReadOnly.click();

        // click to the first item
        buttons.get(0).click();

        // Nothing has changed
        Assert.assertEquals("bar", valueInfo.getText());
    }

    @Test
    public void readOnlyGroup_disableReadOnly_clickOnButtonInput_buttonIsSelected() {
        WebElement switchReadOnly = findElement(By.id("switch-read-only"));
        switchReadOnly.click();

        RadioButtonGroupElement group = $(RadioButtonGroupElement.class)
                .id("button-group-read-only");
        List<RadioButtonElement> buttons = group.$(RadioButtonElement.class)
                .all();

        buttons.get(0).$("input").first().click();

        Assert.assertEquals("Group should have the 'foo' item selected", "foo",
                group.getSelectedText());
    }

    @Test
    public void readOnlyGroup_disableReadOnly_clickOnButtonLabel_buttonIsSelected() {
        WebElement switchReadOnly = findElement(By.id("switch-read-only"));
        switchReadOnly.click();

        RadioButtonGroupElement group = $(RadioButtonGroupElement.class)
                .id("button-group-read-only");
        List<RadioButtonElement> buttons = group.$(RadioButtonElement.class)
                .all();

        buttons.get(0).$("label").first().click();

        Assert.assertEquals("Group should have the 'foo' item selected", "foo",
                group.getSelectedText());
    }

    @Test
    public void verifyHelper() {
        RadioButtonGroupElement groupWithHelperText = $(
                RadioButtonGroupElement.class).id("group-with-helper-text");
        Assert.assertEquals("helperText", groupWithHelperText.getHelperText());

        RadioButtonGroupElement groupWithHelperComponent = $(
                RadioButtonGroupElement.class)
                .id("group-with-helper-component");
        WebElement helperComponent = groupWithHelperComponent
                .getHelperComponent();
        Assert.assertEquals("helperComponent", helperComponent.getText());
        Assert.assertEquals("helper-component",
                helperComponent.getDomAttribute("id"));
    }

    @Test
    public void clearHelper() {
        RadioButtonGroupElement groupWithHelperText = $(
                RadioButtonGroupElement.class).id("group-with-helper-text");
        Assert.assertEquals("helperText", groupWithHelperText.getHelperText());

        $(TestBenchElement.class).id("clear-helper-text-button").click();
        Assert.assertEquals("", groupWithHelperText.getHelperText());

        RadioButtonGroupElement groupWithHelperComponent = $(
                RadioButtonGroupElement.class)
                .id("group-with-helper-component");
        WebElement helperComponent = groupWithHelperComponent
                .getHelperComponent();
        Assert.assertEquals("helper-component",
                helperComponent.getDomAttribute("id"));

        $(TestBenchElement.class).id("clear-helper-component-button").click();
        Assert.assertNull(groupWithHelperComponent.getHelperComponent());
    }
}
