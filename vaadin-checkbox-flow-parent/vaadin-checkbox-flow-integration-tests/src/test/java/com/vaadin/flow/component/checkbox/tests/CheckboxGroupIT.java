/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import java.util.List;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.TestBenchTestCase;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

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
    public void valueChange() {
        WebElement valueDiv = layout.findElement(By.id("checkbox-group-value"));
        WebElement group = layout.findElement(
                By.id("checkbox-group-with-value-change-listener"));

        executeScript("arguments[0].value=['2'];", group);

        waitUntil(driver -> "Checkbox group value changed from '[]' to '[bar]'"
                .equals(valueDiv.getText()));

        executeScript("arguments[0].value=['1','2'];", group);

        waitUntil(
                driver -> "Checkbox group value changed from '[bar]' to '[bar, foo]'"
                        .equals(valueDiv.getText()));
    }

    @Test
    public void itemGenerator() {
        WebElement valueDiv = layout
                .findElement(By.id("checkbox-group-gen-value"));
        WebElement group = layout
                .findElement(By.id("checkbox-group-with-item-generator"));

        executeScript("arguments[0].value=['5'];", group);

        waitUntil(driver -> "Checkbox group value changed from '[]' to '[John]'"
                .equals(valueDiv.getText()));
    }

    @Test
    public void disabledGroup() {
        WebElement group = layout.findElement(By.id("checkbox-group-disabled"));

        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getAttribute("disabled"));
    }

    @Test
    public void disabledGroupItems() {
        TestBenchElement group = $(TestBenchElement.class)
                .id("checkbox-group-disabled-items");

        List<TestBenchElement> checkboxes = group.$("vaadin-checkbox").all();

        Assert.assertEquals(Boolean.TRUE.toString(),
                checkboxes.get(1).getAttribute("disabled"));

        scrollToElement(group);

        executeScript("arguments[0].value=['1'];", group);

        WebElement infoLabel = layout
                .findElement(By.id("checkbox-group-disabled-items-info"));

        Assert.assertEquals("'foo' should be selected", "[foo]",
                infoLabel.getText());

        executeScript("arguments[0].value=['1','2'];", group);

        try {
            waitUntil(
                    driver -> group.findElements(By.tagName("vaadin-checkbox"))
                            .get(1).getAttribute("disabled") != null);
        } catch (WebDriverException wde) {
            Assert.fail("Server should have disabled the checkbox again.");
        }

        Assert.assertEquals("Value 'foo' should have been re-selected", "[foo]",
                infoLabel.getText());

        Assert.assertTrue(
                "Value 'foo' should have been re-selected on the client side",
                Boolean.valueOf(checkboxes.get(0).getAttribute("checked")));
    }

    @Test
    public void readOnlyGroup() {
        WebElement group = layout
                .findElement(By.id("checkbox-group-read-only"));

        List<WebElement> checkboxes = group
                .findElements(By.tagName("vaadin-checkbox"));

        Assert.assertEquals(Boolean.TRUE.toString(),
                checkboxes.get(1).getAttribute("disabled"));
        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getAttribute("disabled"));

        scrollToElement(group);
        getCommandExecutor().executeScript("window.scrollBy(0,50);");

        executeScript("arguments[0].value=['2'];", group);

        WebElement valueInfo = layout.findElement(By.id("selected-value-info"));
        Assert.assertEquals("", valueInfo.getText());

        // make the group not read-only
        WebElement switchReadOnly = findElement(By.id("switch-read-only"));
        new Actions(getDriver()).moveToElement(switchReadOnly).click().build()
                .perform();

        executeScript("arguments[0].value=['2'];", group);
        Assert.assertEquals("[bar]", valueInfo.getText());

        // make it read-only again
        new Actions(getDriver()).moveToElement(switchReadOnly).click().build()
                .perform();

        // click to the first item
        executeScript("arguments[0].value=['1'];", group);

        // Nothing has changed
        Assert.assertEquals("[bar]", valueInfo.getText());
    }

    @Test
    public void assertThemeVariant() {
        WebElement group = findElement(By.id("checkbox-group-theme-variants"));
        scrollToElement(group);
        Assert.assertEquals("vertical", group.getAttribute("theme"));

        findElement(By.id("remove-theme-variant-button")).click();
        Assert.assertNull(group.getAttribute("theme"));
    }

    @Test
    public void groupHasLabelAndErrorMessage_setInvalidShowEM_setValueRemoveEM() {
        TestBenchElement group = $(TestBenchElement.class)
                .id("group-with-label-and-error-message");

        Assert.assertEquals("Label Attribute should present with correct text",
                group.getAttribute("label"), "Group label");

        TestBenchElement errorMessage = group.$("div")
                .attributeContains("slot", "error-message").first();

        verifyGroupValid(group, errorMessage);

        layout.findElement(By.id("group-with-label-button")).click();
        verifyGroupInvalid(group, errorMessage);

        Assert.assertEquals(
                "Correct error message should be shown after the button clicks",
                "Field has been set to invalid from server side",
                errorMessage.getText());
    }

    @Test
    public void assertHelperText() {
        TestBenchElement group = $(TestBenchElement.class)
                .id("checkbox-helper-text");

        TestBenchElement helperText = group.$("div")
                .attributeContains("slot", "helper").first();

        Assert.assertEquals("Helper text", helperText.getText());

        $("button").id("button-clear-helper").click();
        Assert.assertEquals("", helperText.getText());
    }

    @Test
    public void assertHelperComponent() {
        TestBenchElement group = $(TestBenchElement.class)
                .id("checkbox-helper-component");

        TestBenchElement helperComponent = group.$("span")
                .attributeContains("slot", "helper").first();
        Assert.assertEquals("Helper text", helperComponent.getText());

        $("button").id("button-clear-component").click();

        waitUntil(ExpectedConditions
                .invisibilityOfElementLocated(By.id("helper-component")));
    }

    @Test
    public void iconRenderer() {
        WebElement group = findElement(By.id("checkbox-group-icon-renderer"));

        List<WebElement> checkboxes = group
                .findElements(By.tagName("vaadin-checkbox"));

        WebElement anchor = checkboxes.get(2).findElement(By.tagName("img"));

        Assert.assertEquals("https://vaadin.com/images/vaadin-logo.svg",
                anchor.getAttribute("src"));

        Assert.assertEquals("Bill", checkboxes.get(2).getText());
    }

    private void verifyGroupInvalid(TestBenchElement group,
            TestBenchElement errorMessage) {
        Assert.assertEquals("Checkbox group is invalid.", true,
                group.getPropertyBoolean("invalid"));
        Assert.assertFalse("Error message should be shown.",
                errorMessage.getText().isEmpty());
    }

    private void verifyGroupValid(TestBenchElement group,
            TestBenchElement errorMessage) {
        Boolean isInvalid = group.getPropertyBoolean("invalid");
        Assert.assertThat("Checkbox group is not invalid.", isInvalid,
                CoreMatchers.anyOf(CoreMatchers.equalTo(isInvalid),
                        CoreMatchers.equalTo(false)));
        Assert.assertTrue("Error message should be empty.",
                errorMessage.getText().isEmpty());
    }
}
