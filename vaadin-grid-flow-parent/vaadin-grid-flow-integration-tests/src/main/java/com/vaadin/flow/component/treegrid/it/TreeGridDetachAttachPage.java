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

@Route("vaadin-grid/treegrid-detach-attach")
public class TreeGridDetachAttachPage extends Div {

    private TreeGrid<HierarchicalTestBean> grid;

    public TreeGridDetachAttachPage() {
        grid = new TreeGrid<>();
        grid.addHierarchyColumn(HierarchicalTestBean::toString);
        grid.setDataProvider(new LazyHierarchicalDataProvider(200, 1));
        add(grid);

        NativeButton toggleAttached = new NativeButton("toggle attached", e -> {
            if (grid.getParent().isPresent()) {
                remove(grid);
            } else {
                add(grid);
            }
        });
        toggleAttached.setId("toggle-attached");
        add(toggleAttached);

        NativeButton useAutoWidthColumn = new NativeButton(
                "use auto-width column", e -> {
                    grid.removeAllColumns();
                    grid.addHierarchyColumn(HierarchicalTestBean::toString)
                            .setAutoWidth(true).setFlexGrow(0);
                });
        useAutoWidthColumn.setId("use-auto-width-column");
        add(useAutoWidthColumn);
    }
}
