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
package com.vaadin.flow.component.radiobutton.tests;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.demo.ComponentDemoTest;
import com.vaadin.testbench.TestBenchElement;

public class RadioButtonGroupIT extends ComponentDemoTest {

    @Override
    protected String getTestPath() {
        return "/vaadin-radio-button-group-test-demo";
    }

    @Test
    public void valueChange() {
        WebElement valueDiv = layout.findElement(By.id("button-group-value"));
        WebElement group = layout
                .findElement(By.id("button-group-with-value-change-listener"));

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
        WebElement valueDiv = layout
                .findElement(By.id("button-group-gen-value"));
        WebElement group = layout
                .findElement(By.id("button-group-with-item-generator"));

        executeScript("arguments[0].value=2;", group);

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

        executeScript("arguments[0].value=2;", group);
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

        scrollToElement(group);
        getCommandExecutor().executeScript("window.scrollBy(0,50);");

        new Actions(getDriver()).moveToElement(buttons.get(0)).click().build()
                .perform();

        WebElement infoLabel = layout
                .findElement(By.id("button-group-disabled-items-info"));

        Assert.assertEquals("'foo' should be selected", "foo",
                infoLabel.getText());

        executeScript("arguments[0].removeAttribute(\"disabled\");",
                buttons.get(1));

        new Actions(getDriver()).moveToElement(buttons.get(1)).click().build()
                .perform();

        try {
            waitUntil(driver -> group
                    .findElements(By.tagName("vaadin-radio-button")).get(1)
                    .getAttribute("disabled") != null);
        } catch (WebDriverException wde) {
            Assert.fail("Server should have disabled the button again.");
        }

        Assert.assertEquals("Value 'foo' should have been re-selected", "foo",
                infoLabel.getText());

        Assert.assertTrue(
                "Value 'foo' should have been re-selected on the client side",
                Boolean.valueOf(buttons.get(0).getAttribute("checked")));
    }

    @Test
    public void readOnlyGroup() {
        WebElement group = layout.findElement(By.id("button-group-read-only"));

        List<WebElement> buttons = group
                .findElements(By.tagName("vaadin-radio-button"));

        Assert.assertEquals(Boolean.TRUE.toString(),
                buttons.get(1).getAttribute("disabled"));
        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getAttribute("disabled"));

        scrollToElement(group);
        getCommandExecutor().executeScript("window.scrollBy(0,50);");

        new Actions(getDriver()).moveToElement(buttons.get(1)).click().build()
                .perform();

        WebElement valueInfo = layout.findElement(By.id("selected-value-info"));
        Assert.assertEquals("", valueInfo.getText());

        // make the group not read-only
        WebElement switchReadOnly = findElement(By.id("switch-read-only"));
        new Actions(getDriver()).moveToElement(switchReadOnly).click().build()
                .perform();

        new Actions(getDriver()).moveToElement(buttons.get(1)).click().build()
                .perform();
        Assert.assertEquals("bar", valueInfo.getText());

        // make it read-only again
        new Actions(getDriver()).moveToElement(switchReadOnly).click().build()
                .perform();

        // click to the first item
        new Actions(getDriver()).moveToElement(buttons.get(0)).click().build()
                .perform();

