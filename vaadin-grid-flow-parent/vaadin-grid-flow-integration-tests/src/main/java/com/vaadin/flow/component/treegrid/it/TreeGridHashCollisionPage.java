/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-grid/treegrid-hash-collision")
public class TreeGridHashCollisionPage extends VerticalLayout {
    private final TreeGrid<String> treeGrid = new TreeGrid<>();

    public TreeGridHashCollisionPage() {
        TreeData<String> stringTreeData = new TreeData<>();
        // "Aa".hashCode() == "BB".hashCode()
        // Thus if TreeGrid identity provider is based on hash, collision will
        // happen and TreeGrid will fail to render correctly
        stringTreeData.addItem(null, "Aa");
        stringTreeData.addItem("Aa", "BB");
        treeGrid.setDataProvider(new TreeDataProvider<>(stringTreeData));
        treeGrid.addHierarchyColumn(s -> s);
        treeGrid.expand("Aa");
        add(treeGrid);
    }
}
