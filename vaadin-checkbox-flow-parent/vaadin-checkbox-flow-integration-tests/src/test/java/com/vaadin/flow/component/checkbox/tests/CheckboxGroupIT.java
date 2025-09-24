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
package com.vaadin.flow.component.checkbox.tests;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.component.checkbox.testbench.CheckboxGroupElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-checkbox-group-test-demo")
public class CheckboxGroupIT extends AbstractComponentIT {

    private TestBenchTestCase layout;

    @Before
    public void init() {
        open();
        layout = this;
    }

    @Test
    public void valueChangeAndSelection() {
        WebElement valueDiv = layout.findElement(By.id("checkbox-group-value"));
        CheckboxGroupElement group = $(CheckboxGroupElement.class)
                .id("checkbox-group-with-value-change-listener");

        group.selectByText("bar");

        waitUntil(driver -> "Checkbox group value changed from '[]' to '[bar]'"
                .equals(valueDiv.getText()));
        Assert.assertEquals(Arrays.asList("bar"), group.getSelectedTexts());

        group.selectByText("foo");

        waitUntil(
                driver -> "Checkbox group value changed from '[bar]' to '[bar, foo]'"
                        .equals(valueDiv.getText()));
        Assert.assertEquals(Arrays.asList("foo", "bar"),
                group.getSelectedTexts());

        group.deselectByText("bar");
        waitUntil(
                driver -> "Checkbox group value changed from '[bar, foo]' to '[foo]'"
                        .equals(valueDiv.getText()));
        Assert.assertEquals(Arrays.asList("foo"), group.getSelectedTexts());
    }

    @Test
    public void itemGenerator() {
        WebElement valueDiv = layout
                .findElement(By.id("checkbox-group-gen-value"));
        CheckboxGroupElement group = $(CheckboxGroupElement.class)
                .id("checkbox-group-with-item-generator");

        group.selectByText("John");

        waitUntil(driver -> "Checkbox group value changed from '[]' to '[John]'"
                .equals(valueDiv.getText()));
    }

    @Test
    public void disabledGroup() {
        CheckboxGroupElement group = $(CheckboxGroupElement.class)
                .id("checkbox-group-disabled");

        Assert.assertFalse(group.isEnabled());
    }

    @Test
    public void disabledGroupItems() {
        CheckboxGroupElement group = $(CheckboxGroupElement.class)
                .id("checkbox-group-disabled-items");

        List<CheckboxElement> checkboxes = group.getCheckboxes();

        Assert.assertFalse(checkboxes.get(1).isChecked());

        scrollToElement(group);

        group.selectByText("foo");

        WebElement infoLabel = layout
                .findElement(By.id("checkbox-group-disabled-items-info"));

        Assert.assertEquals("'foo' should be selected server-side", "[foo]",
                infoLabel.getText());

        group.selectByText("bar");

        Assert.assertEquals("Still only 'foo' should be selected server-side",
                "[foo]", infoLabel.getText());
    }

    @Test
    public void readOnlyGroup() {
        CheckboxGroupElement group = $(CheckboxGroupElement.class)
                .id("checkbox-group-read-only");

        List<CheckboxElement> checkboxes = group.getCheckboxes();

        Assert.assertTrue(checkboxes.get(1).hasAttribute("readonly"));
        Assert.assertTrue(group.hasAttribute("readonly"));

        scrollToElement(group);
        getCommandExecutor().executeScript("window.scrollBy(0,50);");

        executeScript("arguments[0].value=['2'];", group);

        WebElement valueInfo = layout.findElement(By.id("selected-value-info"));
        Assert.assertEquals("", valueInfo.getText());

        // make the group not read-only
        findElement(By.id("switch-read-only")).click();

        group.selectByText("bar");
        Assert.assertEquals("[bar]", valueInfo.getText());

        // make it read-only again
        findElement(By.id("switch-read-only")).click();

        // click to the first item
        group.selectByText("foo");

        // Nothing has changed
        Assert.assertEquals("[bar]", valueInfo.getText());
    }

    @Test
    public void assertHelperText() {
        CheckboxGroupElement group = $(CheckboxGroupElement.class)
                .id("checkbox-helper-text");

        TestBenchElement helperText = group.getHelperComponent();

        Assert.assertEquals("Helper text", helperText.getText());

        $("button").id("button-clear-helper").click();
        Assert.assertEquals("", helperText.getText());
    }

    @Test
    public void assertHelperComponent() {
        CheckboxGroupElement group = $(CheckboxGroupElement.class)
                .id("checkbox-helper-component");

        TestBenchElement helperComponent = group.getHelperComponent();
        Assert.assertEquals("Helper text", helperComponent.getText());

        $("button").id("button-clear-component").click();

        waitUntil(ExpectedConditions
                .invisibilityOfElementLocated(By.id("helper-component")));
    }

    @Test
    public void iconRenderer() {
        CheckboxGroupElement group = $(CheckboxGroupElement.class)
                .id("checkbox-group-icon-renderer");

        List<CheckboxElement> checkboxes = group.getCheckboxes();

        WebElement anchor = checkboxes.get(2).findElement(By.tagName("img"));

        Assert.assertEquals("https://vaadin.com/images/vaadin-logo.svg",
                anchor.getDomAttribute("src"));

        Assert.assertEquals("Bill", checkboxes.get(2).getText());
    }
}
