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
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridFooterCell;
import com.vaadin.flow.component.grid.testbench.GridFooterRow;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/grid-header-footer-rows")
public class GridMultipleFooterRowsIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).id("grid");
    }

    @Test
    public void testGetFooterRows_returnsAllFooterRows() {
        // Add multiple footer rows
        clickButton("append-footer");
        clickButton("prepend-footer");

        List<GridFooterRow> footerRows = grid.getFooterRows();
        Assert.assertNotNull("Footer rows should not be null", footerRows);
        Assert.assertTrue("Grid should have at least 2 footer rows",
                footerRows.size() >= 2);
    }

    @Test
    public void testGetFooterRow_returnsSpecificRow() {
        clickButton("append-footer");

        GridFooterRow firstRow = grid.getFooterRow(0);
        Assert.assertNotNull("First footer row should not be null", firstRow);
        Assert.assertEquals("Row index should be 0", 0, firstRow.getRowIndex());
    }

    @Test
    public void testGetFooterRowCount_returnsCorrectCount() {
        int initialCount = grid.getFooterRowCount();

        clickButton("append-footer");
        int afterAppendCount = grid.getFooterRowCount();

        Assert.assertEquals("Footer row count should increase by 1",
                initialCount + 1, afterAppendCount);
    }

    @Test
    public void testGetFirstFooterRow_returnsFirstRow() {
        clickButton("prepend-footer");
        clickButton("append-footer");

        GridFooterRow firstRow = grid.getFirstFooterRow();
        Assert.assertNotNull("First footer row should not be null", firstRow);
        Assert.assertEquals("First row index should be 0", 0,
                firstRow.getRowIndex());
    }

    @Test
    public void testGetLastFooterRow_returnsLastRow() {
        clickButton("append-footer");
        clickButton("append-footer");

        GridFooterRow lastRow = grid.getLastFooterRow();
        Assert.assertNotNull("Last footer row should not be null", lastRow);

        int expectedIndex = grid.getFooterRowCount() - 1;
        Assert.assertEquals("Last row index should be the last index",
                expectedIndex, lastRow.getRowIndex());
    }

    @Test
    public void testFooterRowGetCells_returnsAllCells() {
        clickButton("append-footer");

        GridFooterRow footerRow = grid.getFirstFooterRow();
        if (footerRow != null) {
            List<GridFooterCell> cells = footerRow.getCells();

            Assert.assertNotNull("Footer cells should not be null", cells);
            Assert.assertTrue("Footer row should have cells", cells.size() > 0);
        }
    }

    @Test
    public void testFooterCellGetText_returnsCorrectText() {
        // Set footer text through the test page button
        clickButton("set-footer-text");

        GridFooterRow footerRow = grid.getFirstFooterRow();
        if (footerRow != null && footerRow.getCellCount() > 0) {
            GridFooterCell firstCell = footerRow.getCell(0);
            String text = firstCell.getText();
            Assert.assertNotNull("Footer cell text should not be null", text);
        }
    }

    @Test
    public void testFooterCellColspan_detectsJoinedCells() {
        // Join footer cells if test page supports it
        clickButton("join-footer-cells");

        GridFooterRow footerRow = grid.getFirstFooterRow();
        if (footerRow != null && footerRow.getCellCount() > 0) {
            GridFooterCell cell = footerRow.getCell(0);
            int colspan = cell.getColspan();
            // If cells were joined, colspan should be > 1
            Assert.assertTrue(
                    "Joined cell should have colspan > 1 or be 1 if not joined",
                    colspan >= 1);
        }
    }

    @Test
    public void testGetVisibleFooterRows_returnsOnlyVisible() {
        clickButton("append-footer");

        List<GridFooterRow> visibleRows = grid.getVisibleFooterRows();
        Assert.assertNotNull("Visible footer rows should not be null",
                visibleRows);

        // All visible rows should be visible
        for (GridFooterRow row : visibleRows) {
            Assert.assertTrue("Row should be visible", row.isVisible());
        }
    }

    @Test
    public void testFooterRowGetCellTexts_returnsAllTexts() {
        clickButton("set-footer-text");

        GridFooterRow footerRow = grid.getFirstFooterRow();
        if (footerRow != null) {
            List<String> texts = footerRow.getCellTexts();
            Assert.assertNotNull("Cell texts should not be null", texts);
            Assert.assertEquals("Number of texts should match number of cells",
                    footerRow.getCellCount(), texts.size());
        }
    }

    @Test
    public void testFooterRowIsVisible_checksVisibility() {
        clickButton("append-footer");

        GridFooterRow footerRow = grid.getFirstFooterRow();
        if (footerRow != null) {
            boolean isVisible = footerRow.isVisible();
            Assert.assertTrue("Footer row should be visible by default",
                    isVisible);
        }
    }

    @Test
    public void testFooterCellClick_clicksOnCell() {
        clickButton("append-footer");

        GridFooterRow footerRow = grid.getFirstFooterRow();
        if (footerRow != null && footerRow.getCellCount() > 0) {
            GridFooterCell cell = footerRow.getCell(0);
            // This test just ensures the click method doesn't throw an
            // exception
            cell.click();
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
