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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("grid-item-refresh-page")
public class GridItemRefreshPageIT extends AbstractComponentIT {

    @Test
    public void updateAndRefreshItemsOnTheServerUsingDataProvider_withTemplateRenderer() {
        updateAndRefreshItemsOnTheServer("template-grid",
                "template-refresh-first", "template-refresh-multiple",
                "template-refresh-all");
    }

    @Test
    public void updateAndRefreshItemsOnTheServerUsingDataCommunicator_withTemplateRenderer() {
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

    private void updateAndRefreshItemsOnTheServer(String gridId,
            String refreshFirstItemButtonId,
            String refreshMultipleItemsButtonId, String refreshAllButtonId) {
        open();
        WebElement grid = findElement(By.id(gridId));
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
        waitUntilUpdated(grid, 10, 15);

        getCommandExecutor().executeScript("arguments[0].scrollToIndex(1000)",
                grid);
        // rows at the bottom (outside of the initial cache) should also be
        // updated
        waitUntilUpdated(grid, 990, 999);
    }

    private void waitUntilUpdated(WebElement grid, int startIndex,
            int lastIndex) {
        Set<String> expected = IntStream.range(startIndex, lastIndex + 1)
                .mapToObj(intVal -> "updated " + String.valueOf(intVal))
                .collect(Collectors.toSet());
        waitUntil(driver -> grid
                .findElements(By.tagName("vaadin-grid-cell-content")).stream()
                .map(this::getContentIfComponentRenderered)
                .collect(Collectors.toSet()).containsAll(expected));
    }

    private void assertNotUpdated(WebElement grid, int startIndex,
            int lastIndex) {
        Set<String> expected = IntStream.range(startIndex, lastIndex + 1)
                .mapToObj(intVal -> "updated " + String.valueOf(intVal))
                .collect(Collectors.toSet());
        Assert.assertFalse(
                grid.findElements(By.tagName("vaadin-grid-cell-content"))
                        .stream().map(this::getContentIfComponentRenderered)
                        .collect(Collectors.toSet()).removeAll(expected));
    }

    private String getContentIfComponentRenderered(WebElement cell) {
        List<WebElement> renderer = cell
                .findElements(By.tagName("flow-grid-component-renderer"));
        if (renderer.isEmpty()) {
            return cell.getAttribute("innerHTML");
        }
        List<WebElement> label = renderer.get(0)
                .findElements(By.tagName("label"));
        if (label.isEmpty()) {
            return "";
        }
        return label.get(0).getAttribute("innerHTML");
    }
}
