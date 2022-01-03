/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

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
        TreeData<String> data = new TreeData<>();

        final Map<String, String> parentPathMap = new HashMap<>();

        addRootItems("Granddad", 3, data, parentPathMap)
                .forEach(granddad -> addItems("Dad", dadCount, granddad, data,
                        parentPathMap)
                                .forEach(dad -> addItems("Son", 300, dad, data,
                                        parentPathMap)));

        return new TreeDataProvider<>(data);
    }

    static List<String> addRootItems(String name, int numberOfItems,
            TreeData<String> data, Map<String, String> parentPathMap) {
        return addItems(name, numberOfItems, null, data, parentPathMap);
    }

    static List<String> addItems(String name, int numberOfItems, String parent,
            TreeData<String> data, Map<String, String> parentPathMap) {
        List<String> items = new ArrayList<>();
        IntStream.range(0, numberOfItems).forEach(index -> {
            String parentPath = parentPathMap.get(parent);
            String thisPath = Optional.ofNullable(parentPath)
                    .map(path -> path + "/" + index).orElse("" + index);
            String item = addItem(name, thisPath);
            parentPathMap.put(item, thisPath);
            data.addItem(parent, item);
            items.add(item);
        });
        return items;
    }

    private static String addItem(String name, String path) {
        return (name + " " + path).trim();
    }
}
