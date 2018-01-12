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
package com.vaadin.flow.component.combobox.test;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.combobox.demo.ComboBoxView;
import com.vaadin.flow.demo.TabbedComponentDemoTest;

/**
 * Integration tests for the {@link ComboBoxView}.
 */
public class ComboBoxIT extends TabbedComponentDemoTest {

    @Test
    public void openStringBoxAndSelectAnItem() {
        openTabAndCheckForErrors("");
        WebElement comboBox = layout.findElement(By.id("string-selection-box"));
        WebElement message = layout
                .findElement(By.id("string-selection-message"));

        executeScript("arguments[0].selectedItem = arguments[0].items[2]",
                comboBox);

        Assert.assertEquals("Selected browser: Opera", message.getText());
    }

    @Test
    public void openObjectBoxAndSelectAnItem() {
        openTabAndCheckForErrors("");
        WebElement comboBox = layout.findElement(By.id("object-selection-box"));
        WebElement message = layout
                .findElement(By.id("object-selection-message"));

        executeScript("arguments[0].selectedItem = arguments[0].items[1]",
                comboBox);

        waitUntil(driver -> message.getText().equals(
                "Selected song: Sculpted\nFrom album: Two Fold Pt.1\nBy artist: Haywyre"));
    }

    @Test
    public void openValueBoxSelectTwoItems() {
        openTabAndCheckForErrors("");
        WebElement comboBox = layout.findElement(By.id("value-selection-box"));
        WebElement message = layout
                .findElement(By.id("value-selection-message"));

        executeScript("arguments[0].selectedItem = arguments[0].items[1]",
                comboBox);

        waitUntil(
                driver -> message.getText().equals("Selected artist: Haywyre"));

        executeScript("arguments[0].selectedItem = arguments[0].items[0]",
                comboBox);

        waitUntil(driver -> message.getText().equals(
                "Selected artist: Haircuts for Men\nThe old selection was: Haywyre"));
    }

    @Test
    public void openTemplateBox() {
        openTabAndCheckForErrors("using-templates");

        WebElement comboBox = layout
                .findElement(By.id("template-selection-box"));
        WebElement message = layout
                .findElement(By.id("template-selection-message"));

        List<Map<String, ?>> items = (List<Map<String, ?>>) executeScript(
                "return arguments[0].items", comboBox);

        items.forEach(item -> {
            Assert.assertNotNull(item.get("key"));
            Assert.assertNotNull(item.get("label"));
            Assert.assertNotNull(item.get("song"));
            Assert.assertNotNull(item.get("artist"));
        });

        Map<String, ?> firstItem = items.get(0);
        Assert.assertEquals("A V Club Disagrees", firstItem.get("label"));
        Assert.assertEquals("A V Club Disagrees", firstItem.get("song"));
        Assert.assertEquals("Haircuts for Men", firstItem.get("artist"));

        executeScript("arguments[0].selectedItem = arguments[0].items[1]",
                comboBox);

        waitUntil(
                driver -> message.getText().equals("Selected artist: Haywyre"));

        executeScript("arguments[0].selectedItem = arguments[0].items[0]",
                comboBox);

        waitUntil(driver -> message.getText().equals(
                "Selected artist: Haircuts for Men\nThe old selection was: Haywyre"));
    }

    @Test
    public void openComponentBox() {
        openTabAndCheckForErrors("using-components");

        WebElement comboBox = layout
                .findElement(By.id("component-selection-box"));
        WebElement message = layout
                .findElement(By.id("component-selection-message"));

        List<Map<String, ?>> items = (List<Map<String, ?>>) executeScript(
                "return arguments[0].items", comboBox);

        items.forEach(item -> {
            Assert.assertNotNull(item.get("key"));
            Assert.assertNotNull(item.get("label"));
            Assert.assertNotNull(item.get("nodeId"));
        });

        Map<String, ?> firstItem = items.get(0);
        Assert.assertEquals("A V Club Disagrees", firstItem.get("label"));

        executeScript("arguments[0].selectedItem = arguments[0].items[1]",
                comboBox);

        waitUntil(
                driver -> message.getText().equals("Selected artist: Haywyre"));

        executeScript("arguments[0].selectedItem = arguments[0].items[0]",
                comboBox);

        waitUntil(driver -> message.getText().equals(
                "Selected artist: Haircuts for Men\nThe old selection was: Haywyre"));
    }

    @Override
    protected String getTestPath() {
        return ("/vaadin-combo-box");
    }
}
