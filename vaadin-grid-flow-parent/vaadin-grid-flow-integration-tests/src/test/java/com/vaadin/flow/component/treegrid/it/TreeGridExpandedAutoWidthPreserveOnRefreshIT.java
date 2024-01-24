/*
 * Copyright 2000-2024 Vaadin Ltd.
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
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/treegrid-expanded-auto-width-preserve-on-refresh")
public class TreeGridExpandedAutoWidthPreserveOnRefreshIT
        extends AbstractTreeGridIT {

    @Before
    public void before() {
        open();
    }

    /**
     * Test for https://github.com/vaadin/web-components/pull/6684
     */
    @Test
    public void refresh_expectSameColumnWidth() {
        var grid = $(TreeGridElement.class).first();
        var columnOffsetWidth = grid.getCell(0, 0)
                .getPropertyInteger("offsetWidth");

        getDriver().navigate().refresh();

        grid = $(TreeGridElement.class).first();
        Assert.assertEquals(columnOffsetWidth,
                grid.getCell(0, 0).getPropertyInteger("offsetWidth"));
    }

}
