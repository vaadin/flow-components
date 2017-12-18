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
package com.vaadin.flow.component.grid.tests;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
        Map<Integer, Map<String, ?>> items = getItems(driver, grid);
        Set<Entry<Integer, Map<String, ?>>> entrySet = items.entrySet();
        for (Entry<Integer, Map<String, ?>> entry : entrySet) {
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

    public static Map<Integer, Map<String, ?>> getItems(WebDriver driver,
            WebElement element) {
        Object result = ((JavascriptExecutor) driver)
                .executeScript("return arguments[0]._cache.items;", element);

        return (Map<Integer, Map<String, ?>>) result;
    }

}
