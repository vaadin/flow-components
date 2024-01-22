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
package com.vaadin.flow.component.treegrid.it;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/treegrid-scrolling")
public class TreeGridCollapseToLastRowInCacheIT extends AbstractComponentIT {

    @Test
    public void testCollapsingNode_removesLastRowFromGridCache_noInternalError() {
        open(TreeGridScrollingPage.NODES_PARAMETER + "=50");

        TreeGridElement grid = $(TreeGridElement.class).first();

        grid.expandWithClick(0);
        grid.expandWithClick(1);

        checkLogsForErrors();

        Assert.assertEquals("0 | 0", grid.getCell(0, 0).getText());
        Assert.assertEquals("1 | 0", grid.getCell(1, 0).getText());
        Assert.assertEquals("2 | 0", grid.getCell(2, 0).getText());

        grid.collapseWithClick(0);

        Assert.assertEquals("0 | 0", grid.getCell(0, 0).getText());
        Assert.assertEquals("0 | 1", grid.getCell(1, 0).getText());

        checkLogsForErrors();
    }
}
