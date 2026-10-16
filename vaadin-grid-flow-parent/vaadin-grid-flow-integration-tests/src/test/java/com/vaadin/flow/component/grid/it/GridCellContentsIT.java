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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/cell-contents")
public class GridCellContentsIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void getVisibleCellContents_smallGrid_returnsVisibleCells() {
        GridElement grid = $(GridElement.class).id("small-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        List<List<String>> cells = grid.getVisibleCellContents();

        Assert.assertNotNull("Cells should not be null", cells);
        Assert.assertTrue("Grid should have visible cells", cells.size() > 0);

        // Check first row
        List<String> firstRow = cells.get(0);
        Assert.assertEquals("First row should have 2 columns", 2,
                firstRow.size());
        Assert.assertEquals("Person 0", firstRow.get(0));
        Assert.assertEquals("0", firstRow.get(1));
    }

    @Test
    public void getAllCellContents_smallGrid_returnsAllCells() {
        GridElement grid = $(GridElement.class).id("small-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        List<List<String>> cells = grid.getAllCellContents();

        Assert.assertEquals("Should have 10 rows", 10, cells.size());

        // Verify first and last rows
        Assert.assertEquals("Person 0", cells.get(0).get(0));
        Assert.assertEquals("0", cells.get(0).get(1));
        Assert.assertEquals("Person 9", cells.get(9).get(0));
        Assert.assertEquals("9", cells.get(9).get(1));
    }

    @Test
    public void getAllCellContents_largeGrid_returnsAllCells() {
        GridElement grid = $(GridElement.class).id("large-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        List<List<String>> cells = grid.getAllCellContents();

        Assert.assertEquals("Should have 200 rows", 200, cells.size());

        // Verify sampling of rows
        Assert.assertEquals("Person 0", cells.get(0).get(0));
        Assert.assertEquals("Person 100", cells.get(100).get(0));
        Assert.assertEquals("Person 199", cells.get(199).get(0));
    }

    @Test
    public void getCellContents_largeGrid_returnsSpecifiedRange() {
        GridElement grid = $(GridElement.class).id("large-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        List<List<String>> cells = grid.getCellContents(150, 180);

        Assert.assertEquals("Should have 31 rows", 31, cells.size());
        Assert.assertEquals("Person 150", cells.get(0).get(0));
        Assert.assertEquals("Person 180", cells.get(30).get(0));
    }

    @Test
    public void getAllCellContents_hiddenColumn_onlyVisibleColumns() {
        GridElement grid = $(GridElement.class).id("hidden-column-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        List<List<String>> cells = grid.getAllCellContents();

        Assert.assertEquals("Should have 10 rows", 10, cells.size());

        // Should only have 2 visible columns (Name and Email, Age is hidden)
        List<String> firstRow = cells.get(0);
        Assert.assertEquals("Should have 2 visible columns", 2,
                firstRow.size());
        Assert.assertEquals("Person 0", firstRow.get(0));
        Assert.assertEquals("Email0", firstRow.get(1));
    }

    @Test
    public void getCellContents_invalidRange_throwsException() {
        GridElement grid = $(GridElement.class).id("small-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        try {
            grid.getCellContents(-1, 5);
            Assert.fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            grid.getCellContents(0, 100);
            Assert.fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            grid.getCellContents(5, 3);
            Assert.fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    @Test
    public void getAllCellContents_emptyGrid_returnsEmptyList() {
        GridElement grid = $(GridElement.class).id("empty-grid");
        scrollToElement(grid);

        List<List<String>> cells = grid.getAllCellContents();

        Assert.assertTrue("Empty grid should return no rows", cells.isEmpty());
    }

    @Test
    public void getCellContents_emptyGrid_throwsWithClearMessage() {
        GridElement grid = $(GridElement.class).id("empty-grid");
        scrollToElement(grid);

        try {
            grid.getCellContents(0, 0);
            Assert.fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            Assert.assertTrue("Message should mention the empty grid, but was: "
                    + e.getMessage(), e.getMessage().contains("empty"));
        }
    }

    @Test
    public void getCellContents_matchesGetCellText() {
        GridElement grid = $(GridElement.class).id("hidden-column-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        List<List<String>> cells = grid.getAllCellContents();

        int visibleColumns = grid.getVisibleColumns().size();
        for (int row = 0; row < cells.size(); row++) {
            for (int col = 0; col < visibleColumns; col++) {
                Assert.assertEquals(
                        "Cell content should match getCell().getText() at row "
                                + row + ", column " + col,
                        grid.getCell(row, col).getText(),
                        cells.get(row).get(col));
            }
        }
    }

    @Test
    public void getVisibleCellContents_scrolled_returnsVisibleRowsInOrder() {
        GridElement grid = $(GridElement.class).id("large-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        grid.scrollToRow(100);

        List<List<String>> cells = grid.getVisibleCellContents();

        Assert.assertTrue("Grid should show more than one visible row",
                cells.size() > 1);

        // Only the rows in the viewport should be returned, in ascending index
        // order, excluding the buffer rows the grid renders out of view. The
        // grid also recycles row elements, so DOM order does not match index
        // order after scrolling.
        int firstVisible = grid.getFirstVisibleRowIndex();
        int lastVisible = grid.getLastVisibleRowIndex();
        Assert.assertEquals("Should return exactly the visible rows",
                lastVisible - firstVisible + 1, cells.size());

        int expectedIndex = firstVisible;
        for (List<String> row : cells) {
            Assert.assertEquals("Person " + expectedIndex, row.get(0));
            expectedIndex++;
        }
    }

    @Test
    public void getVisibleCellContents_lazyColumns_returnsAllColumns() {
        GridElement grid = $(GridElement.class).id("lazy-columns-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);
        assertLastColumnNotRendered(grid);

        List<List<String>> cells = grid.getVisibleCellContents();

        Assert.assertTrue("Grid should show more than one visible row",
                cells.size() > 1);
        for (int row = 0; row < cells.size(); row++) {
            assertLazyColumnsRow(row, cells.get(row));
        }
        assertLastColumnNotRendered(grid);
    }

    @Test
    public void getAllCellContents_lazyColumns_returnsAllColumns() {
        GridElement grid = $(GridElement.class).id("lazy-columns-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);
        assertLastColumnNotRendered(grid);

        List<List<String>> cells = grid.getAllCellContents();

        Assert.assertEquals("Should have 100 rows", 100, cells.size());
        for (int row = 0; row < cells.size(); row++) {
            assertLazyColumnsRow(row, cells.get(row));
        }
        assertLastColumnNotRendered(grid);
    }

    private void assertLazyColumnsRow(int row, List<String> cells) {
        Assert.assertEquals("Row " + row + " should have all 20 columns", 20,
                cells.size());
        for (int col = 0; col < cells.size(); col++) {
            Assert.assertEquals("R" + row + "C" + col, cells.get(col));
        }
    }

    // Checks that lazy column rendering is active. Used after reading cells
    // to check that the mode is restored, and before to check the setup.
    private void assertLastColumnNotRendered(GridElement grid) {
        Assert.assertEquals("lazy", grid.getProperty("columnRendering"));
        Assert.assertNull("Last column should not be rendered",
                grid.getCell(0, 19));
    }
}
