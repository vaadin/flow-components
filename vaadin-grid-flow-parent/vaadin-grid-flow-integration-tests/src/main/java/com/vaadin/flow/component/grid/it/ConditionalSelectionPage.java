/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/conditional-selection")
public class ConditionalSelectionPage extends Div {
    private final Span selectedItems;

    public ConditionalSelectionPage() {
        Grid<Integer> grid = new Grid<>();
        grid.setItems(IntStream.range(0, 10).boxed().toList());
        grid.addColumn(i -> i).setHeader("Item");

        selectedItems = new Span();
        selectedItems.setId("selected-items");

        NativeButton enableSingleSelect = new NativeButton(
                "Enable single selection", e -> {
                    grid.setSelectionMode(Grid.SelectionMode.SINGLE);
                    grid.addSelectionListener(this::updateSelection);
                });
        enableSingleSelect.setId("enable-single-selection");

        NativeButton enableMultiSelect = new NativeButton(
                "Enable multi selection", e -> {
                    grid.setSelectionMode(Grid.SelectionMode.MULTI);
                    grid.addSelectionListener(this::updateSelection);
                });
        enableMultiSelect.setId("enable-multi-selection");

        NativeButton disableSelectionFirstFive = new NativeButton(
                "Disable selection for first five items", e -> {
                    grid.setItemSelectableProvider(item -> item >= 5);
                });
        disableSelectionFirstFive.setId("disable-selection-first-five");

        NativeButton allowSelectionFirstFive = new NativeButton(
                "Allow selection for first five items", e -> {
                    grid.setItemSelectableProvider(item -> item < 5);
                });
        allowSelectionFirstFive.setId("allow-selection-first-five");

        add(grid);
        add(new Div(enableSingleSelect, enableMultiSelect));
        add(new Div(disableSelectionFirstFive, allowSelectionFirstFive));
        add(new Div(new Span("Selected items: "), selectedItems));
    }

    private void updateSelection(
            SelectionEvent<Grid<Integer>, Integer> selectionEvent) {
        String items = selectionEvent.getAllSelectedItems().stream()
                .map(Object::toString).collect(Collectors.joining(","));
        selectedItems.setText(items);
    }
}
