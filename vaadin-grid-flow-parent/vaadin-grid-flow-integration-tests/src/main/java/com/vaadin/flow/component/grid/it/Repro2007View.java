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
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

/**
 * Repro for https://github.com/vaadin/flow-components/issues/2007 — switching
 * between two grids in one container changes the scroll position of the grid
 * that is switched back to.
 */
@Route("repro-2007")
public class Repro2007View extends Div {

    public Repro2007View() {
        add(createRemoveAddCase());
        add(createVisibilityCase());
    }

    /** Reporter's case: the grids are removed from / added to the container. */
    private Div createRemoveAddCase() {
        Div container = new Div();
        container.setId("remove-add-container");
        container.getStyle().set("height", "300px");

        Grid<String> grid1 = createGrid("remove-add-grid-1");
        Grid<String> grid2 = createGrid("remove-add-grid-2");
        container.add(grid1);

        NativeButton show1 = new NativeButton("remove/add: show grid 1", e -> {
            container.removeAll();
            container.add(grid1);
        });
        show1.setId("remove-add-show-1");

        NativeButton show2 = new NativeButton("remove/add: show grid 2", e -> {
            container.removeAll();
            container.add(grid2);
        });
        show2.setId("remove-add-show-2");

        return new Div(new Div(show1, show2), container);
    }

    /** Second case from the comments: the grids are toggled with setVisible. */
    private Div createVisibilityCase() {
        Div container = new Div();
        container.setId("visibility-container");
        container.getStyle().set("height", "300px");

        Grid<String> grid1 = createGrid("visibility-grid-1");
        Grid<String> grid2 = createGrid("visibility-grid-2");
        grid2.setVisible(false);
        container.add(grid1, grid2);

        NativeButton show1 = new NativeButton("visible: show grid 1", e -> {
            grid1.setVisible(true);
            grid2.setVisible(false);
        });
        show1.setId("visibility-show-1");

        NativeButton show2 = new NativeButton("visible: show grid 2", e -> {
            grid1.setVisible(false);
            grid2.setVisible(true);
        });
        show2.setId("visibility-show-2");

        return new Div(new Div(show1, show2), container);
    }

    private Grid<String> createGrid(String id) {
        List<String> items = new ArrayList<>();
        for (int i = 1; i < 1000; i++) {
            items.add("Row " + i);
        }

        Grid<String> grid = new Grid<>();
        grid.setId(id);
        grid.addColumn(item -> item).setHeader("Data");
        grid.setHeight("300px");
        grid.setItems(items);
        return grid;
    }
}
