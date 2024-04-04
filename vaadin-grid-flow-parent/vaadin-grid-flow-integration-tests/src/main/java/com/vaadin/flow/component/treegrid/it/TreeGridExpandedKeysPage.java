package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/treegrid-expanded-keys")
public class TreeGridExpandedKeysPage extends Div {

    public TreeGridExpandedKeysPage() {
        TreeGrid<String> grid = new TreeGrid<>();
        grid.setPageSize(10);
        grid.addHierarchyColumn(Object::toString).setHeader("Item");

        var data = new TreeData<String>();

        for (int i = 0; i < 500; i++) {
            data.addRootItems("Item-" + i);
        }

        for (int i = 0; i < 500; i++) {
            data.addItems("Item-0", "Item-0-" + i);
        }

        for (int j = 0; j < 500; j++) {
            data.addItems("Item-0-0", "Item-0-0-" + j);
        }

        grid.setDataProvider(new TreeDataProvider<>(data));

        add(grid);

        NativeButton expandAll = new NativeButton("Expand all",
                e -> grid.expandRecursively(data.getRootItems(), 1));
        expandAll.setId("expand-all");

        NativeButton showKeysAfterExpand = new NativeButton(
                "Show keys after expand",
                e -> showKeys(grid, "afterExpandKeys"));
        showKeysAfterExpand.setId("show-keys-after-expand");

        NativeButton scrollTo = new NativeButton("Scroll to 0-250",
                e -> grid.scrollToIndex(0, 250));
        scrollTo.setId("scroll-to-0-250");

        NativeButton showKeysAfterFirstScroll = new NativeButton(
                "Show keys after first scroll",
                e -> showKeys(grid, "afterFirstScroll"));
        showKeysAfterFirstScroll.setId("show-keys-after-first-scroll");

        NativeButton scrollToStart = new NativeButton("Scroll to start",
                e -> grid.scrollToStart());
        scrollToStart.setId("scroll-to-start");

        NativeButton showKeysAfterSecondScroll = new NativeButton(
                "Show keys after second scroll",
                e -> showKeys(grid, "afterSecondScroll"));
        showKeysAfterSecondScroll.setId("show-keys-after-second-scroll");

        add(grid, expandAll, showKeysAfterExpand, scrollTo,
                showKeysAfterFirstScroll, scrollToStart,
                showKeysAfterSecondScroll);
        showKeys(grid, "originalkeys");
    }

    private void showKeys(TreeGrid<String> grid, String id) {
        DataKeyMapper<String> keyMapper = grid.getDataCommunicator()
                .getKeyMapper();
        Span span = new Span(
                keyMapper.key("Item-0") + ":" + keyMapper.key("Item-0-0"));
        span.setId(id);
        add(span);
    }
}
