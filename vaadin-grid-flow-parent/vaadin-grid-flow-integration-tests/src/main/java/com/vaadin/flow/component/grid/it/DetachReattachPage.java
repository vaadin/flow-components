/*
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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

        Span errorMessage = new Span();
        errorMessage.setId("error-message");

        // Set error handler to show errors in the UI
        VaadinSession.getCurrent().setErrorHandler(event -> {
            errorMessage.setText("Error: " + event.getThrowable().getMessage());
        });

        add(btnAttach, btnDetach, btnDisallowDeselect, addItemDetailsButton,
                toggleDetailsVisibleOnClick, resetSortingButton, btnHideGrid,
                btnSelectionModeNone, btnDetachAndReattach, btnShowGrid, grid,
                errorMessage);
    }
}
