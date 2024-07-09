/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-grid/detach-reattach-page")
public class DetachReattachPage extends Div {
    public DetachReattachPage() {
        Grid<String> grid = new Grid<String>();
        grid.setItems("A", "B", "C");
        grid.addColumn(x -> x).setHeader("Col").setSortable(true);

        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        NativeButton btnAttach = new NativeButton("Attach", e -> add(grid));
        btnAttach.setId("attach-button");

        NativeButton btnDetach = new NativeButton("Detach", e -> remove(grid));
        btnDetach.setId("detach-button");

        NativeButton resetSortingButton = new NativeButton("Reset sorting",
                e -> {
                    grid.sort(null);
                });
        resetSortingButton.setId("reset-sorting-button");

        add(btnAttach, btnDetach, resetSortingButton, grid);
    }
}
