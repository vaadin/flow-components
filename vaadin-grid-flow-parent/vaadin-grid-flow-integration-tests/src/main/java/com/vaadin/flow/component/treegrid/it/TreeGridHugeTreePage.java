/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
        TreeData<String> data = new TreeGridStringDataBuilder()
                .addLevel("Granddad", 3).addLevel("Dad", dadCount)
                .addLevel("Son", 300).build();
        return new TreeDataProvider<>(data);
    }
}
