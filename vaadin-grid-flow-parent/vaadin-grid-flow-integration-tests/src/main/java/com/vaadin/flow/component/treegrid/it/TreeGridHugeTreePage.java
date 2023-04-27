/*
 * Copyright 2000-2023 Vaadin Ltd.
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

@Route("vaadin-grid/treegrid-huge-tree")
public class TreeGridHugeTreePage extends Div {

    private TreeGrid<String> treeGrid;

    public TreeGridHugeTreePage() {

        treeGrid = new TreeGrid<>();
        treeGrid.setDataProvider(initializeDataProvider(3));
        treeGrid.setWidth("100%");
        treeGrid.addHierarchyColumn(String::toString).setHeader("String")
                .setId("string");
        treeGrid.addColumn((i) -> "--").setHeader("Nothing");
        treeGrid.setId("testComponent");

        NativeButton expand = new NativeButton("Expand Granddad 1");
        expand.addClickListener(event -> treeGrid.expand("Granddad 1"));
        NativeButton collapse = new NativeButton("Collapse Granddad 1");
        collapse.addClickListener(event -> treeGrid.collapse("Granddad 1"));
        NativeButton initLargeDataSet = new NativeButton(
                "Init larger data set");
        initLargeDataSet.addClickListener(
                event -> treeGrid.setDataProvider(initializeDataProvider(300)));
        NativeButton expandRecursively = new NativeButton("Expand Recursively");
        expandRecursively.addClickListener(event -> treeGrid.expandRecursively(
                ((TreeDataProvider<String>) treeGrid.getDataProvider())
                        .getTreeData().getRootItems(),
                2));

        add(treeGrid, expand, collapse, initLargeDataSet, expandRecursively);
    }

    private TreeDataProvider<String> initializeDataProvider(int dadCount) {
        TreeData<String> data = new TreeGridStringDataGenerator().generate(3,
                dadCount, 300);
        return new TreeDataProvider<>(data);
    }
}
