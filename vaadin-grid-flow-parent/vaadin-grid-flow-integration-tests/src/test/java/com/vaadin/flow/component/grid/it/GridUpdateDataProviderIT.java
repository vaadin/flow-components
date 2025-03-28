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
package com.vaadin.flow.component.grid.it;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/grid-page")
public class GridUpdateDataProviderIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();

        waitForElementPresent(By.tagName("vaadin-grid"));
    }

    @Test
    public void basicGrid() {
        WebElement grid = findElement(By.id("basic-grid"));
        hasCell(grid, "text");
        hasCell(grid, "0");
        hasCell(grid, "1");

        scrollDown(grid, 1045);

        waitUntil(driver -> hasCell(grid, "1050"));
        hasCell(grid, "4");
    }

    @Test
    public void basicGrid_defaultPageSize() {
        WebElement grid = findElement(By.id("basic-grid"));
        Object pageSize = executeScript("return arguments[0].pageSize", grid);
        Assert.assertEquals(
                "The default pageSize of the webcomponent should be 50", 50,
                Integer.parseInt(String.valueOf(pageSize)));
    }

    @Test
    public void basicGrid_changeDataProvider() {
        WebElement grid = findElement(By.id("basic-grid"));
        // change data provider
        findElement(By.id("update-basic-provider")).click();
        waitUntil(driver -> hasData());

        // change data provider again
        findElement(By.id("update-basic-provider")).click();
        waitUntil(driver -> hasCell(grid, "text"));
    }

    @Test
    public void emptyGrid_setItemsAndPageSize() {
        WebElement grid = findElement(By.id("empty-grid"));
        findElement(By.id("set-items-and-page-size")).click();

        waitUntil(driver -> hasCell(grid, "foo"));
        waitUntil(driver -> hasCell(grid, "bar"));
    }

    @Test
    public void beanGrid_changeDataProvider() {
        WebElement grid = findElement(By.id("bean-grid"));
        waitUntil(driver -> hasCell(grid, "foo"));

        findElement(By.id("update-bean-provider")).click();
        waitUntil(driver -> hasCell(grid, "FOOBAR"));
    }

    private void scrollDown(WebElement grid, int index) {
        getCommandExecutor().executeScript(
                "arguments[0].scrollToIndex(" + index + ")", grid);
    }

    private boolean hasCell(WebElement grid, String text) {
        List<WebElement> cells = grid
                .findElements(By.tagName("vaadin-grid-cell-content"));
        return cells.stream().filter(cell -> text.equals(cell.getText()))
                .findAny().isPresent();
    }

    private boolean hasData() {
        Set<String> data = new HashSet<>();
        data.add("foo");
        data.add("foob");
        data.add("fooba");
        data.add("foobar");
        Collection<String> lengths = data.stream().map(String::length)
                .map(Object::toString).collect(Collectors.toList());
        data.addAll(lengths);
        findElements(By.tagName("vaadin-grid-cell-content"))
                .forEach(cell -> data.remove(cell.getText()));
        return data.isEmpty();
    }
}
