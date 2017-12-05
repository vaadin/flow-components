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
package com.vaadin.ui.grid.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
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

        assertItemsArePresent(grid, itemIdPrefix, 0, 20);

        WebElement button = findElement(By.id(itemIdPrefix + "change-list"));

        clickElementWithJs(button);
        assertItemsArePresent(grid, itemIdPrefix, 20, 10);

        clickElementWithJs(button);
        assertItemsArePresent(grid, itemIdPrefix, 0, 20);
    }

    /**
     * Test ignored until https://github.com/vaadin/flow/issues/3077 is fixed.
     * Currently some items are still left in the grid, but not visible. The
     * internal implementation will change drastically with #3077, and this test
     * should be re-enabled.
     */
    @Test
    @Ignore
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
        try {
            grid.findElement(By.id(itemId));
            Assert.fail("Item with Id '" + itemId
                    + "' is not supposed to be in the Grid");
        } catch (NoSuchElementException ex) {
            // expected
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

}
