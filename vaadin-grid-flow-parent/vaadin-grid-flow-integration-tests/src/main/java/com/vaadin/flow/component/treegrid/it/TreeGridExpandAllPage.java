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

@Route("vaadin-grid/treegrid-expand-all")
public class TreeGridExpandAllPage extends Div {
    private TreeGrid<String> treeGrid;
    private TreeDataProvider<String> inMemoryDataProvider;

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

        // second grid
        initializeDataProvider();
        treeGrid = new TreeGrid<>();
        treeGrid.setDataProvider(inMemoryDataProvider);
        treeGrid.setWidth("100%");
        treeGrid.addHierarchyColumn(String::toString).setHeader("String").setAutoWidth(true);
        treeGrid.addColumn((i) -> "Second Column").setHeader("Second Column").setAutoWidth(true);
        treeGrid.setId("second-grid");
        treeGrid.addCollapseListener(e -> treeGrid.recalculateColumnWidths());
        treeGrid.addExpandListener(e -> treeGrid.recalculateColumnWidths());

        add(treeGrid);
    }
    private void initializeDataProvider() {
        TreeData<String> data = new TreeData<>();

        final Map<String, String> parentPathMap = new HashMap<>();

        addRootItems("Granddad", 3, data, parentPathMap).forEach(
                granddad -> addItems("Dad is very long and large", 3, granddad, data, parentPathMap)
                        .forEach(dad -> addItems("Son", 300, dad, data,
                                parentPathMap)));

        inMemoryDataProvider = new TreeDataProvider<>(data);
    }

    static List<String> addRootItems(String name, int numberOfItems,
                                     TreeData<String> data, Map<String, String> parentPathMap) {
        return addItems(name, numberOfItems, null, data, parentPathMap);
    }

    static List<String> addItems(String name, int numberOfItems,
                                 String parent, TreeData<String> data,
                                 Map<String, String> parentPathMap) {
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
