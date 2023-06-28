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

import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the GridSelectRowsByDragging view.
 */
@TestPath("vaadin-grid/grid-drag-select")
public class GridDragSelectIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).id(
                GridDragSelectPage.DRAG_SELECT_GRID_ID);
    }

    private WebElement selectionColumn(WebElement grid) {
        return grid
                .findElement(By.tagName("vaadin-grid-flow-selection-column"));
    }

    @Test
    public void toggle_enabled_selectRowsByDragging() {
        WebElement gridSelectionMode = selectionColumn(grid);
        String selectRowsByDragging = gridSelectionMode
                .getAttribute("dragSelect");
        Assert.assertFalse("dragSelect should be false",
                Boolean.parseBoolean(selectRowsByDragging));

        enableSelectRowsByDragging();

        selectRowsByDragging = gridSelectionMode
                .getAttribute("dragSelect");

        Assert.assertTrue("dragSelect should be true",
                Boolean.parseBoolean(selectRowsByDragging));
    }

    @Test
    public void selectRowsByDragging_enabled_selectDeselectRows() {
        enableSelectRowsByDragging();

        int firstRow = 0;
        int lastRow = 6;
        GridTHTDElement sourceCell = grid.getCell(firstRow, 0);
        GridTHTDElement targetCell = grid.getCell(lastRow, 0);

        selectRowsByDragging(sourceCell, targetCell);
        assertRowsSelected(grid, firstRow, lastRow);

        selectRowsByDragging(sourceCell, targetCell);
        assertRowsUnselected(grid, lastRow, firstRow);
    }

    @Test
    public void selectRowsByDragging_disabled_rowsUnselected() {
        int firstRow = 0;
        int lastRow = 6;
        GridTHTDElement sourceCell = grid.getCell(firstRow, 0);
        GridTHTDElement targetCell = grid.getCell(lastRow, 0);

        selectRowsByDragging(sourceCell, targetCell);
        assertRowsUnselected(grid, lastRow, firstRow);
    }

    private void selectRowsByDragging(WebElement sourceCell, WebElement targetCell) {
        new Actions(getDriver())
                .dragAndDrop(sourceCell, targetCell)
                .release()
                .build().perform();
    }

    private void enableSelectRowsByDragging() {
        CheckboxElement checkbox = $(CheckboxElement.class).id(
                GridDragSelectPage.TOGGLE_DRAG_SELECT_CHECKBOX);
        checkbox.click();
    }

    private void assertRowsSelected(GridElement grid, int first, int last) {
        IntStream.range(first, last).forEach(rowIndex -> Assert
                .assertTrue(grid.getRow(rowIndex).isSelected()));
    }

    private void assertRowsUnselected(GridElement grid, int first, int last) {
        IntStream.range(first, last).forEach(rowIndex -> Assert
                .assertFalse(grid.getRow(rowIndex).isSelected()));
    }

}
