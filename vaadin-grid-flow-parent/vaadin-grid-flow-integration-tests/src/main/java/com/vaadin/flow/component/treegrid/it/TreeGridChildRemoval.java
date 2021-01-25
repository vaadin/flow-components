package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/tree-grid-child-removal")
public class TreeGridChildRemoval extends VerticalLayout {

    public TreeGridChildRemoval() {
        TreeGrid<String> grid = new TreeGrid<>();
        grid.addHierarchyColumn(String::toString).setHeader("Name");

        String root = "Root";
        String child = "child";
        String subChild = "Sub-child";
        String child2 = "child2";
        String subChild2 = "Sub-child2";
        String child3 = "child3";

        TreeData<String> td = grid.getTreeData();
        td.addItem(null, root);
        td.addItem(root, child);
        td.addItem(child, subChild);
        td.addItem(root, child2);
        td.addItem(child2, subChild2);
        td.addItem(root, child3);
        grid.expand(root, child, subChild, child2);

        NativeButton button = new NativeButton("Remove first child", e -> {
            td.removeItem(child);
            grid.getDataProvider().refreshItem(root);
        });

        NativeButton button2 = new NativeButton("Remove 2nd child", e -> {
            td.removeItem(child2);
            grid.getDataProvider().refreshItem(root);
        });

        NativeButton buttonR = new NativeButton("Remove recur first child", e -> {
            td.removeItem(child);
            grid.getDataProvider().refreshItem(root, true);
        });

        NativeButton buttonR2 = new NativeButton("Remove recur 2nd child", e -> {
            td.removeItem(child2);
            grid.getDataProvider().refreshItem(root, true);
        });
        add(grid, button, button2, buttonR, buttonR2);
    }
}
