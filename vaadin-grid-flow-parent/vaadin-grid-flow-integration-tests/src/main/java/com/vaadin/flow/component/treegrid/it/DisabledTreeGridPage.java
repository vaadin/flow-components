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

@Route("vaadin-grid/disabled-tree-grid")
public class DisabledTreeGridPage extends Div {

    public DisabledTreeGridPage() {
        var treeGrid = new TreeGrid<TestTreeData.Item>();
        treeGrid.addHierarchyColumn(TestTreeData.Item::getName)
                .setHeader("Item");
        treeGrid.setEnabled(false);
        treeGrid.setTreeData(new TestTreeData(100, 100));
        add(treeGrid);

        var setAllRowsVisible = new NativeButton("Set all rows visible",
                e -> treeGrid.setAllRowsVisible(true));
        setAllRowsVisible.setId("set-all-rows-visible");

        var scrollToEnd = new NativeButton("Scroll to end",
                e -> treeGrid.scrollToEnd());
        scrollToEnd.setId("scroll-to-end");

        add(setAllRowsVisible, scrollToEnd, treeGrid);
    }
}
