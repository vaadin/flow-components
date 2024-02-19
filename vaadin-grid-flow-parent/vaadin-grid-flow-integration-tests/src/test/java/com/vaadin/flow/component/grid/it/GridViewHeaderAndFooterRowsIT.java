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

import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid-it-demo/header-and-footer-rows")
public class GridViewHeaderAndFooterRowsIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void gridWithHeaderAndFooterRows_headerAndFooterAreRenderered() {
        GridElement grid = $(GridElement.class)
                .id("grid-with-header-and-footer-rows");
        scrollToElement(grid);

        assertRenderedHeaderCell(grid.getHeaderCell(0), "Name", true);
        assertRenderedHeaderCell(grid.getHeaderCell(1), "Age", true);
        assertRenderedHeaderCell(grid.getHeaderCell(2), "Street", false);
        assertRenderedHeaderCell(grid.getHeaderCell(3), "Postal Code", false);

        Assert.assertTrue(
                "The first column group should have 'Basic Information' header text",
                grid.getHeaderCellContent(0, 0).getText()
                        .contains("Basic Information"));

        Assert.assertTrue(
                "The second column group should have 'Address Information' header text",
                grid.getHeaderCellContent(0, 1).getText()
                        .contains("Address Information"));

        Assert.assertTrue("There should be a cell with the renderered footer",
                grid.getFooterCell(0).getText().contains("Total: 500 people"));
    }

    @Test
    public void gridWithHeaderWithComponentRenderer_headerAndFooterAreRenderered() {
        GridElement grid = $(GridElement.class)
                .id("grid-header-with-components");
        scrollToElement(grid);

        GridTHTDElement headerCell = grid.getHeaderCell(0);
        assertRenderedHeaderCell(headerCell, "<span>Name</span>", true);

        headerCell = grid.getHeaderCell(1);
        assertRenderedHeaderCell(headerCell, "<span>Age</span>", true);

        headerCell = grid.getHeaderCell(2);
        assertRenderedHeaderCell(headerCell, "<span>Street</span>", false);

        headerCell = grid.getHeaderCell(3);
        assertRenderedHeaderCell(headerCell, "<span>Postal Code</span>", false);

        Assert.assertTrue(
                "There should be a cell with the renderered 'Basic Information' header",
                hasComponentRenderedHeaderCell(grid, "span",
                        "Basic Information"));

        Assert.assertTrue("There should be a cell with the renderered footer",
                hasComponentRenderedHeaderCell(grid, "span",
                        "Total: 500 people"));
    }

    @Test
    public void setMultiSelect_appendHeader_selectAllCheckboxOnDefaultHeaderRow() {
        GridElement grid = setupMultiSelectGrid();
        $("button").id("append-header").click();
        Assert.assertTrue(getHeaderCell(grid, 1, 0).getInnerHTML()
                .contains("selectAllCheckbox"));
        Assert.assertFalse(getHeaderCell(grid, 2, 0).getInnerHTML()
                .contains("selectAllCheckbox"));
    }

    @Test
    public void setMultiSelect_prependHeader_selectAllCheckboxOnDefaultHeaderRow() {
        GridElement grid = setupMultiSelectGrid();
        $("button").id("prepend-header").click();
        Assert.assertTrue(getHeaderCell(grid, 2, 0).getInnerHTML()
                .contains("selectAllCheckbox"));
        Assert.assertFalse(getHeaderCell(grid, 1, 0).getInnerHTML()
                .contains("selectAllCheckbox"));
    }

    @Test
    public void setMultiSelect_removeAllHeaders_appendHeader_selectAllCheckboxOnFirstHeaderRow() {
        GridElement grid = setupMultiSelectGrid();
        $("button").id("remove-all-headers").click();
        $("button").id("append-header").click();
        Assert.assertTrue(getHeaderCell(grid, 0, 0).getInnerHTML()
                .contains("selectAllCheckbox"));
    }

    private void assertRenderedHeaderCell(GridTHTDElement headerCell,
            String text, boolean withSorter) {

        String html = headerCell.getInnerHTML();
        if (withSorter) {
            Assert.assertTrue(html.contains("<vaadin-grid-sorter"));
        } else {
            Assert.assertFalse(html.contains("<vaadin-grid-sorter"));
        }
        Assert.assertTrue(html.contains(text));
    }

    private boolean hasComponentRenderedHeaderCell(WebElement grid,
            String tagName, String text) {
        return hasComponentRenderedCell(grid, text, tagName);
    }

    private boolean hasComponentRenderedCell(WebElement grid, String text,
            String componentTag) {
        List<WebElement> cells = grid
                .findElements(By.tagName("vaadin-grid-cell-content"));

        return cells.stream()
                .map(cell -> cell.findElements(By.tagName(componentTag)))
                .filter(list -> !list.isEmpty()).map(list -> list.get(0))
                .anyMatch(cell -> text.equals(cell.getAttribute("innerHTML")));
    }

    private GridElement setupMultiSelectGrid() {
        findElement(By.id("selection-mode"))
                .findElements(By.tagName("vaadin-radio-button")).get(1).click();
        GridElement grid = $(GridElement.class)
                .id("grid-with-header-and-footer-rows");
        scrollToElement(grid);
        return grid;
    }

    private GridTHTDElement getHeaderCell(GridElement grid, int headerRowIndex,
            int columnIndex) {
        return ((TestBenchElement) executeScript(
                "return arguments[0].$.header.children[arguments[1]].children[arguments[2]]",
                grid, headerRowIndex, columnIndex)).wrap(GridTHTDElement.class);
    }
}
