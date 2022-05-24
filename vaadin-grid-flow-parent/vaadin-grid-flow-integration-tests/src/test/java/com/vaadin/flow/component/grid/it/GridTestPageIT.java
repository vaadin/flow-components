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

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
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
        assertItemsArePresent(grid, itemIdPrefix, 0, 17);

        WebElement button = findElement(By.id(itemIdPrefix + "change-list"));

        clickElementWithJs(button);
        waitUntil(driver -> getItems(driver, grid).size() == 10);
        assertItemsArePresent(grid, itemIdPrefix, 20, 10);

        clickElementWithJs(button);
        waitUntil(driver -> getItems(driver, grid).size() == 20);
        assertItemsArePresent(grid, itemIdPrefix, 0, 17);
    }

    @Test
    public void openGridWithComponents_removeItems_componentsAreRemoved() {
        WebElement grid = findElement(By.id("grid-with-component-renderers"));
        String itemIdPrefix = "grid-with-component-renderers-";

        assertItemsArePresent(grid, itemIdPrefix, 0, 17);

        WebElement button = findElement(By.id(itemIdPrefix + "remove-0"));
        clickElementWithJs(button);

        assertItemIsNotPresent(grid, itemIdPrefix + "item-name-0");
        assertItemsArePresent(grid, itemIdPrefix, 1, 18);

        button = findElement(By.id(itemIdPrefix + "remove-18"));
        clickElementWithJs(button);

        assertItemIsNotPresent(grid, itemIdPrefix + "item-name-18");
        assertItemsArePresent(grid, itemIdPrefix, 1, 17);
    }

    @Test
    public void grid_does_not_loose_data_on_new_property_sync() {
        int size = Integer
                .valueOf(findElement(By.id("grid-with-component-renderers"))
                        .getAttribute("size"));
        findElement(By.id("toggle-column-ordering")).click();
        int updatedSize = Integer
                .valueOf(findElement(By.id("grid-with-component-renderers"))
                        .getAttribute("size"));
        Assert.assertEquals(
                "When some property is synced, grid size property should stay the same",
                size, updatedSize);
    }

    @Test
    public void openGridWithTemplateDetailsRow_clickOnItems_dataIsTransmitted() {
        TestBenchElement grid = $("*").id("grid-with-template-details-row");

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
        TestBenchElement grid = $("*").id("grid-with-component-details-row");

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

    @Test
    public void detachableGrid_changeContainers_itemsAreStillShown() {
        WebElement container1 = findElement(
                By.id("detachable-grid-container-1"));
        WebElement container2 = findElement(
                By.id("detachable-grid-container-2"));
        WebElement attach1 = findElement(By.id("detachable-grid-attach-1"));
        WebElement attach2 = findElement(By.id("detachable-grid-attach-2"));

        WebElement grid = container1.findElement(By.id("detachable-grid"));
        Map<String, Map<String, ?>> items = getItems(driver, grid);
        Assert.assertEquals(50, items.size());
        items.forEach((row, map) -> {
            Assert.assertEquals("Item " + row, map.get("col0"));
        });
        // sets a property on the $connector, to validate that the connector
        // is not reset when changing containers
        executeScript("arguments[0].$connector._isUsingTheSameInstance = true",
                grid);

        attach2.click();
        grid = container2.findElement(By.id("detachable-grid"));
        items = getItems(driver, grid);
        Assert.assertEquals(50, items.size());
        items.forEach((row, map) -> {
            Assert.assertEquals("Item " + row, map.get("col0"));
        });
        Assert.assertTrue("The $connector instance should be preserved",
                (Boolean) executeScript(
                        "return arguments[0].$connector._isUsingTheSameInstance",
                        grid));

        attach1.click();
        grid = container1.findElement(By.id("detachable-grid"));
        items = getItems(driver, grid);
        Assert.assertEquals(50, items.size());
        items.forEach((row, map) -> {
            Assert.assertEquals("Item " + row, map.get("col0"));
        });
        Assert.assertTrue("The $connector instance should be preserved",
                (Boolean) executeScript(
                        "return arguments[0].$connector._isUsingTheSameInstance",
                        grid));
    }

    @Test
    public void selectItemOnGrid_changeContainers_itemIsStillSelected() {
        TestBenchElement container1 = $("*").id("detachable-grid-container-1");
        TestBenchElement container2 = $("*").id("detachable-grid-container-2");
        WebElement attach1 = findElement(By.id("detachable-grid-attach-1"));
        WebElement attach2 = findElement(By.id("detachable-grid-attach-2"));

        TestBenchElement grid = container1
                .findElement(By.id("detachable-grid"));
        scrollToElement(grid);

        // click to select the first item
        clickElementWithJs(getRow(grid, 0).findElement(By.tagName("td")));
        assertSelection(grid, "Item 0");

        attach2.click();
        grid = container2.findElement(By.id("detachable-grid"));
        assertSelection(grid, "Item 0");
        // click to select the second item
        clickElementWithJs(getRow(grid, 1).findElement(By.tagName("td")));
        assertSelection(grid, "Item 1");

        attach1.click();
        grid = container1.findElement(By.id("detachable-grid"));
        assertSelection(grid, "Item 1");
    }

    @Test
    public void detachableGrid_detachAndReattach_itemsAreStillShown() {
        WebElement container1 = findElement(
                By.id("detachable-grid-container-1"));
        WebElement attach1 = findElement(By.id("detachable-grid-attach-1"));
        WebElement detach = findElement(By.id("detachable-grid-detach"));

        WebElement grid = container1.findElement(By.id("detachable-grid"));
        scrollToElement(grid);

        Map<String, Map<String, ?>> items = getItems(driver, grid);
        Assert.assertEquals(50, items.size());
        items.forEach((row, map) -> {
            Assert.assertEquals("Item " + row, map.get("col0"));
        });

        detach.click();
        waitForElementNotPresent(By.id("detachable-grid"));
        attach1.click();
        grid = container1.findElement(By.id("detachable-grid"));
        items = getItems(driver, grid);
        Assert.assertEquals(50, items.size());
        items.forEach((row, map) -> {
            Assert.assertEquals("Item " + row, map.get("col0"));
        });
    }

    @Test
    public void selectItemOnGrid_detachAndReattach_itemIsStillSelected() {
        TestBenchElement container1 = $("*").id("detachable-grid-container-1");
        WebElement attach1 = findElement(By.id("detachable-grid-attach-1"));
        WebElement detach = findElement(By.id("detachable-grid-detach"));

        TestBenchElement grid = container1
                .findElement(By.id("detachable-grid"));
        scrollToElement(grid);

        // click to select the first item
        clickElementWithJs(getRow(grid, 0).findElement(By.tagName("td")));
        assertSelection(grid, "Item 0");

        detach.click();
        waitForElementNotPresent(By.id("detachable-grid"));
        attach1.click();
        grid = container1.findElement(By.id("detachable-grid"));
        assertSelection(grid, "Item 0");
    }

    @Test
    public void detachableGrid_setInvisibleAndVisible_itemsAreStillShown() {
        WebElement container1 = findElement(
                By.id("detachable-grid-container-1"));
        WebElement invisible = findElement(By.id("detachable-grid-invisible"));
        WebElement visible = findElement(By.id("detachable-grid-visible"));

        WebElement grid = container1.findElement(By.id("detachable-grid"));
        scrollToElement(grid);

        Map<String, Map<String, ?>> items = getItems(driver, grid);
        Assert.assertEquals(50, items.size());
        items.forEach((row, map) -> {
            Assert.assertEquals("Item " + row, map.get("col0"));
        });
        // sets a property on the $connector, to validate that the connector
        // is not reset when changing visibility
        executeScript("arguments[0].$connector._isUsingTheSameInstance = true",
                grid);

        invisible.click();
        waitUntil(driver -> "true".equals(grid.getAttribute("hidden")));
        visible.click();
        waitUntil(driver -> grid.getAttribute("hidden") == null);
        items = getItems(driver, grid);
        Assert.assertEquals(50, items.size());
        items.forEach((row, map) -> {
            Assert.assertEquals("Item " + row, map.get("col0"));
        });
        Assert.assertTrue("The $connector instance should be preserved",
                (Boolean) executeScript(
                        "return arguments[0].$connector._isUsingTheSameInstance",
                        grid));
    }

    @Test
    public void detachableGrid_setInvisibleAndVisible_itemIsStillSelected() {
        TestBenchElement container1 = $("*").id("detachable-grid-container-1");
        WebElement invisible = findElement(By.id("detachable-grid-invisible"));
        WebElement visible = findElement(By.id("detachable-grid-visible"));

        TestBenchElement grid = container1
                .findElement(By.id("detachable-grid"));
        scrollToElement(grid);

        // click to select the first item
        clickElementWithJs(getRow(grid, 0).findElement(By.tagName("td")));
        assertSelection(grid, "Item 0");

        invisible.click();
        waitUntil(driver -> "true".equals(grid.getAttribute("hidden")));
        visible.click();
        waitUntil(driver -> grid.getAttribute("hidden") == null);
        assertSelection(grid, "Item 0");
    }

    @Test
    public void scrollDown_detachAndReattach_firstItemsRendered() {
        GridElement grid = $(GridElement.class).id("detachable-grid");
        grid.scrollToRow(150);

        findElement(By.id("detachable-grid-detach")).click();
        findElement(By.id("detachable-grid-attach-1")).click();

        grid = $(GridElement.class).id("detachable-grid");
        Assert.assertEquals(50, getItems(driver, grid).size());
        Assert.assertEquals("Item 0", grid.getCell(0, 0).getText());
    }

    @Test
    public void mockedColumnReorderEvent_smokeTest() {
        findElement(By.id("toggle-column-ordering")).click();
        GridElement grid = $(GridElement.class)
                .id("grid-with-component-renderers");
        grid.getCommandExecutor().executeScript(
                "arguments[0].dispatchEvent(new CustomEvent('column-reorder'));",
                grid.getWrappedElement());

        final WebElement currentColumnOrdering = findElement(
                By.id("current-column-ordering"));
        Assert.assertEquals("name, number, remove, hidden",
                currentColumnOrdering.getText());
    }

    @Test
    public void mockedColumnReorderEvent_setNewOrder() {
        findElement(By.id("toggle-column-ordering")).click();
        GridElement grid = $(GridElement.class)
                .id("grid-with-component-renderers");
        findElement(By.id("set-reorder-listener")).click();
        grid.getCommandExecutor().executeScript(
                "arguments[0].dispatchEvent(new CustomEvent('column-reorder'));",
                grid.getWrappedElement());

        final WebElement currentColumnOrdering = findElement(
                By.id("current-column-ordering"));
        Assert.assertEquals("name, remove, number, hidden",
                currentColumnOrdering.getText());
    }

    @Test
    public void gridWithNoConnector_noExceptions() {
        executeScript(
                "document.body.appendChild(document.createElement('vaadin-grid'));");
        checkLogsForErrors();
    }

    private void assertSelection(WebElement grid, String value) {
        Assert.assertTrue(value + " should be selected",
                (Boolean) executeScript(
                        "return arguments[0].selectedItems[0].col0 === arguments[1]",
                        grid, value));
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

    @SuppressWarnings("unchecked")
    public static Map<String, Map<String, ?>> getItems(WebDriver driver,
            WebElement element) {
        Object result = ((JavascriptExecutor) driver)
                .executeScript("return arguments[0]._cache.items;", element);

        return (Map<String, Map<String, ?>>) result;
    }

    private WebElement getRow(TestBenchElement grid, int row) {
        return grid.$("*").id("items").findElements(By.cssSelector("tr"))
                .get(row);
    }

}
