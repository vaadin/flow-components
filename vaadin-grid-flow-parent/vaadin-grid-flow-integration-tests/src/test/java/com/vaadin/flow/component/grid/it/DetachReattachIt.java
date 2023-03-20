
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;

@TestPath("vaadin-grid/detach-reattach-page")
public class DetachReattachIt extends AbstractComponentIT {

    @Test
    public void detachAndReattach_setDeselectAllowedPreserved() {
        open();
        GridElement grid = $(GridElement.class).first();

        grid.getRow(1).select();
        Assert.assertTrue("Row is selected.", grid.getRow(1).isSelected());

        // Disable de-selection
        $("button").id("disallow-deselect-button").click();

        grid.getRow(1).deselect();
        Assert.assertTrue("Row is still selected as deselection is disallowed.",
                grid.getRow(1).isSelected());

        // Detach and re-attach
        $("button").id("detach-button").click();

        $("button").id("attach-button").click();

        Assert.assertTrue(
                "Selected row is preserved after detach and re-attach.",
                grid.getRow(1).isSelected());

        grid.getRow(1).deselect();
        Assert.assertTrue("Deselection is still disallowed after re-attach.",
                grid.getRow(1).isSelected());
    }

    @Test
    public void detachAndReattach_resetSorting_noErrorIsThrown() {
        open();
        GridElement grid = $(GridElement.class).first();

        grid.getHeaderCell(0).$("vaadin-grid-sorter").first().click();

        // Detach, reset sorting and re-attach
        $("button").id("detach-button").click();

        $("button").id("reset-sorting-button").click();

        $("button").id("attach-button").click();

        // Check that there are no new exceptions/errors thrown
        // after re-attaching the grid when sorting is reset
        checkLogsForErrors();
    }
}
