/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.dom.ElementUtil;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/inert-tree-grid")
public class InertTreeGridPage extends Div {

    public InertTreeGridPage() {
        var grid = new TreeGrid<String>();
        ElementUtil.setInert(grid.getElement(), true);

        grid.addHierarchyColumn(String::toString).setHeader("Item");
        add(grid);

        var data = new TreeGridStringDataBuilder().addLevel("Parent", 100)
                .addLevel("Child", 100).build();

        grid.setDataProvider(new TreeDataProvider<>(data));

        var expandFirst = new NativeButton("Expand first item",
                e -> grid.expand(data.getRootItems().get(0)));
        expandFirst.setId("expand-first");

        var setAllRowsVisible = new NativeButton("Set all rows visible",
                e -> grid.setAllRowsVisible(true));
        setAllRowsVisible.setId("set-all-rows-visible");

        add(expandFirst, setAllRowsVisible, grid);
    }
}
