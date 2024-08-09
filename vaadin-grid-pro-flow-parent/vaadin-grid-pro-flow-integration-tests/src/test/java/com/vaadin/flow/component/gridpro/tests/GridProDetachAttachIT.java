/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.gridpro.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.gridpro.testbench.GridProElement;
import com.vaadin.flow.component.gridpro.testbench.GridTHTDElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

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

    @Test
    public void attachAndDetach_noClientErrors() {
        toggleAttachedButton.click();
        $("button").id("attach-and-detach").click();
        checkLogsForErrors();
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
