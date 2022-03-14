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
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Integration tests for the {@link ComboBoxDemoPage}.
 */
@TestPath("/vaadin-combo-box-test-demo")
public class ComboBoxIT extends AbstractComboBoxIT {

    @Before
    public void init() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-combo-box"))
                .size() > 0);
    }

    @Test
    public void openStringBoxAndSelectAnItem() {
        checkLogsForErrors();
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("string-selection-box");
        WebElement message = findElement(By.id("string-selection-message"));

        comboBox.openPopup();
        executeScript(
                "arguments[0].selectedItem = arguments[0].filteredItems[2]",
                comboBox);

        Assert.assertEquals("Selected browser: Opera", message.getText());
    }

    @Test
    public void openObjectBoxAndSelectAnItem() {
        checkLogsForErrors();
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("object-selection-box");
        WebElement message = findElement(By.id("object-selection-message"));

        comboBox.openPopup();
        executeScript(
                "arguments[0].selectedItem = arguments[0].filteredItems[1]",
                comboBox);

        waitUntil(driver -> message.getText().equals(
                "Selected song: Sculpted\nFrom album: Two Fold Pt.1\nBy artist: Haywyre"));
    }

    @Test
    public void setEnabledCombobox() {
        checkLogsForErrors();
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("disabled-combo-box");
        executeScript("arguments[0].removeAttribute(\"disabled\");", comboBox);
        comboBox.openPopup();

        try {
            waitUntil(driver -> {
                boolean isLoading = comboBox.getPropertyBoolean("loading");
                Assert.assertTrue(
                        "Expected ComboBox to remain in loading state, "
                                + "as the server should not send data to disabled component.",
                        isLoading);
                return !isLoading;
            }, 2);
        } catch (TimeoutException e) {
            // expected
        }
    }

    @Test
    public void openValueBoxSelectTwoItems() {
        checkLogsForErrors();
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("value-selection-box");
        WebElement message = findElement(By.id("value-selection-message"));

        comboBox.openPopup();
        executeScript(
                "arguments[0].selectedItem = arguments[0].filteredItems[1]",
                comboBox);

        waitUntil(
                driver -> message.getText().equals("Selected artist: Haywyre"));

        executeScript(
                "arguments[0].selectedItem = arguments[0].filteredItems[0]",
                comboBox);

        waitUntil(driver -> message.getText().equals(
                "Selected artist: Haircuts for Men\nThe old selection was: Haywyre"));
    }

    @Test
    public void openTemplateBox() {
        checkLogsForErrors();

        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("template-selection-box");
        WebElement message = findElement(By.id("template-selection-message"));

        comboBox.openPopup();
        List<Map<String, ?>> items = (List<Map<String, ?>>) executeScript(
                "return arguments[0].filteredItems", comboBox);

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

        executeScript(
                "arguments[0].selectedItem = arguments[0].filteredItems[1]",
                comboBox);

        waitUntil(
                driver -> message.getText().equals("Selected artist: Haywyre"));

        executeScript(
                "arguments[0].selectedItem = arguments[0].filteredItems[0]",
                comboBox);

        waitUntil(driver -> message.getText().equals(
                "Selected artist: Haircuts for Men\nThe old selection was: Haywyre"));
    }

    @Test
    public void templateBoxCustomFiltering_filterableByArtist() {
        checkLogsForErrors();
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("template-selection-box");
        comboBox.openPopup();
        comboBox.setFilter("ha");

        waitUntil(driver -> ((List<Map<String, ?>>) executeScript(
                "return arguments[0].filteredItems", comboBox)).size() == 2);

        List<Map<String, ?>> items = (List<Map<String, ?>>) executeScript(
                "return arguments[0].filteredItems", comboBox);

        Assert.assertEquals("Haircuts for Men", items.get(0).get("artist"));
        Assert.assertEquals("Haywyre", items.get(1).get("artist"));
    }

    @Test
    public void componentBoxCustomFiltering_filterableByArtist() {
        checkLogsForErrors();
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("component-selection-box");
        comboBox.openPopup();
        comboBox.setFilter("ha");

        waitUntil(driver -> ((List<Map<String, ?>>) executeScript(
                "return arguments[0].filteredItems", comboBox)).size() == 2);

        List<Map<String, ?>> items = (List<Map<String, ?>>) executeScript(
                "return arguments[0].filteredItems", comboBox);

        Assert.assertEquals("A V Club Disagrees", items.get(0).get("label"));
        Assert.assertEquals("Sculpted", items.get(1).get("label"));
    }

    @Test
    public void openComponentBox() {
        checkLogsForErrors();

        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("component-selection-box");
        WebElement message = findElement(By.id("component-selection-message"));

        comboBox.openPopup();
        List<Map<String, ?>> items = (List<Map<String, ?>>) executeScript(
                "return arguments[0].filteredItems", comboBox);

        items.forEach(item -> {
            Assert.assertNotNull(item.get("key"));
            Assert.assertNotNull(item.get("label"));
        });

        Map<String, ?> firstItem = items.get(0);
        Assert.assertEquals("A V Club Disagrees", firstItem.get("label"));

        executeScript(
                "arguments[0].selectedItem = arguments[0].filteredItems[1]",
                comboBox);

        waitUntil(
                driver -> message.getText().equals("Selected artist: Haywyre"));

        executeScript(
                "arguments[0].selectedItem = arguments[0].filteredItems[0]",
                comboBox);

        waitUntil(driver -> message.getText().equals(
                "Selected artist: Haircuts for Men\nThe old selection was: Haywyre"));
    }

    @Test
    public void inMemoryLazyComboBox_itemsLoadedLazily() {
        testLazyComboBox("lazy-loading-box");
    }

    @Test
    public void callBackLazycallBackLazyComboBox_itemsLoadedLazilyComboBox_itemsLoadedLazily() {
        testLazyComboBox("callback-box");
    }

    private void testLazyComboBox(String comboBoxId) {
        checkLogsForErrors();
        ComboBoxElement comboBox = $(ComboBoxElement.class).id(comboBoxId);
        scrollToElement(comboBox);

        Assert.assertEquals("No items should be loaded initially.", 0,
                getLoadedItems(comboBox).size());

        comboBox.openPopup();

        Assert.assertEquals(
                "First page should be loaded after opening overlay.", 50,
                getLoadedItems(comboBox).size());
        assertRendered();

        scrollToItem(comboBox, 50);
        Assert.assertEquals("Second page should be loaded after scrolling.",
                100, getLoadedItems(comboBox).size());
    }

    private void assertRendered() {
        try {
            waitUntil(driver -> {
                List<String> items = getRenderedItems();
                return items.size() > 0 && items.get(0).length() > 0;
            });
        } catch (TimeoutException e) {
            Assert.fail("Timeout: no items with text content rendered.");
        }
        List<String> items = getRenderedItems();
        Assert.assertTrue("Expected more than 10 items to be rendered.",
                items.size() > 10);
        items.forEach(item -> {
            boolean containsAtLeastTwoWords = Pattern.matches("\\S+\\s\\S+.*",
                    item);
            Assert.assertTrue(
                    "Expected rendered item to contain at least two words, but was: "
                            + item,
                    containsAtLeastTwoWords);
        });
    }

    private List<String> getRenderedItems() {
        return getItemElements().stream()
                .map(element -> element.getPropertyString("innerHTML"))
                .collect(Collectors.toList());
    }
}
