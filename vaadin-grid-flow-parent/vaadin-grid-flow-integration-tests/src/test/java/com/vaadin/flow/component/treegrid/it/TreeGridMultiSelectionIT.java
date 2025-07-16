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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/treegrid-multi-selection")
public class TreeGridMultiSelectionIT extends AbstractComponentIT {
    private TreeGridElement treeGrid;

    @Before
    public void before() {
        open();
        treeGrid = $(TreeGridElement.class).first();
    }

    @Test
    public void select_and_deselect_all() {
        assertAllRowsDeselected();

        treeGrid.clickSelectAll();
        assertAllRowsSelected();

        treeGrid.expandWithClick(1, 1);
        treeGrid.expandWithClick(2, 1);
        assertAllRowsSelected();

        treeGrid.clickSelectAll();
        assertAllRowsDeselected();

        treeGrid.clickSelectAll();
        treeGrid.collapseWithClick(2, 1);
        treeGrid.expandWithClick(2, 1);
        assertAllRowsSelected();

        treeGrid.collapseWithClick(2, 1);
        treeGrid.clickSelectAll();
        treeGrid.expandWithClick(2, 1);
        assertAllRowsDeselected();
    }

    private void assertAllRowsSelected() {
        for (int i = 0; i < treeGrid.getRowCount(); i++) {
            Assert.assertTrue(treeGrid.getRow(i).isSelected());
        }
    }

    private void assertAllRowsDeselected() {
        for (int i = 0; i < treeGrid.getRowCount(); i++) {
            Assert.assertFalse(treeGrid.getRow(i).isSelected());
        }
    }
}
