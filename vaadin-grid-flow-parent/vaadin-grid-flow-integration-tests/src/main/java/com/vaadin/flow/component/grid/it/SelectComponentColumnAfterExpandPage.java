/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.router.Route;

@Route(SelectComponentColumnAfterExpandPage.VIEW)
public class SelectComponentColumnAfterExpandPage extends Div {

    public static final String VIEW = "vaadin-grid/select-component-column-after-expand";

    public SelectComponentColumnAfterExpandPage() {
        TreeGrid<String> grid = new TreeGrid<>();
        grid.addHierarchyColumn(String::toString).setHeader("HierarchyColumn");
        grid.addComponentColumn(Span::new).setHeader("ComponentColumn");

        String root = "Root";
        String child = "child";
        String child2 = "child2";
        String subChild = "sub-child";

        TreeData<String> td = grid.getTreeData();
        td.addItem(null, root);
        td.addItem(root, child);
        td.addItem(root, child2);
        td.addItem(child, subChild);

        // Expand All initially
        grid.expandRecursively(grid.getTreeData().getRootItems(), 100);

        Button collapse = new Button("Collapse All", e -> {
            grid.collapseRecursively(grid.getTreeData().getRootItems(), 100);
        });
        collapse.setId("collapse-button");

        Button expand = new Button("Expand All", e -> {
            grid.expandRecursively(grid.getTreeData().getRootItems(), 100);
        });
        expand.setId("expand-button");

        Button select = new Button("Select 'child'", e -> {
            grid.select(child);
        });
        select.setId("select-button");
        add(grid, collapse, expand, select);
    }
}
