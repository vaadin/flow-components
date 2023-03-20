

package com.vaadin.flow.component.grid.it;

import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

import static com.vaadin.flow.component.grid.it.GridClearAfterDetachAndReattachPage.CLEAR_BUTTON_ID;
import static com.vaadin.flow.component.grid.it.GridClearAfterDetachAndReattachPage.GRID_ID;
import static com.vaadin.flow.component.grid.it.GridClearAfterDetachAndReattachPage.GRID_ROW_COUNT;

@TestPath("vaadin-grid/clean-grid-items-after-detach-and-reattach")
public class GridClearAfterDetachAndReattachIT extends AbstractComponentIT {

    @Test // https://github.com/vaadin/vaadin-grid/issues/1837
    public void clearGridItemsAfterDetachAndReattach_gridItemsClearedWithNoErrors() {
        open();

        GridElement gridElement = $(GridElement.class).id(GRID_ID);
        waitUntil((driver) -> gridElement.getLastVisibleRowIndex() > 0);
        // Scroll to the end
        gridElement.scrollToRow(gridElement.getRowCount());
        waitUntil((driver) -> gridElement
                .getLastVisibleRowIndex() == GRID_ROW_COUNT - 1);

        // Check that there are no exceptions/errors in the logs
        checkLogsForErrors();

        $("button").id(CLEAR_BUTTON_ID).click();
        waitUntil((driver) -> gridElement.getRowCount() == 0);

        // Check that there are no new exceptions/errors throws after
        // cleaning the grid
        checkLogsForErrors();
    }
}
