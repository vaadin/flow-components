/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.Route;

import static com.vaadin.flow.component.treegrid.it.TreeGridHugeTreePage.addItems;
import static com.vaadin.flow.component.treegrid.it.TreeGridHugeTreePage.addRootItems;

@Route("vaadin-grid/treegrid-huge-tree-navigation")
public class TreeGridHugeTreeNavigationPage extends Div {

    private TreeGrid<String> treeGrid;
    private TreeDataProvider<String> inMemoryDataProvider;

    public TreeGridHugeTreeNavigationPage() {
        initializeDataProvider();
        treeGrid = new TreeGrid<>();
        treeGrid.setWidth("100%");
        treeGrid.setDataProvider(inMemoryDataProvider);
        treeGrid.addHierarchyColumn(String::toString).setHeader("String")
                .setId("string");
        treeGrid.addColumn((i) -> "--").setHeader("Nothing");
        treeGrid.setId("testComponent");
        add(treeGrid);
    }

    private void initializeDataProvider() {
        TreeData<String> data = new TreeData<>();

        final Map<String, String> parentPathMap = new HashMap<>();

        addRootItems("Granddad", 3, data, parentPathMap).forEach(
                granddad -> addItems("Dad", 3, granddad, data, parentPathMap)
                        .forEach(dad -> addItems("Son", 300, dad, data,
                                parentPathMap)));

        inMemoryDataProvider = new TreeDataProvider<>(data);
    }

}
