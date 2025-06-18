/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/gridsetitemsafterdetachpage")
public class GridSetItemsAfterDetachIT extends AbstractComponentIT {

    @Test
    public void selectItem_detachGrid_setItemsAndAttachGrid_noClientSideErrors() {
        open();
        final GridElement grid = $(GridElement.class).waitForFirst();
        grid.select(0);

        $(TestBenchElement.class).id("detach").click();
        waitForDevServer();
        $(TestBenchElement.class).id("set-items-and-attach").click();
        waitForDevServer();

        checkLogsForErrors();
    }
}
