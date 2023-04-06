/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-grid/gridsetitemsafterdetachpage")
public class GridSetItemsAfterDetachPage extends VerticalLayout {

    public GridSetItemsAfterDetachPage() {
        List<String> items = Arrays.asList("foo", "bar");

        Grid<String> grid = new Grid<>();
        grid.setItems(items);
        grid.addColumn(s -> s);

        NativeButton detach = new NativeButton("detach", e -> remove(grid));
        NativeButton setItemsAndAttach = new NativeButton(
                "set items and attach", e -> {
                    grid.setItems(items);
                    add(grid);
                });

        detach.setId("detach");
        setItemsAndAttach.setId("set-items-and-attach");

        add(grid, detach, setItemsAndAttach);
    }

}
