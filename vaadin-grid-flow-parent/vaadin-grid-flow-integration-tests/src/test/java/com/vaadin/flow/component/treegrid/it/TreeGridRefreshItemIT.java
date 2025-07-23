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

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/treegrid-refresh-item")
public class TreeGridRefreshItemIT extends AbstractComponentIT {
    private TreeGridElement treeGrid;

    @Before
    public void init() {
        open();
        treeGrid = $(TreeGridElement.class).first();
    }

    @Test
    public void refreshRootItem_rootItemRefreshed() {
        clickElementWithJs("refresh-item-0");

        assertRowContents(0, "Updated", "Item 1", "Item 2", "Item 3");
    }

    @Test
    public void expandRootItem_refreshChildItem_childItemRefreshed() {
        treeGrid.expandWithClick(0);
        clickElementWithJs("refresh-item-0-1");

        assertRowContents(0, "Item 0", "Item 0-0", "Updated", "Item 0-2",
                "Item 1");
    }

    @Test
    public void expandRootItem_scrollToBottom_refreshChildItem_scrollToTop_childItemRefreshed() {
        treeGrid.expandWithClick(0);

        treeGrid.scrollToRow(99);
        assertRowContents(treeGrid.getRowCount() - 1, "Item 99");

        clickElementWithJs("refresh-item-0-1");
        treeGrid.scrollToRow(0);

        assertRowContents(0, "Item 0", "Item 0-0", "Updated", "Item 0-2",
                "Item 1");
    }

    @Test
    public void expandRootItem_refreshRootAndChildItem_rootAndChildItemsRefreshed() {
        treeGrid.expandWithClick(0);
        clickElementWithJs("refresh-item-0-and-0-1");

        assertRowContents(0, "Updated", "Item 0-0", "Updated", "Item 0-2",
                "Item 1");
    }

    @Test
    public void refreshChildItem_expandParentItem_childItemRefreshed() {
        clickElementWithJs("refresh-item-0-1");
        treeGrid.expandWithClick(0);

        assertRowContents(0, "Item 0", "Item 0-0", "Updated", "Item 0-2",
                "Item 1");
    }

    private void assertRowContents(int startRowIndex, String... expected) {
        String[] actual = IntStream.range(0, expected.length)
                .mapToObj(i -> treeGrid.getRow(startRowIndex + i).getText())
                .toArray(String[]::new);
        Assert.assertArrayEquals(expected, actual);
    }
}
