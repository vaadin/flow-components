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
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/1201.
 *
 * After grid.removeColumn() on a sorted column, the client-side sorter state
 * is reportedly not cleared, so a later sortersChanged call from the client
 * references the removed column and the server throws IllegalArgumentException
 * "Received a sorters changed call from the client for a non-existent column"
 * (especially with multi-sort).
 */
@Route("repro-1201")
public class Repro1201View extends Div {

    public Repro1201View() {
        Span status = new Span("status: idle");
        status.setId("status");

        Grid<String> grid = new Grid<>();
        grid.setMultiSort(true);
        grid.setItems("row-1", "row-2", "row-3");

        Column<String> colA = grid.addColumn(s -> s + "-A").setHeader("A")
                .setSortable(true).setKey("A");
        Column<String> colB = grid.addColumn(s -> s + "-B").setHeader("B")
                .setSortable(true).setKey("B");
        grid.addColumn(s -> s + "-C").setHeader("C").setSortable(true)
                .setKey("C");

        NativeButton removeA = new NativeButton("remove column A", e -> {
            try {
                grid.removeColumn(colA);
                status.setText("status: removed A ok");
            } catch (RuntimeException ex) {
                status.setText("status: EXCEPTION on remove "
                        + ex.getClass().getSimpleName() + ": "
                        + ex.getMessage());
            }
        });
        removeA.setId("remove-a");

        NativeButton removeAll = new NativeButton("remove all columns", e -> {
            try {
                grid.removeAllColumns();
                status.setText("status: removed all ok");
            } catch (RuntimeException ex) {
                status.setText("status: EXCEPTION on removeAll "
                        + ex.getClass().getSimpleName() + ": "
                        + ex.getMessage());
            }
        });
        removeAll.setId("remove-all");

        add(grid, removeA, removeAll, status);
    }
}
