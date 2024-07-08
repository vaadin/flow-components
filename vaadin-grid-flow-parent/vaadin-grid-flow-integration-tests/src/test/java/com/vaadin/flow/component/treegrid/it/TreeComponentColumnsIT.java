/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

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
        System.out.println("Init method...");
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
        int rowHeight = compThenGrid.getRow(1).getSize().getHeight();
        compThenGrid.expandWithClick(0);
        compThenGrid.expandWithClick(1);
        compThenGrid.scrollToRowAndWait(104);

        List<GridTRElement> visibleRows = compThenGrid.getVisibleRows();
        Assert.assertFalse(visibleRows.isEmpty());

        visibleRows.forEach(row -> Assert.assertEquals(rowHeight,
                row.getSize().getHeight()));
    }

    private void assertCellContains(GridElement grid, int rowIndex,
            int colIndex, String expected) {
        Assert.assertThat(grid.getCell(rowIndex, colIndex).getInnerHTML(),
                CoreMatchers.containsString(expected));
    }

}
