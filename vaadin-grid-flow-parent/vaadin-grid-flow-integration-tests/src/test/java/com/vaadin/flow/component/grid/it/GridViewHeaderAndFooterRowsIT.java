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

        assertRendereredHeaderCell(grid.getHeaderCell(0), "Name", false, true);
        assertRendereredHeaderCell(grid.getHeaderCell(1), "Age", false, true);
        assertRendereredHeaderCell(grid.getHeaderCell(2), "Street", false,
                false);
        assertRendereredHeaderCell(grid.getHeaderCell(3), "Postal Code", false,
                false);

        List<WebElement> columnGroups = grid
                .findElements(By.tagName("vaadin-grid-column-group"));

        Assert.assertTrue(
                "The first column group should have 'Basic Information' header text",
                columnGroups.get(0).getAttribute("innerHTML")
                        .contains("Basic Information"));

        Assert.assertTrue(
                "The second column group should have 'Address Information' header text",
                columnGroups.get(1).getAttribute("innerHTML")
                        .contains("Address Information"));

        List<WebElement> columns = grid
                .findElements(By.tagName("vaadin-grid-column"));

        Assert.assertTrue("There should be a cell with the renderered footer",
                columns.get(0).getAttribute("innerHTML")
                        .contains("Total: 500 people"));
    }

    @Test
    public void gridWithHeaderWithComponentRenderer_headerAndFooterAreRenderered() {
        GridElement grid = $(GridElement.class)
                .id("grid-header-with-components");
        scrollToElement(grid);

        GridTHTDElement headerCell = grid.getHeaderCell(0);
        assertRendereredHeaderCell(headerCell, "<span>Name</span>", true, true);

        headerCell = grid.getHeaderCell(1);
        assertRendereredHeaderCell(headerCell, "<span>Age</span>", true, true);

        headerCell = grid.getHeaderCell(2);
        assertRendereredHeaderCell(headerCell, "<span>Street</span>", true,
                false);

        headerCell = grid.getHeaderCell(3);
        assertRendereredHeaderCell(headerCell, "<span>Postal Code</span>", true,
                false);

        Assert.assertTrue(
                "There should be a cell with the renderered 'Basic Information' header",
                hasComponentRendereredHeaderCell(grid,
                        "<span>Basic Information</span>"));

        Assert.assertTrue("There should be a cell with the renderered footer",
                hasComponentRendereredHeaderCell(grid,
                        "<span>Total: 500 people</span>"));
    }

    private void assertRendereredHeaderCell(GridTHTDElement headerCell,
            String text, boolean componentRenderer, boolean withSorter) {

        String html = headerCell.getInnerHTML();
        if (withSorter) {
            Assert.assertTrue(html.contains("<vaadin-grid-sorter"));
        } else {
            Assert.assertFalse(html.contains("<vaadin-grid-sorter"));
        }
        if (componentRenderer) {
            Assert.assertTrue(html.contains("<flow-component-renderer"));
        }
        Assert.assertTrue(html.contains(text));
    }

    private boolean hasComponentRendereredHeaderCell(WebElement grid,
            String text) {
        return hasComponentRendereredCell(grid, text,
                "flow-component-renderer");
    }

    private boolean hasComponentRendereredCell(WebElement grid, String text,
            String componentTag) {
        List<WebElement> cells = grid
                .findElements(By.tagName("vaadin-grid-cell-content"));

        return cells.stream()
                .map(cell -> cell.findElements(By.tagName(componentTag)))
                .filter(list -> !list.isEmpty()).map(list -> list.get(0))
                .anyMatch(cell -> text.equals(cell.getAttribute("innerHTML")));
    }
}
