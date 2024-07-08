/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.bean.HierarchicalTestBean;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/treegrid-refresh-all")
public class TreeGridRefreshAllPage extends Div {

    public TreeGridRefreshAllPage() {
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
}
