/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Tests for dynamically adding new columns with different renderers after the
 * Grid has already been attached and rendered.
 */
@TestPath("vaadin-grid/component-columns")
public class ComponentColumnsIT extends AbstractComponentIT {

    private GridElement gridThenComp;
    private GridElement compThenGrid;

    @Before
    public void init() {
        open();
        clickElementWithJs("btn-add-comp-then-grid");
        clickElementWithJs("btn-add-grid-then-comp");

        gridThenComp = $(GridElement.class).id("grid-then-comp");
        compThenGrid = $(GridElement.class).id("comp-then-grid");
    }

    @Test
    public void gridThenCompRendered_nativeButton() {
        assertCellContents(gridThenComp, 0, 0, "<button>click</button>");
        assertCellContents(gridThenComp, 1, 0, "<button>click</button>");
    }

    @Test
    public void compThenGridRendered_nativeButton() {
        assertCellContents(compThenGrid, 0, 0, "<button>click</button>");
        assertCellContents(compThenGrid, 1, 0, "<button>click</button>");
    }

    @Test
    public void compThenGridRendered_compButton() {
        assertCellContains(compThenGrid, 0, 1, "foo");
        assertCellContains(compThenGrid, 0, 1, "vaadin-button");
        assertCellContains(compThenGrid, 1, 1, "bar");
        assertCellContains(compThenGrid, 1, 1, "vaadin-button");
    }

    @Test
    public void gridThenCompRendered_compButton() {
        assertCellContains(gridThenComp, 0, 1, "foo");
        assertCellContains(gridThenComp, 0, 1, "vaadin-button");
        assertCellContains(gridThenComp, 1, 1, "bar");
        assertCellContains(gridThenComp, 1, 1, "vaadin-button");
    }

    private void assertCellContents(GridElement grid, int rowIndex,
            int colIndex, String expected) {
        Assert.assertEquals(expected, TestHelper.stripComments(
                grid.getCell(rowIndex, colIndex).getInnerHTML()));
    }

    private void assertCellContains(GridElement grid, int rowIndex,
            int colIndex, String expected) {
        Assert.assertTrue("Expected cell content to contain: " + expected, grid
                .getCell(rowIndex, colIndex).getInnerHTML().contains(expected));
    }

    @Test
    public void gridWithComponentRenderer_cellsAreRenderered() {
        WebElement grid = findElement(By.id("component-renderer"));
        scrollToElement(grid);

        Assert.assertTrue(
                hasComponentRendereredCell(grid, "Hi, I'm Person 1!", "div"));
        Assert.assertTrue(
                hasComponentRendereredCell(grid, "Hi, I'm Person 2!", "div"));

        WebElement idField = findElement(By.id("component-renderer-id-field"));
        WebElement nameField = findElement(
                By.id("component-renderer-name-field"));
        WebElement updateButton = findElement(
                By.id("component-renderer-update-button"));

        idField.sendKeys("1");
        executeScript("arguments[0].blur();", idField);
        nameField.sendKeys("SomeOtherName");
        executeScript("arguments[0].blur();", nameField);
        clickElementWithJs(updateButton);

        waitUntil(driver -> hasComponentRendereredCell(grid,
                "Hi, I'm SomeOtherName!", "div"));

        idField.sendKeys(Keys.BACK_SPACE, "2");
        executeScript("arguments[0].blur();", idField);
        nameField.sendKeys("2");
        executeScript("arguments[0].blur();", nameField);
        clickElementWithJs(updateButton);

        waitUntil(driver -> hasComponentRendereredCell(grid,
                "Hi, I'm SomeOtherName2!", "div"));
    }

    @Test
    public void gridWithComponentRenderer_detailsAreRenderered() {
        GridElement grid = $(GridElement.class).id("component-renderer");
        scrollToElement(grid);

        clickElementWithJs(getRow(grid, 0).findElement(By.tagName("td")));
        assertComponentRendereredDetails(grid, 0, "Person 1");

        clickElementWithJs(getRow(grid, 1).findElement(By.tagName("td")));
        assertComponentRendereredDetails(grid, 1, "Person 2");

        WebElement idField = findElement(By.id("component-renderer-id-field"));
        WebElement nameField = findElement(
                By.id("component-renderer-name-field"));
        WebElement updateButton = findElement(
                By.id("component-renderer-update-button"));

        idField.sendKeys("1");
        executeScript("arguments[0].blur();", idField);
        nameField.sendKeys("SomeOtherName");
        executeScript("arguments[0].blur();", nameField);
        clickElementWithJs(updateButton);

        clickElementWithJs(getRow(grid, 0).findElement(By.tagName("td")));
        assertComponentRendereredDetails(grid, 0, "SomeOtherName");

        idField.sendKeys(Keys.BACK_SPACE, "2");
        executeScript("arguments[0].blur();", idField);
        nameField.sendKeys("2");
        executeScript("arguments[0].blur();", nameField);
        clickElementWithJs(updateButton);

        clickElementWithJs(getRow(grid, 1).findElement(By.tagName("td")));
        assertComponentRendereredDetails(grid, 1, "SomeOtherName2");
    }

    private WebElement getRow(TestBenchElement grid, int row) {
        return grid.$("*").id("items").findElements(By.cssSelector("tr"))
                .get(row);
    }

    private boolean hasComponentRendereredCell(WebElement grid, String text,
            String componentTag) {
        List<WebElement> cells = grid
                .findElements(By.tagName("vaadin-grid-cell-content"));

        return cells.stream()
                .map(cell -> cell.findElements(By.tagName(componentTag)))
                .filter(list -> !list.isEmpty()).map(list -> list.get(0))
                .anyMatch(
                        cell -> text.equals(cell.getDomProperty("innerHTML")));
    }

    private void assertComponentRendereredDetails(WebElement grid, int rowIndex,
            String personName) {
        waitUntil(driver -> isElementPresent(
                By.id("person-card-" + (rowIndex + 1))), 20);

        WebElement element = findElement(
                By.id("person-card-" + (rowIndex + 1)));

        element = element.findElement(By.tagName("vaadin-horizontal-layout"));
        Assert.assertNotNull(element);

        List<WebElement> layouts = element
                .findElements(By.tagName("vaadin-vertical-layout"));
        Assert.assertNotNull(layouts);
        Assert.assertEquals(2, layouts.size());

        Pattern pattern = Pattern.compile("<span>Name:\\s?([\\w\\s]*)</span>");
        Matcher innerHTML = pattern
                .matcher(layouts.get(0).getDomProperty("innerHTML"));
        Assert.assertTrue(
                "No result found for " + pattern.toString()
                        + " when searching for name: " + personName,
                innerHTML.lookingAt());
        Assert.assertEquals("Expected name was not same as found one.",
                personName, innerHTML.group(1));
    }

}
