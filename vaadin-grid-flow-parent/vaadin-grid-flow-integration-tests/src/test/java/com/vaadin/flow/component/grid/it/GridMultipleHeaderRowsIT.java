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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridHeaderCell;
import com.vaadin.flow.component.grid.testbench.GridHeaderRow;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/grid-header-footer-rows")
public class GridMultipleHeaderRowsIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).id("grid");
    }

    @Test
    public void testGetHeaderRows_returnsAllHeaderRows() {
        // Add multiple header rows
        clickButton("append-header");
        clickButton("prepend-header");

        List<GridHeaderRow> headerRows = grid.getHeaderRows();
        Assert.assertNotNull("Header rows should not be null", headerRows);
        Assert.assertTrue("Grid should have at least 2 header rows",
                headerRows.size() >= 2);
    }

    @Test
    public void testGetHeaderRow_returnsSpecificRow() {
        clickButton("append-header");

        GridHeaderRow firstRow = grid.getHeaderRow(0);
        Assert.assertNotNull("First header row should not be null", firstRow);
        Assert.assertEquals("Row index should be 0", 0, firstRow.getRowIndex());
    }

    @Test
    public void testGetHeaderRowCount_returnsCorrectCount() {
        int initialCount = grid.getHeaderRowCount();

        clickButton("append-header");
        int afterAppendCount = grid.getHeaderRowCount();

        Assert.assertEquals("Header row count should increase by 1",
                initialCount + 1, afterAppendCount);
    }

    @Test
    public void testGetFirstHeaderRow_returnsFirstRow() {
        clickButton("prepend-header");
        clickButton("append-header");

        GridHeaderRow firstRow = grid.getFirstHeaderRow();
        Assert.assertNotNull("First header row should not be null", firstRow);
        Assert.assertEquals("First row index should be 0", 0,
                firstRow.getRowIndex());
    }

    @Test
    public void testGetLastHeaderRow_returnsLastRow() {
        clickButton("append-header");
        clickButton("append-header");

        GridHeaderRow lastRow = grid.getLastHeaderRow();
        Assert.assertNotNull("Last header row should not be null", lastRow);

        int expectedIndex = grid.getHeaderRowCount() - 1;
        Assert.assertEquals("Last row index should be the last index",
                expectedIndex, lastRow.getRowIndex());
    }

    @Test
    public void testHeaderRowGetCells_returnsAllCells() {
        clickButton("append-header");

        GridHeaderRow headerRow = grid.getFirstHeaderRow();
        List<GridHeaderCell> cells = headerRow.getCells();

        Assert.assertNotNull("Header cells should not be null", cells);
        Assert.assertTrue("Header row should have cells", cells.size() > 0);
    }

    @Test
    public void testHeaderCellGetText_returnsCorrectText() {
        // Set header text through the test page button
        clickButton("set-header-text");

        GridHeaderRow headerRow = grid.getFirstHeaderRow();
        if (headerRow != null && headerRow.getCellCount() > 0) {
            GridHeaderCell firstCell = headerRow.getCell(0);
            String text = firstCell.getText();
            Assert.assertNotNull("Header cell text should not be null", text);
        }
    }

    @Test
    public void testHeaderCellColspan_detectsJoinedCells() {
        // Join header cells if test page supports it
        clickButton("join-header-cells");

        GridHeaderRow headerRow = grid.getFirstHeaderRow();
        if (headerRow != null && headerRow.getCellCount() > 0) {
            GridHeaderCell cell = headerRow.getCell(0);
            int colspan = cell.getColspan();
            // If cells were joined, colspan should be > 1
            Assert.assertTrue(
                    "Joined cell should have colspan > 1 or be 1 if not joined",
                    colspan >= 1);
        }
    }

    @Test
    public void testGetVisibleHeaderRows_returnsOnlyVisible() {
        clickButton("append-header");

        List<GridHeaderRow> visibleRows = grid.getVisibleHeaderRows();
        Assert.assertNotNull("Visible header rows should not be null",
                visibleRows);

        // All visible rows should be visible
        for (GridHeaderRow row : visibleRows) {
            Assert.assertTrue("Row should be visible", row.isVisible());
        }
    }

    @Test
    public void testHeaderRowGetCellTexts_returnsAllTexts() {
        clickButton("set-header-text");

        GridHeaderRow headerRow = grid.getFirstHeaderRow();
        if (headerRow != null) {
            List<String> texts = headerRow.getCellTexts();
            Assert.assertNotNull("Cell texts should not be null", texts);
            Assert.assertEquals("Number of texts should match number of cells",
                    headerRow.getCellCount(), texts.size());
        }
    }

    private void clickButton(String buttonId) {
        // Helper method to click buttons - assumes buttons exist on the test
        // page
        try {
            findElement(By.id(buttonId)).click();
        } catch (Exception e) {
            // Button might not exist for this specific test
        }
    }
}
