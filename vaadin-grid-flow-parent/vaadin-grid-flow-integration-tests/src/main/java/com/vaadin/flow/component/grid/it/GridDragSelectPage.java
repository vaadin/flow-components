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

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * Test view for grid's select rows by dragging feature.
 */
@Route("vaadin-grid/grid-drag-select")
public class GridDragSelectPage extends VerticalLayout {

    public static final int ITEM_COUNT = 100;

    static final String DRAG_SELECT_GRID_ID = "drag-select-grid";
    static final String TOGGLE_DRAG_SELECT_CHECKBOX = "toggle-drag-select-checkbox";

    public GridDragSelectPage() {
        createGridMultiRowSelectionByDragging();
    }

    private void createGridMultiRowSelectionByDragging() {
        Grid<String> grid = new Grid<>();
        grid.setId(DRAG_SELECT_GRID_ID);
        grid.setItems(IntStream.range(0, ITEM_COUNT).mapToObj(Integer::toString)
                .collect(Collectors.toList()));
        grid.addColumn(i -> i).setHeader("text");
        grid.addColumn(i -> String.valueOf(i.length())).setHeader("length");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        Checkbox toggleDragSelect = new Checkbox(
                "Drag select rows");
        toggleDragSelect
                .setId(TOGGLE_DRAG_SELECT_CHECKBOX);
        toggleDragSelect.setValue(false);
        toggleDragSelect.addValueChangeListener(
                e -> ((GridMultiSelectionModel<String>) grid
                        .getSelectionModel()).setDragSelect(e.getValue()));

        add(new H2("Grid with rows drag select support"), grid,
                toggleDragSelect);
    }
}
