/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.Route;

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
        TreeData<String> data = new TreeGridStringDataBuilder()
                .addLevel("Granddad", 3).addLevel("Dad", 3).addLevel("Son", 300)
                .build();

        inMemoryDataProvider = new TreeDataProvider<>(data);
    }

}
