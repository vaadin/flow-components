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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/tree-grid-basic")
public class TreeGridBasicPage extends Div {

    public TreeGridBasicPage() {
        TreeGrid<String> grid = new TreeGrid<>();
        grid.addHierarchyColumn(item -> item).setHeader("Item");

        TreeData<String> data = new TreeGridStringDataBuilder()
                .addLevel("Item", 50)
                .addLevel("Item ", 50)
                .addLevel("Item  ", 50)
                .build();

        grid.setDataProvider(new TreeDataProvider<>(data));

        NativeButton expandAll = new NativeButton("Expand all",
                e -> grid.expandRecursively(data.getRootItems(), 3));
        expandAll.setId("expand-all");

        add(grid, expandAll);
    }

}
