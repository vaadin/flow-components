/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.testbench.GridColumnElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Tests for ensuring utility methods to fetch rows, and cells from specific
 * rows and columns work as expected.
 */
@TestPath("vaadin-grid-it-demo/all-rows-visible")
public class GridViewAllRowsVisibleIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).id("grid-all-rows-visible");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() == 50);
    }

    @Test
    public void allRowsVisible_allRowsAreFetched() {
        Assert.assertEquals("Grid should have allRowsVisible set to true",
                "true", grid.getAttribute("allRowsVisible"));
    }

    @Test
    public void cellsRowIndexUtilMethod_returnsExpectedCells() {
        for (GridTHTDElement cell : grid.getCells(0)) {
            Assert.assertEquals(grid.getCell(cell.getRow(), cell.getColumn()),
                    cell);
        }
    }

    @Test
    public void cellsColumnElementsUtilMethod_returnsExpectedCells() {
        for (GridColumnElement col : grid.getAllColumns()) {
            for (GridTHTDElement cell : grid.getCells(0, col)) {
                Assert.assertEquals(
                        grid.getCell(cell.getRow(), cell.getColumn()), cell);
            }
        }
    }

    @Test
    public void indexesBasedRowsFetchingMethod_returnsExpectedRows() {
        int expectedRowAmount = grid.getRowCount();
        int actualRowAmount = grid.getRows(0, expectedRowAmount - 1).size();
        Assert.assertEquals(expectedRowAmount, actualRowAmount);
    }

    @Test
    public void indexesBasedRowsFetchingMethod_returnsSameElementAsSingularRowFetchingMethod() {
        List<GridTRElement> rows = grid.getRows(0, grid.getRowCount() - 1);
        for (int i = 0; i < rows.size(); i++) {
            Assert.assertEquals(grid.getRow(i), rows.get(i));
        }
    }

}
