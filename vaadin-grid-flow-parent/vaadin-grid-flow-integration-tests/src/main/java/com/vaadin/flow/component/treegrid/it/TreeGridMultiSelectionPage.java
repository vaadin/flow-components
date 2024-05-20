package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/treegrid-multi-selection")
public class TreeGridMultiSelectionPage extends Div {

    public TreeGridMultiSelectionPage() {
        TreeGrid<String> treeGrid = new TreeGrid<>();
        treeGrid.addHierarchyColumn(String::toString).setHeader("Item");
        treeGrid.setSelectionMode(TreeGrid.SelectionMode.MULTI);

        TreeData<String> data = new TreeGridStringDataBuilder()
                .addLevel("Granddad", 5000).addLevel("Dad", 10).build();

        treeGrid.setDataProvider(new TreeDataProvider<>(data));

        NativeButton expandAll = new NativeButton("Expand all",
                e -> {
                    treeGrid.expandRecursively(data.getRootItems(), 3);
        });
        expandAll.setId("expand-all");

        add(treeGrid, expandAll);
    }
}
