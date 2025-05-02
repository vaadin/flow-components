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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the {@link CheckboxDemoPage}.
 */
@TestPath("vaadin-checkbox-test-demo")
public class CheckboxIT extends AbstractComponentIT {
    private TestBenchTestCase layout;

    @Before
    public void init() {
        open();
        layout = this;
    }

    @Test
    public void defaultCheckbox() {
        WebElement checkbox = layout.findElement(By.id("default-checkbox"));
        Assert.assertTrue("Default checkbox should be present",
                "vaadin-checkbox".equals(checkbox.getTagName()));
    }

    @Test
    public void defaultCheckbox_labelIsDisplayed() {
        WebElement checkbox = layout.findElement(By.id("default-checkbox"));
        Assert.assertEquals(
                "Checkbox label should have the text 'Default Checkbox'",
                "Default Checkbox", checkbox.getText());
    }

    @Test
    public void defaultCheckbox_changeLabel_newLabelIsDisplayed() {
        clickButton("change-default-label");

        WebElement checkbox = layout.findElement(By.id("default-checkbox"));
        Assert.assertEquals("Checkbox label should have the text 'New Label'",
                "New Label", checkbox.getText());
    }

    @Test
    public void defaultCheckbox_changeLabel_clickOnLabel_checkboxIsChecked() {
        clickButton("change-default-label");

        CheckboxElement checkbox = $(CheckboxElement.class)
                .id("default-checkbox");
        checkbox.$("label").first().click();

        Assert.assertEquals("Checkbox should be checked", true,
                checkbox.isChecked());
    }

    @Test
    public void defaultCheckbox_changeLabel_clickOnInput_checkboxIsChecked() {
        clickButton("change-default-label");

        CheckboxElement checkbox = $(CheckboxElement.class)
                .id("default-checkbox");
        checkbox.$("input").first().click();

        Assert.assertEquals("Checkbox should be checked", true,
                checkbox.isChecked());
    }

    @Test
    public void disabledCheckbox() {
        CheckboxElement checkbox = layout.$(CheckboxElement.class)
                .id("disabled-checkbox");
        Assert.assertFalse(checkbox.isEnabled());
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
        CheckboxElement checkbox = layout.$(CheckboxElement.class)
                .id("indeterminate-checkbox");
        WebElement button = layout.findElement(By.id("reset-indeterminate"));
        Assert.assertTrue("This checkbox should be in indeterminate state",
                checkbox.hasAttribute("indeterminate"));

        checkbox.click();
        Assert.assertFalse(
                "Checkbox should not be in indeterminate state after clicking it",
                checkbox.hasAttribute("indeterminate"));

        clickElementWithJs(button);
        Assert.assertTrue(
                "This checkbox should be in indeterminate state after resetting",
                checkbox.hasAttribute("indeterminate"));
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
    public void imgCheckbox_clickOnLabel_checkboxIsChecked() {
        CheckboxElement checkbox = $(CheckboxElement.class)
                .id("img-component-label-checkbox");
        checkbox.$("label").first().click();

        Assert.assertTrue("Checkbox should be checked", checkbox.isChecked());
    }

    @Test
    public void imgCheckbox_clickOnLabel_checkboxIsCheckedAfterLabelChange() {
        clickButton("change-img-component-label");

        CheckboxElement checkbox = $(CheckboxElement.class)
                .id("img-component-label-checkbox");
        checkbox.$("label").first().click();

        Assert.assertTrue("Checkbox should be checked", checkbox.isChecked());
    }

    private void clickButton(String id) {
        $("button").id(id).click();
    }
}
