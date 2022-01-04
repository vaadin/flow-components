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
