/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/grid-item-refresh-page")
public class GridItemRefreshPageIT extends AbstractComponentIT {

    @Test
    public void updateAndRefreshItemsOnTheServerUsingDataProvider_withLitRenderer() {
        updateAndRefreshItemsOnTheServer("template-grid",
                "template-refresh-first", "template-refresh-multiple",
                "template-refresh-all");
    }

    @Test
    public void updateAndRefreshItemsOnTheServerUsingDataCommunicator_withLitRenderer() {
        updateAndRefreshItemsOnTheServer("template-grid",
                "template-refresh-first-communicator",
                "template-refresh-multiple-communicator",
                "template-reset-communicator");
    }

    @Test
    public void updateAndRefreshItemsOnTheServerUsingDataProvider_withComponentRenderer() {
        updateAndRefreshItemsOnTheServer("component-grid",
                "component-refresh-first", "component-refresh-multiple",
                "component-refresh-all");
    }

    @Test
    public void updateAndRefreshItemsOnTheServerUsingDataCommunicator_withComponentRenderer() {
        updateAndRefreshItemsOnTheServer("component-grid",
                "component-refresh-first-communicator",
                "component-refresh-multiple-communicator",
                "component-reset-communicator");
    }

    @Test
    public void dataProviderRefreshItem_gridDoesntTouchOtherItemsOnDOM() {
        open();

        GridElement grid = $(GridElement.class).first();
        GridTHTDElement firstRowCell = grid.getCell(0, 1);

        Assert.assertEquals("Invalid cell content", "0",
                firstRowCell.getText());
        Assert.assertEquals("Invalid cell content", "5",
                grid.getCell(5, 1).getText());

        // click the next cell to get the value updated in the DOM, but not in
        // the cache
        grid.findElement(By.id("div-0")).click();
        Assert.assertEquals("Invalid cell content", "EDITED",
                firstRowCell.getText());

        // refreshing items 5-10 should only effect those sells, but not cell on
        // row 0
        // before the fix for #419 all visible rows in DOM were refreshed in the
        // grid all the time
        findElement(By.id("template-refresh-multiple")).click();

        Assert.assertEquals("Cell content should have updated", "12345",
                grid.getCell(5, 1).getText());
        Assert.assertEquals("Cell content should have not updated", "EDITED",
                firstRowCell.getText());

        grid.findElement(By.id("div-5")).click();
        Assert.assertEquals("Invalid cell content", "EDITED",
                grid.getCell(5, 1).getText());

        findElement(By.id("template-refresh-first")).click();

        Assert.assertEquals("Cell content should have updated", "12345",
                firstRowCell.getText());
        Assert.assertEquals("Cell content should have not updated", "EDITED",
                grid.getCell(5, 1).getText());
    }

    private void updateAndRefreshItemsOnTheServer(String gridId,
            String refreshFirstItemButtonId,
            String refreshMultipleItemsButtonId, String refreshAllButtonId) {
        open();
        GridElement grid = $(GridElement.class).id(gridId);
        scrollToElement(grid);

        WebElement refreshFirstItem = findElement(
                By.id(refreshFirstItemButtonId));
        WebElement refreshMultipleItems = findElement(
                By.id(refreshMultipleItemsButtonId));
        WebElement refreshAll = findElement(By.id(refreshAllButtonId));

        assertNotUpdated(grid, 0, 0);
        clickElementWithJs(refreshFirstItem);
        waitUntilUpdated(grid, 0, 0);

        assertNotUpdated(grid, 4, 9);
        clickElementWithJs(refreshMultipleItems);
        waitUntilUpdated(grid, 4, 9);

        assertNotUpdated(grid, 10, 15);
        clickElementWithJs(refreshAll);
        try {
            // Sleep for a while so we don't check the dom when the elements
            // are being removed/disconnected as it leads to Stale Element
            // Reference exceptions
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // NOOP: thread was woken
        }
        waitUntilUpdated(grid, 10, 15);

        // Scroll down
        grid.scrollToRow(500);

        // rows outside of the initial cache should also be updated
        waitUntilUpdated(grid, 500, 505);
    }

    private void waitUntilUpdated(WebElement grid, int startIndex,
            int lastIndex) {
        Set<String> expected = IntStream.range(startIndex, lastIndex + 1)
                .mapToObj(intVal -> "updated " + String.valueOf(intVal))
                .collect(Collectors.toSet());
        waitUntil(driver -> grid
                .findElements(By.tagName("vaadin-grid-cell-content")).stream()
                .map(WebElement::getText).collect(Collectors.toSet())
                .containsAll(expected));
    }

    private void assertNotUpdated(WebElement grid, int startIndex,
            int lastIndex) {
        Set<String> expected = IntStream.range(startIndex, lastIndex + 1)
                .mapToObj(intVal -> "updated " + String.valueOf(intVal))
                .collect(Collectors.toSet());
        Assert.assertFalse(
                grid.findElements(By.tagName("vaadin-grid-cell-content"))
                        .stream().map(WebElement::getText)
                        .collect(Collectors.toSet()).removeAll(expected));
    }
}
