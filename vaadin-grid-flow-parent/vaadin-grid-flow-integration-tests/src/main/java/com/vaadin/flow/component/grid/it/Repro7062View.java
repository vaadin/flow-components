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

import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

@Route("repro-7062")
public class Repro7062View extends Div {

    public record Item(String name) {
    }

    private static final List<Item> ITEMS = List.of(new Item("A"),
            new Item("B"), new Item("C"), new Item("D"), new Item("E"));

    public Repro7062View() {
        // Failing case: empty grid attached, columns added later (reporter)
        Grid<Item> lateGrid = new Grid<>();
        lateGrid.setId("late-grid");
        lateGrid.setHeight("300px");

        NativeButton addColumns = new NativeButton("add columns (late-grid)",
                e -> setUp(lateGrid));
        addColumns.setId("add-columns");

        // Control: same setup before the grid is attached
        Grid<Item> eagerGrid = new Grid<>();
        eagerGrid.setId("eager-grid");
        eagerGrid.setHeight("300px");
        setUp(eagerGrid);

        add(addColumns, lateGrid, eagerGrid);
    }

    private static void setUp(Grid<Item> grid) {
        if (!grid.getColumns().isEmpty()) {
            grid.removeAllColumns();
        }
        grid.addColumn(Item::name).setHeader("Name").setFlexGrow(1);
        grid.setItems(ITEMS);
        grid.setItemDetailsRenderer(
                new ComponentRenderer<>(item -> new Span(item.name())));
    }
}
