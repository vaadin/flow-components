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

import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/tree-grid-child-removal")
public class TreeGridChildRemoval extends VerticalLayout {

    public TreeGridChildRemoval() {
        TreeGrid<String> grid = new TreeGrid<>();
        grid.addHierarchyColumn(String::toString).setHeader("Name");

        String root = "Root";
        String child = "child";
        String subChild = "Sub-child";
        String child2 = "child2";
        String subChild2 = "Sub-child2";
        String child3 = "child3";

        TreeData<String> td = grid.getTreeData();
        td.addItem(null, root);
        td.addItem(root, child);
        td.addItem(child, subChild);
        td.addItem(root, child2);
        td.addItem(child2, subChild2);
        td.addItem(root, child3);
        grid.expand(root, child, subChild, child2);

        NativeButton buttonR = new NativeButton("Remove recur first child",
                e -> {
                    td.removeItem(child);
                    grid.getDataProvider().refreshItem(root, true);
                });
        buttonR.setId("remove1");
        NativeButton buttonR2 = new NativeButton("Remove recur 2nd child",
                e -> {
                    td.removeItem(child2);
                    grid.getDataProvider().refreshItem(root, true);
                });
        buttonR2.setId("remove2");
        add(grid, buttonR, buttonR2);
    }
}
