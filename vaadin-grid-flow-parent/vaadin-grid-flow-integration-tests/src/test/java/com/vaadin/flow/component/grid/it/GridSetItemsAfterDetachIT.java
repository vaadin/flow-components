
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Test;

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
