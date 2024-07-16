/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for dynamically adding new columns with different renderers after the
 * Grid has already been attached and rendered.
 */
@TestPath("vaadin-grid/component-columns")
public class ComponentColumnsIT extends AbstractComponentIT {

    private GridElement gridThenComp;
    private GridElement compThenGrid;

    @Before
    public void init() {
        open();
        clickElementWithJs("btn-add-comp-then-grid");
        clickElementWithJs("btn-add-grid-then-comp");

        gridThenComp = $(GridElement.class).id("grid-then-comp");
        compThenGrid = $(GridElement.class).id("comp-then-grid");
    }

    @Test
    public void gridThenCompRendered_nativeButton() {
        assertCellContents(gridThenComp, 0, 0, "<button>click</button>");
        assertCellContents(gridThenComp, 1, 0, "<button>click</button>");
    }

    @Test
    public void compThenGridRendered_nativeButton() {
        assertCellContents(compThenGrid, 0, 0, "<button>click</button>");
        assertCellContents(compThenGrid, 1, 0, "<button>click</button>");
    }

    @Test
    public void compThenGridRendered_compButton() {
        // <flow-component-renderer appid="ROOT" style=""><vaadin-button
        // tabindex="0" role="button"
        // focus-target="true">foo</vaadin-button></flow-component-renderer>
        assertCellContains(compThenGrid, 0, 1, "foo");
        assertCellContains(compThenGrid, 0, 1, "vaadin-button");
        assertCellContains(compThenGrid, 1, 1, "bar");
        assertCellContains(compThenGrid, 1, 1, "vaadin-button");
    }

    @Test
    public void gridThenCompRendered_compButton() {
        // <flow-component-renderer appid="ROOT" style=""><vaadin-button
        // tabindex="0" role="button"
        // focus-target="true">foo</vaadin-button></flow-component-renderer>
        assertCellContains(gridThenComp, 0, 1, "foo");
        assertCellContains(gridThenComp, 0, 1, "vaadin-button");
        assertCellContains(gridThenComp, 1, 1, "bar");
        assertCellContains(gridThenComp, 1, 1, "vaadin-button");
    }

    private void assertCellContents(GridElement grid, int rowIndex,
            int colIndex, String expected) {
        Assert.assertEquals(expected,
                grid.getCell(rowIndex, colIndex).getInnerHTML());
    }

    private void assertCellContains(GridElement grid, int rowIndex,
            int colIndex, String expected) {
        Assert.assertThat(grid.getCell(rowIndex, colIndex).getInnerHTML(),
                CoreMatchers.containsString(expected));
    }

}
