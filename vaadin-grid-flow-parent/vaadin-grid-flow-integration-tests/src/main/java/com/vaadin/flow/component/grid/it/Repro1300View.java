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
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/1300.
 *
 * A grid configured with many columns after its initial render (e.g. from a
 * button click listener or ui.access from another thread) reportedly shows the
 * columns in incorrect order. A grid configured before the initial render is
 * the control.
 */
@Route("repro-1300")
public class Repro1300View extends Div {

    public Repro1300View() {
        Grid<String[]> grid1 = new Grid<>();
        grid1.getElement().setAttribute("id", "immediate-grid");
        Grid<String[]> grid2 = new Grid<>();
        grid2.getElement().setAttribute("id", "deferred-grid");

        add(new Span("Configured now"), grid1);
        add(new Span("Configured from button"), grid2);

        configureTable(grid1);

        Grid<String[]> grid3 = new Grid<>();
        grid3.getElement().setAttribute("id", "no-header-row-grid");
        add(new Span("Configured from button, no extra header row"), grid3);

        NativeButton configure = new NativeButton("Configure", event -> {
            configureTable(grid2);
            // same deferred configuration but without appendHeaderRow
            for (int i = 0; i < 50; i++) {
                grid3.addColumn(t -> "value").setHeader(i + " - Category");
            }
            grid3.setItems(new String[5]);
        });
        configure.setId("configure-button");
        add(configure);
    }

    private static void configureTable(Grid<String[]> grid) {
        for (int i = 0; i < 50; i++) {
            grid.addColumn(t -> "value").setHeader(i + " - Category");
        }

        List<HeaderRow.HeaderCell> cells = grid.appendHeaderRow().getCells();
        for (HeaderRow.HeaderCell cell : cells) {
            cell.setComponent(new Span("h1 col"));
        }

        grid.setItems(new String[5]);
    }
}
