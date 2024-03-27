/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/grid-drag-select")
public class GridDragSelectPage extends VerticalLayout {
    public GridDragSelectPage() {
        Grid<String> grid = new Grid<>();
        grid.setItems(IntStream.range(0, 100).mapToObj(Integer::toString)
                .collect(Collectors.toList()));
        grid.addColumn(i -> i).setHeader("text");
        grid.addColumn(i -> String.valueOf(i.length())).setHeader("length");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        Checkbox toggleDragSelect = new Checkbox("Drag select rows");
        toggleDragSelect.setId("toggle-drag-select");
        toggleDragSelect.setValue(false);
        toggleDragSelect.addValueChangeListener(
                e -> ((GridMultiSelectionModel<String>) grid
                        .getSelectionModel()).setDragSelect(e.getValue()));

        Span selectedItemsCount = new Span("Selected items: 0");
        selectedItemsCount.setId("selected-items-count");
        grid.addSelectionListener(e -> selectedItemsCount
                .setText("Selected items: " + e.getAllSelectedItems().size()));

        add(new H2("Grid with rows drag select support"), grid,
                toggleDragSelect, selectedItemsCount);
    }
}
