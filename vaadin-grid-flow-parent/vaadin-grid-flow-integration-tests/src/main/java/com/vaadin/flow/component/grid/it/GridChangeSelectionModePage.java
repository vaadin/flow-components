/*
 * Copyright 2000-2024 Vaadin Ltd.
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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/change-selection-mode")
public class GridChangeSelectionModePage extends Div {
    public GridChangeSelectionModePage() {
        Grid<String> grid = new Grid<>();
        grid.setItems("A", "B", "C", "D");
        grid.addColumn(item -> item).setHeader("Item");

        NativeButton setSingleSelection = new NativeButton(
                "Set single selection", e -> {
                    grid.setSelectionMode(Grid.SelectionMode.SINGLE);
                });
        setSingleSelection.setId("set-single-selection");

        NativeButton setMultiSelection = new NativeButton("Set multi selection",
                e -> {
                    grid.setSelectionMode(Grid.SelectionMode.MULTI);
                });
        setMultiSelection.setId("set-multi-selection");

        NativeButton setNoneSelection = new NativeButton("Set none selection",
                e -> {
                    grid.setSelectionMode(Grid.SelectionMode.NONE);
                });
        setNoneSelection.setId("set-none-selection");

        add(grid, setSingleSelection, setMultiSelection, setNoneSelection);
    }
}
