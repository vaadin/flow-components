/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.shared.SelectionPreservationMode;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/selection-preservation")
public class SelectionPreservationPage extends Div {

    public SelectionPreservationPage() {
        var items = new ArrayList<>(IntStream.range(0, 10).boxed().toList());
        var dataProvider = new ListDataProvider<>(items);

        var grid = new Grid<Integer>();
        grid.addColumn(String::valueOf).setHeader("Value");
        grid.setItems(dataProvider);
        grid.setAllRowsVisible(true);

        // Selection mode buttons
        var modeMultiSelect = new NativeButton("Multi select",
                e -> grid.setSelectionMode(Grid.SelectionMode.MULTI));
        modeMultiSelect.setId("mode-multi-select");

        var modeSingleSelect = new NativeButton("Single select",
                e -> grid.setSelectionMode(Grid.SelectionMode.SINGLE));
        modeSingleSelect.setId("mode-single-select");

        // Preservation mode buttons
        var modePreserveAll = new NativeButton("Preserve All",
                e -> grid.setSelectionPreservationMode(
                        SelectionPreservationMode.PRESERVE_ALL));
        modePreserveAll.setId("mode-preserve-all");

        var modePreserveExisting = new NativeButton("Preserve Existing",
                e -> grid.setSelectionPreservationMode(
                        SelectionPreservationMode.PRESERVE_EXISTING));
        modePreserveExisting.setId("mode-preserve-existing");

        var modeDiscard = new NativeButton("Discard",
                e -> grid.setSelectionPreservationMode(
                        SelectionPreservationMode.DISCARD));
        modeDiscard.setId("mode-discard");

        // Action buttons
        var refreshAll = new NativeButton("Refresh all",
                e -> dataProvider.refreshAll());
        refreshAll.setId("refresh-all");

        var removeItem5 = new NativeButton("Remove item 5",
                e -> items.remove(Integer.valueOf(5)));
        removeItem5.setId("remove-item-5");

        var serverValue = new Span();
        serverValue.setId("server-value");

        var showServerValue = new NativeButton("Show server value",
                e -> serverValue.setText(grid.getSelectedItems().stream()
                        .sorted().map(String::valueOf)
                        .collect(Collectors.joining(","))));
        showServerValue.setId("show-server-value");

        var selectionModeButtons = new Div(modeMultiSelect, modeSingleSelect);
        var preservationModeButtons = new Div(modePreserveAll,
                modePreserveExisting, modeDiscard);
        var actionButtons = new Div(refreshAll, removeItem5, showServerValue);

        add(grid, selectionModeButtons, preservationModeButtons, actionButtons,
                serverValue);
    }
}