        // Nothing has changed
        Assert.assertEquals("bar", valueInfo.getText());
    }

    @Test
    public void addedComponentsAfterItems() {
        WebElement group = layout
                .findElement(By.id("button-group-with-appended-text"));

        List<WebElement> elements = group.findElements(By.xpath("./*"));

        Assert.assertEquals(
                "Unexpected amount of elements in radio-button-group", 4,
                elements.size());
        Assert.assertEquals("First element should be a <vaadin-radio-button>",
                "vaadin-radio-button", elements.get(0).getTagName());
        Assert.assertEquals("Second element should be a <vaadin-radio-button>",
                "vaadin-radio-button", elements.get(1).getTagName());
        Assert.assertEquals("Thirs element should be a <vaadin-radio-button>",
                "vaadin-radio-button", elements.get(2).getTagName());
        Assert.assertEquals("Fourth element should be a <label>", "label",
                elements.get(3).getTagName());
    }

    @Test
    public void insertedComponentsBetweenItems() {
        WebElement group = layout
                .findElement(By.id("button-group-with-inserted-component"));

        List<WebElement> elements = group.findElements(By.xpath("./*"));

        Assert.assertEquals(
                "Unexpected amount of elements in radio-button-group", 5,
                elements.size());
        Assert.assertEquals("Second element should be a label", "label",
                elements.get(1).getTagName());
        Assert.assertEquals("Third element should be a <hr>", "hr",
                elements.get(2).getTagName());
        Assert.assertEquals("Fourth element should be a <vaadin-radio-button>",
                "vaadin-radio-button", elements.get(3).getTagName());
    }

    @Test
    public void componentsPrependedBeforeItems() {
        WebElement group = layout
                .findElement(By.id("button-group-with-prepended-component"));

        List<WebElement> elements = group.findElements(By.xpath("./*"));

        Assert.assertEquals(
                "Unexpected amount of elements in radio-button-group", 12,
                elements.size());
        // Three groups of (label, hr, vaadin-radio-button, vaadin-radio-button)
        IntStream.range(0, 2).forEach(i -> {
            int firstInGroup = i * 4;
            Assert.assertEquals(
                    "Expected first in group " + (i + 1) + " to be a label",
                    "label", elements.get(firstInGroup).getTagName());
            Assert.assertEquals(
                    "Expected second in group " + (i + 1) + " to be a <hr>",
                    "hr", elements.get(firstInGroup + 1).getTagName());
            Assert.assertEquals(
                    "Expected third in group " + (i + 1)
                            + " to be a <vaadin-radio-button>",
                    "vaadin-radio-button",
                    elements.get(firstInGroup + 2).getTagName());
            Assert.assertEquals(
                    "Expected fourth in group " + (i + 1)
                            + " to be a <vaadin-radio-button>",
                    "vaadin-radio-button",
                    elements.get(firstInGroup + 3).getTagName());
        });
    }

    @Test
    public void dynamicComponentForAfterItem() {
        WebElement group = layout
                .findElement(By.id("button-group-with-dynamic-component"));

        List<WebElement> elements = group.findElements(By.xpath("./*"));

        Assert.assertEquals(
                "Unexpected amount of elements in radio-button-group", 6,
                elements.size());
        for (int i = 0; i < 6; i++) {
            Assert.assertEquals(
                    "Expected only <vaadin-radio-button> to be available",
                    "vaadin-radio-button", elements.get(i).getTagName());
        }

        executeScript("arguments[0].value=1;", group);

        elements = group.findElements(By.xpath("./*"));

        Assert.assertEquals(
                "Expected one label to have been added to radio-button-group",
                7, elements.size());

        Assert.assertEquals(
                "Second element should be a label as first element was selected",
                "label", elements.get(1).getTagName());

        executeScript("arguments[0].value=5;", group);

        elements = group.findElements(By.xpath("./*"));

        Assert.assertEquals(
                "Expected label to stay, just change place in radio-button-group",
                7, elements.size());

        Assert.assertEquals(
                "Fifth element should be a label as fourth element was selected",
                "label", elements.get(5).getTagName());
    }

    @Test
    public void assertThemeVariant() {
        verifyThemeVariantsBeingToggled();
    }

    @Test
    public void groupHasLabelAndErrorMessage_setInvalidShowEM_setValueRemoveEM() {
        TestBenchElement group = $(TestBenchElement.class)
                .id("group-with-label-and-error-message");

        Assert.assertEquals("Label Attribute should present with correct text",
                group.getAttribute("label"), "Group label");

        TestBenchElement errorMessage = group.$(TestBenchElement.class)
                .id("vaadin-radio-group-error-1");
        verifyGroupValid(group, errorMessage);

        layout.findElement(By.id("group-with-label-button")).click();
        verifyGroupInvalid(group, errorMessage);

        Assert.assertEquals(
                "Correct error message should be shown after the button clicks",
                "Field has been set to invalid from server side",
                errorMessage.getText());

        executeScript("arguments[0].value=2;", group);
        verifyGroupValid(group, errorMessage);
    }

    private void verifyGroupInvalid(TestBenchElement group,
            TestBenchElement errorMessage) {
        Assert.assertEquals("Radio button group is invalid.",
                true, group.getPropertyBoolean("invalid"));
        Assert.assertEquals("Error message should be shown.",
                Boolean.FALSE.toString(),
                errorMessage.getAttribute("aria-hidden"));
    }

    private void verifyGroupValid(TestBenchElement group,
            TestBenchElement errorMessage) {
        Assert.assertEquals("Radio button group is not invalid.",
                false, group.getPropertyBoolean("invalid"));
        Assert.assertEquals("Error message should be hidden.",
                Boolean.TRUE.toString(),
                errorMessage.getAttribute("aria-hidden"));
    }
}
