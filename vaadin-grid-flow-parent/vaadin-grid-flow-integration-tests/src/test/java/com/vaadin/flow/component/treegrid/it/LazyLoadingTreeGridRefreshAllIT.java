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

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.component.textfield.testbench.IntegerFieldElement;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.stream.IntStream;

@TestPath("vaadin-grid/lazy-loading-treegrid-refreshall")
public class LazyLoadingTreeGridRefreshAllIT extends AbstractTreeGridIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void setChildCountAndRefreshAll_resultIsNotEmpty() {
        TreeGridElement treeGrid = $(TreeGridElement.class).get(0);

        // The row index should be larger than 100 in order to replicate the
        // issue.
        treeGrid.scrollToRow(120);

        // The count should be smaller than 100 in order to replicate the issue.
        IntegerFieldElement childCount = $(IntegerFieldElement.class)
                .id("child-count");
        childCount.setValue("5");

        ButtonElement refreshAll = $(ButtonElement.class).id("refresh-all");
        refreshAll.click();

        Assert.assertTrue(treeGrid.getRowCount() > 0);
    }
}
