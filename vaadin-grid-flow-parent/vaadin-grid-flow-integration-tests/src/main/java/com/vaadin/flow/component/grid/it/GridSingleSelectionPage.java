/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.stream.IntStream;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSingleSelectionModel;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * Test view for variants of grid single selection mode.
 */
@Route("vaadin-grid/grid-single-selection")
public class GridSingleSelectionPage extends VerticalLayout {

    public static final String DESELECT_ALLOWED_GRID_ID = "deselect-allowed-grid";
    public static final String DESELECT_ALLOWED_TOGGLE_ID = "deselect-allowed-toggle";
    public static final String DESELECT_DISALLOWED_GRID_ID = "deselect-disallowed-grid";
    public static final String DESELECT_DISALLOWED_TOGGLE_ID = "deselect-disallowed-toggle";
    public static final String SET_ITEMS = "set-item-toggle";
    public static final String ITEMS_GRID = "items-grid";

    public GridSingleSelectionPage() {
        setSizeUndefined();
        setWidthFull();

        Grid<String> deselectAllowedGrid = buildGrid(true,
                DESELECT_ALLOWED_GRID_ID);
        NativeButton firstToggle = buildToggle(deselectAllowedGrid,
                DESELECT_ALLOWED_TOGGLE_ID);

        Grid<String> deselectDisallowedGrid = buildGrid(false,
                DESELECT_DISALLOWED_GRID_ID);
        NativeButton secondToggle = buildToggle(deselectDisallowedGrid,
                DESELECT_DISALLOWED_TOGGLE_ID);

        Grid<String> thirdGrid = setDeselectAllowedAndSetItems();

        add(deselectAllowedGrid, firstToggle, deselectDisallowedGrid,
                secondToggle, thirdGrid);

        NativeButton thirdToggle = toggleSetItems(thirdGrid, SET_ITEMS);
        add(thirdToggle);
    }

    @SuppressWarnings("rawtypes")
    private Grid<String> buildGrid(boolean deselectAllowed, String id) {
        Grid<String> grid = new Grid<>();
        grid.addColumn(string -> String.valueOf(Math.random()))
                .setHeader("column 1");
        grid.setItems(IntStream.rangeClosed(1, 3).mapToObj(String::valueOf));
        grid.setAllRowsVisible(true);
        if (!deselectAllowed) {
            ((GridSingleSelectionModel) grid.getSelectionModel())
                    .setDeselectAllowed(deselectAllowed);
        }
        grid.setId(id);
        return grid;
    }

    @SuppressWarnings("rawtypes")
    private Grid<String> setItemsGrid(Grid grid, String id) {
        grid.addColumn(string -> String.valueOf(Math.random()))
                .setHeader("column 1");
        grid.setItems(IntStream.rangeClosed(1, 3).mapToObj(String::valueOf));
        grid.setAllRowsVisible(true);
        grid.setId(id);
        return grid;
    }

    @SuppressWarnings("rawtypes")
    private NativeButton buildToggle(Grid grid, String id) {
        NativeButton button = new NativeButton("Toggle deselectAllowed", e -> {
            GridSingleSelectionModel gssm = (GridSingleSelectionModel) grid
                    .getSelectionModel();
            gssm.setDeselectAllowed(!gssm.isDeselectAllowed());
        });
        button.setId(id);
        return button;
    }

    private Grid<String> setDeselectAllowedAndSetItems() {
        Grid<String> grid = new Grid<>();

        GridSingleSelectionModel gssm = (GridSingleSelectionModel) grid
                .getSelectionModel();
        // Set deselectAllowed to false
        gssm.setDeselectAllowed(false);
        // Set Items for grid
        grid = setItemsGrid(grid, ITEMS_GRID);
        Button text = new Button();
        grid.addSelectionListener(e -> {
            if (e.getFirstSelectedItem().isPresent()) {
                text.setId("item" + e.getFirstSelectedItem().get());
                text.setText("The row " + e.getFirstSelectedItem().get()
                        + " is selected");
                add(text);
            }
        });
        return grid;
    }

    private NativeButton toggleSetItems(Grid grid, String idSetItems) {
        NativeButton buttonSetItems = new NativeButton("Toggle set items",
                e -> {
                    if (!grid.getColumns().isEmpty()) {
                        grid.removeAllColumns();
                    }
                    setItemsGrid(grid, ITEMS_GRID);
                });
        buttonSetItems.setId(idSetItems);
        return buttonSetItems;
    }

}
