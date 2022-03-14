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

import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid-it-demo/styling")
public class GridViewStylingIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void stylingDemo_classNamesGenerated() {
        GridElement grid = $(GridElement.class).id("class-name-generator");
        scrollToElement(grid);

        GridStylingIT.assertCellClassNames(grid, 0, 0, "subscriber");
        GridStylingIT.assertCellClassNames(grid, 0, 1, "subscriber");
        GridStylingIT.assertCellClassNames(grid, 0, 2, "subscriber");

        GridStylingIT.assertCellClassNames(grid, 5, 0, "");
        GridStylingIT.assertCellClassNames(grid, 5, 1, "minor");
        GridStylingIT.assertCellClassNames(grid, 5, 2, "");

        GridStylingIT.assertCellClassNames(grid, 9, 0, "subscriber");
        GridStylingIT.assertCellClassNames(grid, 9, 1, "subscriber minor");
        GridStylingIT.assertCellClassNames(grid, 9, 2, "subscriber");
    }
}
