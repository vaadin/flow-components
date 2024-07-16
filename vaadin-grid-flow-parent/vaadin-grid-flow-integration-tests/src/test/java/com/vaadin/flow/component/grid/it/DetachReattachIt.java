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
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Test;

@TestPath("vaadin-grid/detach-reattach-page")
public class DetachReattachIt extends AbstractComponentIT {

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
