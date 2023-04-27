package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
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

        TreeGridHugeTreePage.addRootItems("Granddad", 50, data, parentPathMap)
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

        Input scrollToIndex = new Input(ValueChangeMode.ON_BLUR);
        scrollToIndex.setPlaceholder("Scroll to index (format: 30-1-1)");
        scrollToIndex.setWidth("200px");
        scrollToIndex.addValueChangeListener(event -> {
            int[] path = Arrays.stream(event.getValue().split("-"))
                    .mapToInt(Integer::parseInt).toArray();
            grid.scrollToIndex(path);
        });
        scrollToIndex.setId("scroll-to-index");

        add(grid, expandAll, scrollToStart, scrollToEnd, scrollToIndex);
    }
}
