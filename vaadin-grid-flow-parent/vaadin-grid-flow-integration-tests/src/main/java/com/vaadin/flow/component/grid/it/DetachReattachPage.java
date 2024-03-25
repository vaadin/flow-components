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
import com.vaadin.flow.component.grid.GridSingleSelectionModel;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

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

        NativeButton btnDisallowDeselect = new NativeButton("Disallow deselect",
                e -> {
                    GridSingleSelectionModel<String> singleSelect = (GridSingleSelectionModel<String>) grid
                            .getSelectionModel();
                    singleSelect.setDeselectAllowed(false);
                });
        btnDisallowDeselect.setId("disallow-deselect-button");

        NativeButton addItemDetailsButton = new NativeButton("Add item details",
                e -> {
                    grid.setSelectionMode(Grid.SelectionMode.NONE);
                    grid.setItemDetailsRenderer(new ComponentRenderer<>(
                            item -> new Span("Item details")));
                });
        addItemDetailsButton.setId("add-item-details-button");

        NativeButton toggleDetailsVisibleOnClick = new NativeButton(
                "Toggle details visible on click", e -> {
                    grid.setDetailsVisibleOnClick(
                            !grid.isDetailsVisibleOnClick());
                });
        toggleDetailsVisibleOnClick
                .setId("toggle-details-visible-click-button");

        NativeButton resetSortingButton = new NativeButton("Reset sorting",
                e -> {
                    grid.sort(null);
                });
        resetSortingButton.setId("reset-sorting-button");

        NativeButton selectAndDetachButton = new NativeButton(
                "Select and detach", e -> {
                    grid.select("A");
                    remove(grid);
                });
        selectAndDetachButton.setId("select-and-detach-button");

        NativeButton btnSelectionModeNone = new NativeButton(
                "Change to selection none",
                e -> grid.setSelectionMode(Grid.SelectionMode.NONE));
        btnSelectionModeNone.setId("selection-mode-none-button");

        NativeButton btnHideGrid = new NativeButton("Hide grid",
                e -> grid.setVisible(false));
        btnHideGrid.setId("hide-grid-button");

        NativeButton btnShowGrid = new NativeButton("Show grid",
                e -> grid.setVisible(true));
        btnShowGrid.setId("show-grid-button");

        NativeButton btnDetachAndReattach = new NativeButton(
                "Detach and reattach", e -> {
                    remove(grid);
                    add(grid);
                });
        btnDetachAndReattach.setId("detach-and-reattach-button");

        add(btnAttach, btnDetach, btnDisallowDeselect, addItemDetailsButton,
                toggleDetailsVisibleOnClick, resetSortingButton,
                selectAndDetachButton, btnHideGrid, btnSelectionModeNone,
                btnDetachAndReattach, btnShowGrid, grid);
    }
}
