package com.vaadin.flow.component.treegrid.it;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.Route;

@Route("treegrid-expand-all")
public class TreeGridExpandAllPage extends Div {

    public TreeGridExpandAllPage() {
        TreeGrid<String> grid = new TreeGrid<>();
        grid.setId("treegrid");
        grid.addHierarchyColumn(String::toString).setHeader("String")
                .setId("string");

        add(grid);

        TreeData<String> data = new TreeData<>();

        Map<String, String> parentPathMap = new HashMap<>();

        TreeGridHugeTreePage.addRootItems("Granddad", 1, data, parentPathMap)
                .forEach(granddad -> TreeGridHugeTreePage
                        .addItems("Dad", 1, granddad, data, parentPathMap)
                        .forEach(dad -> TreeGridHugeTreePage.addItems("Son", 3,
                                dad, data, parentPathMap)));

        TreeDataProvider<String> dataprovider = new TreeDataProvider<>(data);
        grid.setDataProvider(dataprovider);
        grid.expandRecursively(data.getRootItems(), 3);

        NativeButton collapse = new NativeButton("Collapse All",
                event -> grid.collapseRecursively(data.getRootItems(), 3));
        collapse.setId("collapse");
        add(collapse);

        NativeButton expand = new NativeButton("Expand All",
                event -> grid.expandRecursively(data.getRootItems(), 3));
        expand.setId("expand");
        add(expand);

        NativeButton addNew = new NativeButton("Add New son", event -> {
            dataprovider.getTreeData().addItem("Dad 0/0", "New son");
            dataprovider.refreshAll();
            event.getSource().setEnabled(false);
        });
        addNew.setId("add-new");
        add(addNew);
    }
}
