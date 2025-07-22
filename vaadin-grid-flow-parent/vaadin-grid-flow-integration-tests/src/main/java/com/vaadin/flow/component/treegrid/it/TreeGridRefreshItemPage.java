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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/treegrid-refresh-item")
public class TreeGridRefreshItemPage extends Div {
    private TreeGrid<TestTreeData.Item> treeGrid;

    public TreeGridRefreshItemPage() {
        treeGrid = new TreeGrid<>();
        treeGrid.addHierarchyColumn(TestTreeData.Item::getName);
        treeGrid.setTreeData(new TestTreeData(100, 3, 3));

        NativeButton refreshItem_0 = new NativeButton("Refresh item 0",
                event -> refreshItemByPath(0));
        refreshItem_0.setId("refresh-item-0");

        NativeButton refreshItem_0_1 = new NativeButton("Refresh item 0-1",
                event -> refreshItemByPath(0, 1));
        refreshItem_0_1.setId("refresh-item-0-1");

        NativeButton refreshItem_0_and_0_1 = new NativeButton(
                "Refresh item 0 and 0-1", event -> {
                    refreshItemByPath(0);
                    refreshItemByPath(0, 1);
                });
        refreshItem_0_and_0_1.setId("refresh-item-0-and-0-1");

        add(treeGrid, refreshItem_0, refreshItem_0_1, refreshItem_0_and_0_1);
    }

    private void refreshItemByPath(int... indexes) {
        TestTreeData treeData = (TestTreeData) treeGrid.getTreeData();
        TestTreeData.Item item = null;

        for (int i = 0; i < indexes.length; i++) {
            int index = indexes[i];
            if (item == null) {
                item = treeData.getRootItems().get(index);
            } else {
                item = treeData.getChildren(item).get(index);
            }
        }

        item.setName("Updated");
        treeGrid.getDataProvider().refreshItem(item);
    }
}
