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
