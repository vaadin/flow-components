/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.Route;

import static com.vaadin.flow.component.treegrid.it.TreeGridHugeTreePage.addItems;
import static com.vaadin.flow.component.treegrid.it.TreeGridHugeTreePage.addRootItems;

@Route("vaadin-grid/treegrid-page-size")
public class TreeGridPageSizePage extends Div {

    private TextArea log;

    /**
     * Creates a view with a grid with page size of 10.
     */
    public TreeGridPageSizePage() {

        TreeGrid<String> grid = new TreeGrid<>();
        grid.setPageSize(10);
        grid.addHierarchyColumn(String::toString).setHeader("String")
                .setId("string");

        TreeData<String> data = new TreeData<>();

        final Map<String, String> parentPathMap = new HashMap<>();

        addRootItems("Granddad", 3, data, parentPathMap).forEach(
                granddad -> addItems("Dad", 3, granddad, data, parentPathMap)
                        .forEach(dad -> addItems("Son", 300, dad, data,
                                parentPathMap)));

        TreeDataProvider<String> dataProvider = new TreeDataProvider<String>(
                data) {

            @Override
            public Stream<String> fetchChildren(
                    HierarchicalQuery<String, SerializablePredicate<String>> query) {
                if (log != null) {
                    log.setValue(String.format(
                            "Query offset: %d Query limit: %d Query parent: %s",
                            query.getOffset(), query.getLimit(),
                            query.getParentOptional().map(String::valueOf)
                                    .orElse("root"))
                            + "\n" + log.getValue());
                }
                return super.fetchChildren(query);
            }

        };
        grid.setDataProvider(dataProvider);

        grid.expandRecursively(data.getRootItems(), 3);

        log = new TextArea();
        log.setId("log");
        log.setHeight("300px");
        log.setWidth("100%");
        NativeButton clearLog = new NativeButton("Clear", event -> log.clear());
        clearLog.setId("clear-log");

        Input size = new Input();
        size.setId("size-input");
        NativeButton button = new NativeButton("Change page size", event -> {
            int pageSize = Integer.parseInt(size.getValue());
            grid.setPageSize(pageSize);
        });
        button.setId("size-submit");

        add(grid, log, clearLog, new Div(size, button));
    }

}
