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

import com.vaadin.flow.component.grid.testbench.GridColumnElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Test for scrolling to columns in Grid, especially important when
 * columnRendering is set to lazy.
 */
@TestPath("vaadin-grid/grid-column-scroll")
public class GridScrollToColumnIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        waitForDevServer();
        grid = $(GridElement.class).first();
    }

    @Test
    public void testScrollToColumn_bringsColumnIntoView() {
        // Assuming grid has many columns and some are out of view
        List<GridColumnElement> columns = grid.getAllColumns();

        if (columns.size() > 5) {
            // Get a column that might be out of view
            GridColumnElement lastColumn = columns.get(columns.size() - 1);

            // Scroll to the last column
            grid.scrollToColumn(lastColumn);

            // Verify the column is now in view
            Assert.assertTrue("Column should be in view after scrolling",
                    grid.isColumnInView(lastColumn));
        }
    }

    @Test
    public void testScrollToColumnByIndex_bringsColumnIntoView() {
        List<GridColumnElement> columns = grid.getVisibleColumns();

        if (columns.size() > 5) {
            int lastIndex = columns.size() - 1;

            // Scroll to the last column by index
            grid.scrollToColumn(lastIndex);

            // Verify we can get the cell without it being null
            GridTHTDElement cell = grid.getCell(0, lastIndex);
            Assert.assertNotNull(
                    "Cell should not be null after scrolling to column", cell);
        }
    }

    @Test
    public void testIsColumnInView_detectsVisibleColumns() {
        List<GridColumnElement> columns = grid.getVisibleColumns();

        if (!columns.isEmpty()) {
            // First column should typically be in view
            GridColumnElement firstColumn = columns.get(0);
            Assert.assertTrue("First column should be in view",
                    grid.isColumnInView(firstColumn));
        }
    }

    @Test
    public void testGetCell_automaticallyScrollsColumnIntoView() {
        List<GridColumnElement> columns = grid.getAllColumns();

        if (columns.size() > 10) {
            // Try to get a cell from a column that's likely out of view
            GridColumnElement farColumn = columns.get(columns.size() - 1);

            // This should automatically scroll the column into view
            GridTHTDElement cell = grid.getCell(0, farColumn);

            Assert.assertNotNull(
                    "Cell should not be null after automatic scrolling", cell);
            Assert.assertTrue("Column should be in view after getCell",
                    grid.isColumnInView(farColumn));
        }
    }

    @Test
    public void testScrollToColumn_withLazyColumnRendering() {
        // This test specifically addresses the issue mentioned in #8046
        // When columnRendering is lazy, cells out of view return null

        List<GridColumnElement> columns = grid.getAllColumns();
        if (columns.size() > 5) {
            // First, try to get cells from the first row
            for (int i = 0; i < columns.size(); i++) {
                GridColumnElement column = columns.get(i);

                // Ensure column is scrolled into view
                if (!grid.isColumnInView(column)) {
                    grid.scrollToColumn(column);
                }

                // Now the cell should be accessible
                GridTHTDElement cell = grid.getRow(0).getCell(column);
                Assert.assertNotNull("Cell at column " + i
                        + " should not be null after scrolling", cell);
            }
        }
    }

    @Test
    public void testScrollToColumn_multipleScrolls() {
        List<GridColumnElement> columns = grid.getVisibleColumns();

        if (columns.size() > 10) {
            // Scroll to last column
            GridColumnElement lastColumn = columns.get(columns.size() - 1);
            grid.scrollToColumn(lastColumn);
            Assert.assertTrue("Last column should be in view",
                    grid.isColumnInView(lastColumn));

            // Scroll back to first column
            GridColumnElement firstColumn = columns.get(0);
            grid.scrollToColumn(firstColumn);
            Assert.assertTrue("First column should be in view",
                    grid.isColumnInView(firstColumn));

            // Scroll to middle column
            GridColumnElement middleColumn = columns.get(columns.size() / 2);
            grid.scrollToColumn(middleColumn);
            Assert.assertTrue("Middle column should be in view",
                    grid.isColumnInView(middleColumn));
        }
    }
}
