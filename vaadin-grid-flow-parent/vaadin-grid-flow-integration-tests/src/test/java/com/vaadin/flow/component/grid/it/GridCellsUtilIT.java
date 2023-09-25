/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import com.vaadin.flow.component.grid.testbench.GridColumnElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Tests for ensuring utility methods to fetch cells from specific rows and columns work as expected.
 */
@TestPath("vaadin-grid-it-demo/all-rows-visible")
public class GridCellsUtilIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).first();
    }

    @Test
    public void cellsRowIndexUtilMethod_returnsExpectedCells() {
        List<GridColumnElement> columns = grid.getAllColumns();
        for (int i = 0; i < columns.size(); i++) {
            for (GridTHTDElement cell : grid.getCells(i)) {
                Assert.assertEquals(grid.getCell(cell.getRow(), cell.getColumn()), cell);
            }
        }
    }

    @Test
    public void cellsColumnElementsUtilMethod_returnsExpectedCells() {
        List<GridColumnElement> columns = grid.getAllColumns();
        for (int i = 0; i < columns.size(); i++) {
            for (GridTHTDElement cell : grid.getCells(i, columns.get(i))) {
                Assert.assertEquals(grid.getCell(cell.getRow(), cell.getColumn()), cell);
            }
        }
    }

}
