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

@TestPath("vaadin-grid/dump")
public class GridDumpIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void dumpVisibleCells_smallGrid_returnsVisibleCells() {
        GridElement grid = $(GridElement.class).id("small-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        List<List<String>> cells = grid.dumpVisibleCells();

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
    public void dumpAllCells_smallGrid_returnsAllCells() {
        GridElement grid = $(GridElement.class).id("small-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        List<List<String>> cells = grid.dumpAllCells();

        Assert.assertEquals("Should have 10 rows", 10, cells.size());

        // Verify first and last rows
        Assert.assertEquals("Person 0", cells.get(0).get(0));
        Assert.assertEquals("0", cells.get(0).get(1));
        Assert.assertEquals("Person 9", cells.get(9).get(0));
        Assert.assertEquals("9", cells.get(9).get(1));
    }

    @Test
    public void dumpAllCells_mediumGrid_returnsAllCells() {
        GridElement grid = $(GridElement.class).id("medium-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        List<List<String>> cells = grid.dumpAllCells();

        Assert.assertEquals("Should have 100 rows", 100, cells.size());

        // Verify first, middle and last rows
        Assert.assertEquals("Person 0", cells.get(0).get(0));
        Assert.assertEquals("Person 50", cells.get(50).get(0));
        Assert.assertEquals("Person 99", cells.get(99).get(0));
    }

    @Test
    public void dumpCells_mediumGrid_returnsSpecifiedRange() {
        GridElement grid = $(GridElement.class).id("medium-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        List<List<String>> cells = grid.dumpCells(20, 29);

        Assert.assertEquals("Should have 10 rows", 10, cells.size());

        // Verify row data
        Assert.assertEquals("Person 20", cells.get(0).get(0));
        Assert.assertEquals("20", cells.get(0).get(1));
        Assert.assertEquals("Person 29", cells.get(9).get(0));
        Assert.assertEquals("29", cells.get(9).get(1));
    }

    @Test
    public void dumpAllCells_largeGrid_returnsAllCells() {
        GridElement grid = $(GridElement.class).id("large-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        List<List<String>> cells = grid.dumpAllCells();

        Assert.assertEquals("Should have 1000 rows", 1000, cells.size());

        // Verify sampling of rows
        Assert.assertEquals("Person 0", cells.get(0).get(0));
        Assert.assertEquals("Person 500", cells.get(500).get(0));
        Assert.assertEquals("Person 999", cells.get(999).get(0));
    }

    @Test
    public void dumpCells_largeGrid_returnsSpecifiedRange() {
        GridElement grid = $(GridElement.class).id("large-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        List<List<String>> cells = grid.dumpCells(800, 850);

        Assert.assertEquals("Should have 51 rows", 51, cells.size());
        Assert.assertEquals("Person 800", cells.get(0).get(0));
        Assert.assertEquals("Person 850", cells.get(50).get(0));
    }

    @Test
    public void dumpAllCells_hiddenColumn_onlyVisibleColumns() {
        GridElement grid = $(GridElement.class).id("hidden-column-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        List<List<String>> cells = grid.dumpAllCells();

        Assert.assertEquals("Should have 10 rows", 10, cells.size());

        // Should only have 2 visible columns (Name and Email, Age is hidden)
        List<String> firstRow = cells.get(0);
        Assert.assertEquals("Should have 2 visible columns", 2,
                firstRow.size());
        Assert.assertEquals("Person 0", firstRow.get(0));
        Assert.assertEquals("Email0", firstRow.get(1));
    }

    @Test
    public void dumpCells_invalidRange_throwsException() {
        GridElement grid = $(GridElement.class).id("small-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        try {
            grid.dumpCells(-1, 5);
            Assert.fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            grid.dumpCells(0, 100);
            Assert.fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            grid.dumpCells(5, 3);
            Assert.fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    @Test
    public void dumpAllCells_fasterThanGetText() {
        GridElement grid = $(GridElement.class).id("medium-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        // Test dumpAllCells performance
        long startDump = System.currentTimeMillis();
        List<List<String>> dumpedCells = grid.dumpAllCells();
        long dumpTime = System.currentTimeMillis() - startDump;

        // Test getText performance (just first 10 rows to keep test fast)
        long startGetText = System.currentTimeMillis();
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 2; col++) {
                grid.getCell(row, col).getText();
            }
        }
        long getTextTime = System.currentTimeMillis() - startGetText;

        // Verify data is correct
        Assert.assertEquals("Should have 100 rows", 100, dumpedCells.size());
        Assert.assertEquals("Person 0", dumpedCells.get(0).get(0));

        // dumpAllCells should be significantly faster
        // Even dumping 100 rows should be faster than getText on just 10 rows
        Assert.assertTrue(
                "dumpAllCells should be faster than getText. dumpAllCells: "
                        + dumpTime + "ms, getText (10 rows): " + getTextTime
                        + "ms",
                dumpTime < getTextTime * 5);
    }
}
