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

@Route("vaadin-grid/treegrid-expand-all")
public class TreeGridExpandAllPage extends Div {
    private TreeGrid<String> treeGrid;
    private TreeDataProvider<String> inMemoryDataProvider;

    public TreeGridExpandAllPage() {
        TreeGrid<String> grid = new TreeGrid<>();
        grid.setId("treegrid");
        grid.addHierarchyColumn(String::toString).setHeader("String")
                .setId("string");

        add(grid);

        TreeData<String> data = new TreeGridStringDataBuilder()
                .addLevel("Granddad", 1).addLevel("Dad", 1).addLevel("Son", 3)
                .build();

        TreeDataProvider<String> dataprovider = new TreeDataProvider<>(data);
        grid.setDataProvider(dataprovider);
        grid.expandRecursively(data.getRootItems(), 3);

        NativeButton collapse = new NativeButton("Collapse All",
                event -> grid.collapseRecursively(data.getRootItems(), 3));
        collapse.setId("collapse");
        add(collapse);

        NativeButton expand = new NativeButton("Expand All",
                event -> grid.expandRecursively(data.getRootItems(), 3));
        expand.setId("expand");
        add(expand);

        NativeButton addNew = new NativeButton("Add New son", event -> {
            dataprovider.getTreeData().addItem("Dad 0/0", "New son");
            dataprovider.refreshAll();
            event.getSource().setEnabled(false);
        });
        addNew.setId("add-new");
        add(addNew);

        // second grid
        initializeDataProvider();
        treeGrid = new TreeGrid<>();
        treeGrid.setDataProvider(inMemoryDataProvider);
        treeGrid.setWidth("100%");
        treeGrid.addHierarchyColumn(String::toString).setHeader("String")
                .setAutoWidth(true);
        treeGrid.addColumn((i) -> "Second Column").setHeader("Second Column")
                .setAutoWidth(true);
        treeGrid.setId("second-grid");
        treeGrid.addCollapseListener(e -> treeGrid.recalculateColumnWidths());
        treeGrid.addExpandListener(e -> treeGrid.recalculateColumnWidths());

        add(treeGrid);
    }

    private void initializeDataProvider() {
        TreeData<String> data = new TreeGridStringDataBuilder()
                .addLevel("Granddad", 3)
                .addLevel("Dad is very long and large", 3).addLevel("Son", 300)
                .build();

        inMemoryDataProvider = new TreeDataProvider<>(data);
    }
}
