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
package com.vaadin.flow.component.treegrid.it;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Tests for dynamically adding new columns with different renderers after the
 * Grid has already been attached and rendered.
 */
@TestPath("vaadin-grid/tree-component-columns")
public class TreeComponentColumnsIT extends AbstractComponentIT {

    private TreeGridElement gridThenComp;
    private TreeGridElement compThenGrid;

    @Before
    public void init() {
        open();
        clickElementWithJs("btn-add-comp-then-grid");
        clickElementWithJs("btn-add-grid-then-comp");

        gridThenComp = $(TreeGridElement.class).id("grid-then-comp");
        compThenGrid = $(TreeGridElement.class).id("comp-then-grid");
    }

    @Test
    public void compThenGridRendered_compButton() {
        assertCellContains(compThenGrid, 0, 0, "vaadin-text-field");
        assertCellContains(compThenGrid, 1, 0, "vaadin-text-field");
        assertCellContains(compThenGrid, 0, 1, "vaadin-text-field");
        assertCellContains(compThenGrid, 1, 1, "vaadin-text-field");
        assertCellContains(compThenGrid, 1, 2, "vaadin-button");
        assertCellContains(compThenGrid, 1, 2, "Granddad");
    }

    @Test
    public void gridThenCompRendered_compButton() {
        assertCellContains(gridThenComp, 0, 0, "vaadin-text-field");
        assertCellContains(gridThenComp, 1, 0, "vaadin-text-field");
        assertCellContains(gridThenComp, 0, 1, "vaadin-text-field");
        assertCellContains(gridThenComp, 1, 1, "vaadin-text-field");
        assertCellContains(gridThenComp, 1, 2, "vaadin-button");
        assertCellContains(gridThenComp, 1, 2, "Granddad");
    }

    @Test
    public void treegridComponentRenderer_expandCollapse_renderersShows() {
        compThenGrid.expandWithClick(1);
        compThenGrid.expandWithClick(0);
        compThenGrid.collapseWithClick(0);
        compThenGrid.select(1);

        assertCellContains(compThenGrid, 2, 0, "vaadin-text-field");
        assertCellContains(compThenGrid, 3, 0, "vaadin-text-field");
        assertCellContains(compThenGrid, 4, 0, "vaadin-text-field");
        assertCellContains(compThenGrid, 2, 1, "vaadin-text-field");
        assertCellContains(compThenGrid, 3, 1, "vaadin-text-field");
        assertCellContains(compThenGrid, 4, 1, "vaadin-text-field");
        assertCellContains(compThenGrid, 2, 2, "Dad 1/0");
        assertCellContains(compThenGrid, 3, 2, "Dad 1/1");
        assertCellContains(compThenGrid, 4, 2, "Dad 1/2");
    }

    @Test
    public void treegridComponentRenderer_expandCollapseExpand_componentsVisible() {
        compThenGrid.expandWithClick(0);
        compThenGrid.collapseWithClick(0);
        compThenGrid.expandWithClick(0);

        assertCellContains(compThenGrid, 4, 0, "vaadin-text-field");
        assertCellContains(compThenGrid, 4, 1, "vaadin-text-field");
        assertCellContains(compThenGrid, 4, 2, "Granddad 1");
    }

    @Test
    public void treegridComponentRenderer_expandScrollExpand_expectedRowHeights() {
        var rowHeight = compThenGrid.getRow(1).getSize().getHeight();
        compThenGrid.expandWithClick(0);
        compThenGrid.expandWithClick(1);
        compThenGrid.scrollToRow(104);

        var visibleRows = compThenGrid.getVisibleRows();
        Assert.assertFalse(visibleRows.isEmpty());

        for (int i = 1; i < visibleRows.size(); i++) {
            Assert.assertEquals(visibleRows.get(i - 1).getRect().y + rowHeight,
                    visibleRows.get(i).getRect().y, 1);
            Assert.assertEquals(rowHeight,
                    visibleRows.get(i).getSize().getHeight());
        }
    }

    private void assertCellContains(GridElement grid, int rowIndex,
            int colIndex, String expected) {
        Assert.assertThat(grid.getCell(rowIndex, colIndex).getInnerHTML(),
                CoreMatchers.containsString(expected));
    }

}
