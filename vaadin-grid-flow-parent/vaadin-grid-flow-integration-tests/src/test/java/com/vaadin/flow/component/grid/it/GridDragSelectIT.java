/*
 * Copyright 2000-2024 Vaadin Ltd.
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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/grid-drag-select")
public class GridDragSelectIT extends AbstractComponentIT {
    private GridElement grid;
    private CheckboxElement toggleDragSelect;
    private WebElement selectedItemsCount;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).waitForFirst();
        toggleDragSelect = $(CheckboxElement.class).id("toggle-drag-select");
        selectedItemsCount = $("span").id("selected-items-count");
    }

    @Test
    public void selectRowsByDragging_enabled_selectDeselectRows() {
        toggleDragSelect.click();

        int firstRow = 0;
        int lastRow = 6;
        GridTHTDElement sourceCell = grid.getCell(firstRow, 0);
        GridTHTDElement targetCell = grid.getCell(lastRow, 0);

        selectRowsByDragging(sourceCell, targetCell);
        assertRowsSelected(grid, firstRow, lastRow);
        Assert.assertEquals("Selected items: 7", selectedItemsCount.getText());

        selectRowsByDragging(sourceCell, targetCell);
        assertRowsUnselected(grid, lastRow, firstRow);
        Assert.assertEquals("Selected items: 0", selectedItemsCount.getText());
    }

    @Test
    public void selectRowsByDragging_disabled_rowsUnselected() {
        int firstRow = 0;
        int lastRow = 6;
        GridTHTDElement sourceCell = grid.getCell(firstRow, 0);
        GridTHTDElement targetCell = grid.getCell(lastRow, 0);

        selectRowsByDragging(sourceCell, targetCell);
        assertRowsUnselected(grid, lastRow, firstRow);
        Assert.assertEquals("Selected items: 0", selectedItemsCount.getText());
    }

    private void selectRowsByDragging(WebElement sourceCell,
            WebElement targetCell) {
        new Actions(getDriver()).dragAndDrop(sourceCell, targetCell).release()
                .build().perform();
    }

    private void assertRowsSelected(GridElement grid, int first, int last) {
        grid.getRows(first, last)
                .forEach(row -> Assert.assertTrue(row.isSelected()));
    }

    private void assertRowsUnselected(GridElement grid, int first, int last) {
        grid.getRows(first, last)
                .forEach(row -> Assert.assertFalse(row.isSelected()));
    }
}
