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

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/beangridpage")
public class BeanGridIT extends AbstractComponentIT {

    @Test
    public void gridNullValuesRenderedAsEmptyStrings() {
        open();
        GridElement grid = $(GridElement.class).first();
        String text = grid.getText();
        Assert.assertFalse("Null values should be presented as empty strings",
                text.contains("null"));
    }

    @Test
    public void rowCanBeDeselectedOnSingleSelectMode() {
        open();
        GridElement grid = $(GridElement.class).first();

        // Select first row.
        assertRowSelectionStatus(grid, 0, false);
        grid.select(0);
        assertRowSelectionStatus(grid, 0, true);

        // Try to deselect the wrong row
        grid.deselect(1);
        assertRowSelectionStatus(grid, 0, true);

        // Deselect the first row
        grid.deselect(0);
        assertRowSelectionStatus(grid, 0, false);
    }

    @Test
    public void setColumns_columnsAndCellsUpdated() {
        open();
        GridElement grid = $(GridElement.class).id("set-columns-grid");

        $("button").id("show-address-columns").click();
        Assert.assertEquals(3, grid.getAllColumns().size());
        Assert.assertEquals("Street", grid.getHeaderCell(0).getText());
        Assert.assertEquals("Number", grid.getHeaderCell(1).getText());
        Assert.assertEquals("Postal Code", grid.getHeaderCell(2).getText());
        Assert.assertEquals("Main Street", grid.getCell(0, 0).getText());
        Assert.assertEquals("5", grid.getCell(0, 1).getText());
        Assert.assertEquals("12345", grid.getCell(0, 2).getText());

        $("button").id("show-basic-columns").click();
        Assert.assertEquals(2, grid.getAllColumns().size());
        Assert.assertEquals("First Name", grid.getHeaderCell(0).getText());
        Assert.assertEquals("Age", grid.getHeaderCell(1).getText());
        Assert.assertEquals("Anna", grid.getCell(0, 0).getText());
        Assert.assertEquals("25", grid.getCell(0, 1).getText());
    }

    private void assertRowSelectionStatus(GridElement grid, int rowIndex,
            boolean expectedStatus) {
        Assert.assertEquals("Unexpected selection status for row " + rowIndex,
                expectedStatus, grid.getRow(rowIndex).isSelected());
    }
}
