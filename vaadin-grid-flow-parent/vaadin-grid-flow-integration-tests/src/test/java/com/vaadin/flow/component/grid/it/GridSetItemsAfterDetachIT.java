/*
 * Copyright 2000-2020 Vaadin Ltd.
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
