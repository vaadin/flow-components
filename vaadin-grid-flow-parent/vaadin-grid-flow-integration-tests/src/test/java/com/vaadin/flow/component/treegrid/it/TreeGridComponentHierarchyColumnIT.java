/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/treegrid-component-hierarchy-column")
public class TreeGridComponentHierarchyColumnIT extends AbstractTreeGridIT {

    @Before
    public void before() {
        open();
        setupTreeGrid();
    }

    @Test
    public void treegridComponentRenderer_expandCollapseExpand_renderersShows() {

        assertAllRowsHasTextField(3);

        getTreeGrid().expandWithClick(0);
        assertAllRowsHasTextField(6);

        getTreeGrid().collapseWithClick(0);
        assertAllRowsHasTextField(3);

        getTreeGrid().expandWithClick(0);
        assertAllRowsHasTextField(6);
    }

    @Test
    public void treegridComponentRenderer_expandCollapse_rowLevelStateUpdated() {
        assertRowLevel(0, new int[] { 0, 0, 0 });

        getTreeGrid().expandWithClick(0);
        assertRowLevel(0, new int[] { 0, 1, 1, 1, 0, 0 });

        getTreeGrid().collapseWithClick(0);
        assertRowLevel(0, new int[] { 0, 0, 0 });
    }

    @Test
    public void treegridComponentRenderer_expandCollapse_rowExpandedStateUpdated() {
        assertRowExpanded(0, new boolean[] { false, false, false });

        getTreeGrid().expandWithClick(0);
        assertRowExpanded(0,
                new boolean[] { true, false, false, false, false, false });

        getTreeGrid().collapseWithClick(0);
        assertRowExpanded(0, new boolean[] { false, false, false });
    }

    private void assertAllRowsHasTextField(int expectedRowCount) {
        Assert.assertEquals(expectedRowCount, getTreeGrid().getRowCount());
        IntStream.range(0, getTreeGrid().getRowCount())
                .forEach(i -> Assert.assertNotNull(
                        "Row with index " + i + " has no component renderer",
                        getTreeGrid().getCell(i, 0).$("vaadin-text-field")));
    }
}
