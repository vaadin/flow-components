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
package com.vaadin.flow.component.gridpro.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.gridpro.testbench.GridProElement;
import com.vaadin.flow.component.gridpro.testbench.GridTHTDElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("gridpro-detach-attach")
public class GridProDetachAttachIT extends AbstractComponentIT {

    private GridProElement grid;
    private TestBenchElement toggleAttachedButton;
    private TestBenchElement addColumnButton;

    @Before
    public void before() {
        open();
        grid = $(GridProElement.class).first();
        toggleAttachedButton = $("button").id("toggle-attached");
        addColumnButton = $("button").id("add-column");
    }

    @Test
    public void detach_attach_customEditOpens() {
        toggleAttachedButton.click();
        toggleAttachedButton.click();

        grid = $(GridProElement.class).waitForFirst();
        assertCellEnterEditModeOnDoubleClick(grid, 0, 0, "vaadin-text-field");
    }

    @Test
    public void addColumn_customEditOpens() {
        addColumnButton.click();

        grid = $(GridProElement.class).waitForFirst();
        assertCellEnterEditModeOnDoubleClick(grid, 0, 1, "vaadin-text-field");
    }

    private void assertCellEnterEditModeOnDoubleClick(GridProElement grid,
            Integer rowIndex, Integer colIndex, String editorTag) {
        GridTHTDElement cell = grid.getCell(rowIndex, colIndex);

        // Not in edit mode initially
        Assert.assertFalse(cell.innerHTMLContains(editorTag));

        // Entering edit mode with double click
        executeScript(
                "arguments[0].dispatchEvent(new CustomEvent('dblclick', {composed: true, bubbles: true}));",
                cell);
        Assert.assertTrue(cell.innerHTMLContains(editorTag));
        // Assert there are no null reference errors
        checkLogsForErrors();
    }
}
