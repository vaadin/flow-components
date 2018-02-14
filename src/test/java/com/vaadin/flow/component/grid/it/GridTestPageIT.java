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
package com.vaadin.flow.component.grid.it;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

/**
 * 
 * Integration tests for the {@link GridTestPage}.
 * 
 * @author Vaadin Ltd.
 */
@TestPath("vaadin-grid-test")
public class GridTestPageIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-grid")).size() > 0);
    }

    @Test
    public void openGridWithComponents_changeLists_componentsAreRendered() {
        WebElement grid = findElement(By.id("grid-with-component-renderers"));
        String itemIdPrefix = "grid-with-component-renderers-";

        waitUntil(driver -> getItems(driver, grid).size() == 20);
        assertItemsArePresent(grid, itemIdPrefix, 0, 20);

        WebElement button = findElement(By.id(itemIdPrefix + "change-list"));

        clickElementWithJs(button);
        waitUntil(driver -> getItems(driver, grid).size() == 10);
        assertItemsArePresent(grid, itemIdPrefix, 20, 10);

        clickElementWithJs(button);
        waitUntil(driver -> getItems(driver, grid).size() == 20);
        assertItemsArePresent(grid, itemIdPrefix, 0, 20);
    }

    @Test
    public void openGridWithComponents_removeItems_componentsAreRemoved() {
        WebElement grid = findElement(By.id("grid-with-component-renderers"));
        String itemIdPrefix = "grid-with-component-renderers-";

        assertItemsArePresent(grid, itemIdPrefix, 0, 20);

        WebElement button = findElement(By.id(itemIdPrefix + "remove-0"));
        clickElementWithJs(button);

        assertItemIsNotPresent(grid, itemIdPrefix + "item-name-0");
        assertItemsArePresent(grid, itemIdPrefix, 1, 19);

        button = findElement(By.id(itemIdPrefix + "remove-19"));
        clickElementWithJs(button);

        assertItemIsNotPresent(grid, itemIdPrefix + "item-name-19");
        assertItemsArePresent(grid, itemIdPrefix, 1, 18);
    }

    @Test
    public void openGridWithTemplateDetailsRow_clickOnItems_dataIsTransmitted() {
        WebElement grid = findElement(By.id("grid-with-template-details-row"));

        Map<String, Map<String, ?>> items = getItems(driver, grid);
        // verify that the properties needed for the details row are not loaded
        items.forEach((row, map) -> {
            Assert.assertEquals("Item " + row, map.get("col0"));
            Assert.assertThat(map.keySet(),
                    CoreMatchers.not(CoreMatchers.hasItem("detailsProperty")));
        });

        // click on the cell to open the details row
        clickElementWithJs(getRow(grid, 0).findElement(By.tagName("td")));

        items = getItems(driver, grid);
        // verify that the properties needed for the details row are loaded for
        // row 0
        items.forEach((row, map) -> {
            Assert.assertEquals("Item " + row, map.get("col0"));
            if ("0".equals(row)) {
                Assert.assertEquals("Details opened! 0",
                        map.get("detailsProperty"));
            } else {
                Assert.assertThat(map.keySet(), CoreMatchers
                        .not(CoreMatchers.hasItem("detailsProperty")));
            }
        });
    }

    @Test
    public void openGridWithComponentDetailsRow_clickOnItems_dataIsTransmitted() {
        WebElement grid = findElement(By.id("grid-with-component-details-row"));

        Map<String, Map<String, ?>> items = getItems(driver, grid);
        // verify that the nodeId of the details row is not loaded
        items.forEach((row, map) -> {
            Assert.assertEquals("Item " + row, map.get("col0"));
            Assert.assertThat(map.keySet(),
                    CoreMatchers.not(CoreMatchers.hasItem("nodeId")));
        });

        // click on the cell to open the details row
        clickElementWithJs(getRow(grid, 0).findElement(By.tagName("td")));

        items = getItems(driver, grid);
        // verify that the nodeId of the details row is loaded for row 0
        items.forEach((row, map) -> {
            Assert.assertEquals("Item " + row, map.get("col0"));
            if ("0".equals(row)) {
                Assert.assertTrue("_renderer_* property not found for item 0",
                        map.keySet().stream()
                                .anyMatch(key -> key.startsWith("_renderer_")));
            } else {
                Assert.assertFalse(
                        "_renderer_* property should not be present for item 0",
                        map.keySet().stream()
                                .anyMatch(key -> key.startsWith("_renderer_")));
            }
        });
    }

    @Test
    public void openGridWithRemovableColumns_removeNameColumn_dataIsNotTransmitted() {
        WebElement grid = findElement(By.id("grid-with-removable-columns"));

        /*
         * The first column was removed before the grid was rendered, so the
         * col0 key shouldn't be present
         */
        Map<String, Map<String, ?>> items = getItems(driver, grid);
        items.forEach((row, map) -> {
            Assert.assertThat(map.keySet(),
                    CoreMatchers.not(CoreMatchers.hasItem("col0")));
            Assert.assertEquals("Item " + row, map.get("col1"));
            Assert.assertEquals(String.valueOf(row),
                    String.valueOf(map.get("col2")));
        });

        WebElement button = findElement(By.id("remove-name-column-button"));
        clickElementWithJs(button);

        /*
         * The second column was removed and the DataCommunicator reset, so the
         * col1 key shouldn't be present
         */
        items = getItems(driver, grid);
        items.forEach((row, map) -> {
            Assert.assertThat(map.keySet(),
                    CoreMatchers.not(CoreMatchers.hasItem("col0")));
            Assert.assertThat(map.keySet(),
                    CoreMatchers.not(CoreMatchers.hasItem("col1")));
            Assert.assertEquals(String.valueOf(row),
                    String.valueOf(map.get("col2")));
        });
    }

    private void assertItemsArePresent(WebElement grid, String itemIdPrefix,
            int startingIndex, int length) {
        for (int i = 0; i < length; i++) {
            int index = startingIndex + i;
            assertItemIsPresent(grid, itemIdPrefix + "item-name-" + index,
                    "Item " + index);
            assertItemIsPresent(grid, itemIdPrefix + "item-number-" + index,
                    String.valueOf(index));
        }
    }

    private void assertItemIsNotPresent(WebElement grid, String itemId) {
        /*
         * Grid reuses some cells over time, and items not used anymore can
         * still be shown in the DOM tree, but are not visible in the UI.
         * Because of that, we can't just try to find the item in the tree and
         * assert that it's not there.
         */
        Map<String, Map<String, ?>> items = getItems(driver, grid);
        Set<Entry<String, Map<String, ?>>> entrySet = items.entrySet();
        for (Entry<String, Map<String, ?>> entry : entrySet) {
            Map<String, ?> map = entry.getValue();
            if (itemId.equals(map.get("id"))) {
                Assert.fail("Item ID '" + itemId
                        + "' is not supposed to be in the Grid");
            }
        }
    }

    private void assertItemIsPresent(WebElement grid, String itemId,
            String expectedInnerText) {
        try {
            WebElement item = grid.findElement(By.id(itemId));
            Assert.assertEquals(expectedInnerText, getInnerText(item));
        } catch (NoSuchElementException ex) {
            Assert.fail("Item with Id '" + itemId + "' should be in the Grid");
        }
    }

    private String getInnerText(WebElement element) {
        return String.valueOf(
                executeScript("return arguments[0].innerText", element));
    }

    public static Map<String, Map<String, ?>> getItems(WebDriver driver,
            WebElement element) {
        Object result = ((JavascriptExecutor) driver)
                .executeScript("return arguments[0]._cache.items;", element);

        return (Map<String, Map<String, ?>>) result;
    }

    private WebElement getRow(WebElement grid, int row) {
        return getInShadowRoot(grid, By.id("items"))
                .findElements(By.cssSelector("tr")).get(row);
    }

}
