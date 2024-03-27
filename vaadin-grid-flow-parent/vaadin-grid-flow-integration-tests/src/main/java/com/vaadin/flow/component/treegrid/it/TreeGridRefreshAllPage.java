/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.bean.HierarchicalTestBean;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Route("vaadin-grid/treegrid-refresh-all")
public class TreeGridRefreshAllPage extends Div {

    public TreeGridRefreshAllPage() {
        addRefreshAllGrid();
        addGridWithPageSize();
    }

    private void addRefreshAllGrid() {
        TreeGrid<HierarchicalTestBean> grid = new TreeGrid<>();
        grid.addHierarchyColumn(HierarchicalTestBean::toString);
        LazyHierarchicalDataProvider dataProvider = new LazyHierarchicalDataProvider(
                50, 4);
        grid.setDataProvider(dataProvider);

        NativeButton refreshAll = new NativeButton("Refresh All",
                e -> grid.getDataProvider().refreshAll());
        refreshAll.setId("refresh-all");

        NativeButton clear = new NativeButton("clear", e -> {
            dataProvider.clear();
            dataProvider.refreshAll();
        });
        clear.setId("clear");

        add(grid, refreshAll, clear);
    }

    private void addGridWithPageSize() {
        TreeGrid<String> treeGrid = new TreeGrid<>();
        treeGrid.setId("grid-with-page-size");
        treeGrid.addHierarchyColumn(item -> item);

        TreeData<String> treeData = new TreeData<>();
        List<String> rootItems = IntStream.iterate(1, i -> i + 1).limit(11)
                .mapToObj(String::valueOf).collect(Collectors.toList());
        treeData.addRootItems(rootItems);
        rootItems.forEach(
                rootItem -> treeData.addItem(rootItem, "item: " + rootItem));

        TreeDataProvider<String> provider = new TreeDataProvider<>(treeData);
        treeGrid.setDataProvider(provider);

        treeGrid.setPageSize(10);
        treeGrid.expand(treeData.getRootItems());

        Button button = new Button("Refresh All", e -> provider.refreshAll());
        button.setId("refresh-all-grid-with-page-size");

        add(treeGrid, button);
    }
}
