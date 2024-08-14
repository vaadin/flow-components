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

@Route("vaadin-grid/treegrid-huge-tree")
public class TreeGridHugeTreePage extends Div {

    private TreeGrid<String> treeGrid;

    public TreeGridHugeTreePage() {

        treeGrid = new TreeGrid<>();
        treeGrid.setDataProvider(initializeDataProvider(3, 3));
        treeGrid.setWidth("100%");
        treeGrid.addHierarchyColumn(String::toString).setHeader("String")
                .setId("string");
        treeGrid.addColumn((i) -> "--").setHeader("Nothing");
        treeGrid.setId("testComponent");

        NativeButton expandSecondRow = new NativeButton("Expand Granddad 1",
                event -> treeGrid.expand("Granddad 1"));
        expandSecondRow.setId("expand-second-row");

        NativeButton collapseSecondRowButton = new NativeButton(
                "Collapse Granddad 1",
                event -> treeGrid.collapse("Granddad 1"));
        collapseSecondRowButton.setId("collapse-second-row");

        NativeButton initLargeDataSet = new NativeButton("Init larger data set",
                event -> treeGrid
                        .setDataProvider(initializeDataProvider(3, 300)));
        initLargeDataSet.setId("init-large-data-set");

        NativeButton initHugeDataSet = new NativeButton("Init huge data set",
                event -> treeGrid
                        .setDataProvider(initializeDataProvider(300, 1)));
        initHugeDataSet.setId("init-huge-data-set");

        NativeButton checkFirstRootItemKey = new NativeButton(
                "check key of first root item", event -> {
                    event.getSource().setText(
                            String.valueOf(treeGrid.getDataCommunicator()
                                    .getKeyMapper().has("Granddad 0")));
                });
        checkFirstRootItemKey.setId("check-first-root-item-key");

        NativeButton expandRecursively = new NativeButton("Expand Recursively",
                event -> treeGrid.expandRecursively(
                        ((TreeDataProvider<String>) treeGrid.getDataProvider())
                                .getTreeData().getRootItems(),
                        2));
        expandRecursively.setId("expand-recursively");

        add(treeGrid, expandSecondRow, collapseSecondRowButton,
                initLargeDataSet, expandRecursively, initHugeDataSet,
                checkFirstRootItemKey);
    }

    private TreeDataProvider<String> initializeDataProvider(int granddadCount,
            int dadCount) {
        TreeData<String> data = new TreeGridStringDataBuilder()
                .addLevel("Granddad", granddadCount).addLevel("Dad", dadCount)
                .addLevel("Son", 300).build();
        return new TreeDataProvider<>(data);
    }
}
