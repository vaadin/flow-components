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
        grid.setId("treegrid");
        grid.addHierarchyColumn(String::toString).setHeader("String")
                .setId("string");

        add(grid);

        TreeData<String> data = new TreeData<>();

        Map<String, String> parentPathMap = new HashMap<>();

        TreeGridHugeTreePage.addRootItems("Granddad", 200, data, parentPathMap)
                .forEach(granddad -> TreeGridHugeTreePage
                        .addItems("Dad", 100, granddad, data, parentPathMap)
                        .forEach(dad -> TreeGridHugeTreePage.addItems("Son", 50,
                                dad, data, parentPathMap)));

        TreeDataProvider<String> dataprovider = new TreeDataProvider<>(data);
        grid.setDataProvider(dataprovider);
        grid.expandRecursively(data.getRootItems(), 3);

        NativeButton scrollToStart = new NativeButton("Scroll to start",
                e -> grid.scrollToStart());
        scrollToStart.setId("scroll-to-start");

        NativeButton scrollToEnd = new NativeButton("Scroll to end",
                e -> grid.scrollToEnd());
        scrollToEnd.setId("scroll-to-end");

        NativeButton scrollToIndex150 = new NativeButton("Scroll to index 150",
                e -> grid.scrollToIndex(150));
        scrollToIndex150.setId("scroll-to-index-150");

        NativeButton scrollToIndex100_30 = new NativeButton("Scroll to index 100-30",
                e -> grid.scrollToIndex(100, 30));
        scrollToIndex100_30.setId("scroll-to-index-100-30");

        NativeButton scrollToIndex50_10_15 = new NativeButton("Scroll to index 50-10-15",
                e -> grid.scrollToIndex(50, 10, 15));
        scrollToIndex50_10_15.setId("scroll-to-index-50-10-15");

        add(grid, scrollToStart, scrollToEnd, scrollToIndex150, scrollToIndex100_30, scrollToIndex50_10_15);
    }
}
