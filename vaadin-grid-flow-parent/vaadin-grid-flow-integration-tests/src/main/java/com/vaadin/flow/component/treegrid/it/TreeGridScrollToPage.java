package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.Route;

import java.util.HashMap;
import java.util.Map;

@Route("vaadin-grid/treegrid-scroll-to")
public class TreeGridScrollToPage extends Div {

    public TreeGridScrollToPage() {
        TreeGrid<String> grid = new TreeGrid<>();
        grid.setPageSize(10);
        grid.addHierarchyColumn(String::toString).setHeader("Item");

        add(grid);

        TreeData<String> data = new TreeData<>();

        Map<String, String> parentPathMap = new HashMap<>();

        TreeGridHugeTreePage.addRootItems("Granddad", 40, data, parentPathMap)
                .forEach(granddad -> TreeGridHugeTreePage
                        .addItems("Dad", 3, granddad, data, parentPathMap)
                        .forEach(dad -> TreeGridHugeTreePage.addItems("Son", 3,
                                dad, data, parentPathMap)));

        TreeDataProvider<String> dataProvider = new TreeDataProvider<>(data);
        grid.setDataProvider(dataProvider);

        NativeButton expandAll = new NativeButton("Expand all",
                e -> grid.expandRecursively(data.getRootItems(), 3));
        expandAll.setId("expand-all");

        NativeButton scrollToStart = new NativeButton("Scroll to start",
                e -> grid.scrollToStart());
        scrollToStart.setId("scroll-to-start");

        NativeButton scrollToEnd = new NativeButton("Scroll to end",
                e -> grid.scrollToEnd());
        scrollToEnd.setId("scroll-to-end");

        NativeButton scrollToIndex30 = new NativeButton("Scroll to index 30",
                e -> grid.scrollToIndex(30));
        scrollToIndex30.setId("scroll-to-index-30");

        NativeButton scrollToIndex30_2 = new NativeButton(
                "Scroll to index 30-2", e -> grid.scrollToIndex(30, 2));
        scrollToIndex30_2.setId("scroll-to-index-30-2");

        NativeButton scrollToIndex30_2_2 = new NativeButton(
                "Scroll to index 30-2-2", e -> grid.scrollToIndex(30, 2, 2));
        scrollToIndex30_2_2.setId("scroll-to-index-30-2-2");

        add(grid, expandAll, scrollToStart, scrollToEnd, scrollToIndex30,
                scrollToIndex30_2, scrollToIndex30_2_2);
    }
}
