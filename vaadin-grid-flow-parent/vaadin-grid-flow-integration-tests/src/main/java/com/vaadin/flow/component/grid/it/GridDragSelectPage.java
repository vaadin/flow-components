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
