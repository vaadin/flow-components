/*
 * Copyright 2000-2022 Vaadin Ltd.
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
