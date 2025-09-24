/*
 * Copyright 2000-2025 Vaadin Ltd.
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
