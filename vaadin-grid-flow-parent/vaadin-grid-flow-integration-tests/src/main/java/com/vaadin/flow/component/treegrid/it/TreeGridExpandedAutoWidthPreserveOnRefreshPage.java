/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import java.util.List;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/treegrid-expanded-auto-width-preserve-on-refresh")
@PreserveOnRefresh
public class TreeGridExpandedAutoWidthPreserveOnRefreshPage
        extends VerticalLayout {

    public TreeGridExpandedAutoWidthPreserveOnRefreshPage() {
        var grid = new TreeGrid<String>();
        grid.setAllRowsVisible(true);
        grid.addHierarchyColumn(String::toString).setHeader("Name")
                .setAutoWidth(true).setFlexGrow(0);

        var items = List.of("Item 1", "Parent 1");
        grid.setItems(items, item -> {
            if (item.startsWith("Parent")) {
                var index = Integer.parseInt(item.split(" ")[1]);
                if (index < 6) {
                    return List.of("Parent " + (index + 1));
                } else {
                    return List.of("Item");
                }
            }
            return List.of();
        });
        grid.expandRecursively(items, Integer.MAX_VALUE);

        add(grid);
    }
}
