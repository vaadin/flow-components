package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import java.util.Arrays;

@Route("vaadin-grid/treegrid-expanded-keys")
public class TreeGridExpandedKeysPage extends Div {

    public TreeGridExpandedKeysPage() {
        var grid = new TreeGrid<String>();
        grid.setPageSize(10);
        grid.addHierarchyColumn(String::toString).setHeader("Item");

        var data = new TreeData<String>();
        data.addItems(null, "Granddad");
        for (int i = 0; i < 20; i++) {
            data.addItems("Granddad", "Dad " + i);
            for (int j = 0; j < 20; j++) {
                data.addItems("Dad " + i, "Son " + i + "/" + j);
            }
        }

        grid.setDataProvider(new TreeDataProvider<>(data));

        NativeButton expandAll = new NativeButton("Expand all",
                e -> grid.expandRecursively(data.getRootItems(), 1));
        expandAll.setId("expand-all");

        NativeButton showKeys = new NativeButton("Show keys",
                e -> showKeys(grid, "afterExpandKeys"));
        showKeys.setId("show-keys");

        add(grid, expandAll, showKeys);
        showKeys(grid, "originalkeys");
    }

    private void showKeys(TreeGrid<String> grid, String id) {
        StringBuilder keys = new StringBuilder();
        DataKeyMapper<String> keyMapper = grid.getDataCommunicator()
                .getKeyMapper();
        for (int i = 0; i < 20; i++) {
            keys.append(keyMapper.key("Dad " + i)).append(":");
        }
        Span span = new Span(keys.substring(0, keys.length() - 1));
        span.setId(id);
        add(span);
    }
}
