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
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.data.performance.TreeGridMemory;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-grid/treegrid-huge-tree")
public class TreeGridHugeTreeIT extends AbstractTreeGridIT {

    @Test
    public void toggle_expand_when_row_out_of_cache() {
        open();
        setupTreeGrid();

        TreeGridElement grid = getTreeGrid();
        TestBenchElement expandSecondRowButton = $("button")
                .id("expand-second-row");
        TestBenchElement collapseSecondRowButton = $("button")
                .id("collapse-second-row");

        grid.expandWithClick(2);
        grid.expandWithClick(3);
        grid.scrollToRowByPath(300);

        expandSecondRowButton.click();

        grid.scrollToRowByPath(0);
        assertCellTexts(0, 0, new String[] { "Granddad 0", "Granddad 1",
                "Dad 1/0", "Dad 1/1", "Dad 1/2", "Granddad 2", "Dad 2/0" });

        grid.scrollToRowByPath(300);
        collapseSecondRowButton.click();
        grid.scrollToRowByPath(0);
        assertCellTexts(0, 0, new String[] { "Granddad 0", "Granddad 1",
                "Granddad 2", "Dad 2/0" });

        grid.scrollToRowByPath(300);
        expandSecondRowButton.click();
        collapseSecondRowButton.click();
        grid.scrollToRowByPath(0);
        assertCellTexts(0, 0, new String[] { "Granddad 0", "Granddad 1",
                "Granddad 2", "Dad 2/0" });
    }

    @Test
    public void collapsed_rows_invalidated_correctly() {
        open();
        setupTreeGrid();

        TreeGridElement grid = getTreeGrid();

        grid.expandWithClick(2);
        grid.expandWithClick(3);
        grid.expandWithClick(0);
        grid.collapseWithClick(0);
        grid.expandWithClick(0);
        grid.expandWithClick(1);
        assertCellTexts(0, 0,
                new String[] { "Granddad 0", "Dad 0/0", "Son 0/0/0" });
    }

    @Test
    public void root_keys_dropped_from_keymapper_properly() {
        open();
        setupTreeGrid();

        TreeGridElement grid = getTreeGrid();

        TestBenchElement checkFirstRootItemKey = $("button")
                .id("check-first-root-item-key");
        TestBenchElement initHugeDataSet = $("button").id("init-huge-data-set");

        initHugeDataSet.click();
        checkFirstRootItemKey.click();
        Assert.assertEquals("First root key was not in KeyMapper as expected",
                "true", checkFirstRootItemKey.getText());

        // Scroll first root item way out of viewport and check that the key was
        // dropped
        grid.scrollToRowByPath(200);
        checkFirstRootItemKey.click();
        Assert.assertEquals(
                "First root key was in KeyMapper when it should not be",
                "false", checkFirstRootItemKey.getText());
    }

    @Test
    public void collapsed_subtrees_outside_of_cache_stay_expanded() {

        if (Double.parseDouble(
                System.getProperty("java.specification.version")) >= 16) {
            System.err.println(
                    "\n-----------\n\n  Detected JDK16+ ignoring 'TreeGridHugeTreeIT.collapsed_subtrees_outside_of_cache_stay_expanded'\n  See https://github.com/vaadin/flow-components/issues/1835\n\n-----------\n");
            return;
        }

        getDriver().get(getRootURL() + "/vaadin-grid/" + TreeGridMemory.PATH
                + "/items=200&initiallyExpanded");
        setupTreeGrid();

        TreeGridElement grid = getTreeGrid();

        // assuming cache size to be visible row count + buffer before/after
        // assuming buffer to match visible row count
        int assumedCachedSize = (grid.getLastVisibleRowIndex()
                - grid.getFirstVisibleRowIndex()) * 3;
        String[] cellTexts = new String[assumedCachedSize];
        for (int i = 0; i < assumedCachedSize; i++) {
            cellTexts[i] = grid.getCellWaitForRow(i, 0).getText();
        }
        grid.scrollToRowByPath(0);
        grid.collapseWithClick(1);
        grid.expandWithClick(1);

        assertCellTexts(0, 0, cellTexts);
    }

    @Test
    public void expanded_nodes_populate_after_scroll() {
        open();
        setupTreeGrid();

        TreeGridElement grid = getTreeGrid();

        $("button").id("init-large-data-set").click();
        $("button").id("expand-recursively").click();

        // Scroll as far as possible
        grid.scrollToRowByPath(1000000);
        assertExpandedNodesPopulated(grid);

        // Repeat
        grid.scrollToRowByPath(1000000);
        assertExpandedNodesPopulated(grid);
    }

    private void assertExpandedNodesPopulated(TreeGridElement grid) {
        int firstVisibleIndex = grid.getFirstVisibleRowIndex();
        int lastVisibleIndex = grid.getLastVisibleRowIndex();
        for (int i = lastVisibleIndex; i >= firstVisibleIndex; i--) {
            String cellText = grid.getCell(i, 0).getText();
            if (cellText.contains("Dad")) {
                String sonText = grid.getCell(i + 1, 0).getText();
                Assert.assertTrue(sonText + " is not a Son item",
                        sonText.contains("Son"));

            }
        }
    }

}
