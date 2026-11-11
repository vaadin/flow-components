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

import java.util.stream.Collectors;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/10640
 */
@Route("repro-10640")
public class Repro10640View extends Div {

    private enum Variant {
        CONTROL, TOOLTIP_BEFORE_COLUMNS, TOOLTIP_AFTER_COLUMNS, MULTI_SELECT
    }

    public Repro10640View() {
        add(new Span(
                "Assertions enabled: " + Grid.class.desiredAssertionStatus()));
        for (Variant variant : Variant.values()) {
            addCase(variant);
        }
    }

    private void addCase(Variant variant) {
        String id = variant.name().toLowerCase().replace('_', '-');

        Grid<String> grid = new Grid<>();
        grid.setId(id + "-grid");
        grid.setAllRowsVisible(true);

        if (variant == Variant.TOOLTIP_BEFORE_COLUMNS) {
            grid.setTooltipGenerator(item -> item);
        }
        if (variant == Variant.MULTI_SELECT) {
            grid.setSelectionMode(Grid.SelectionMode.MULTI);
        }

        Grid.Column<String> a = grid.addColumn(item -> "a" + item)
                .setHeader("a");
        Grid.Column<String> b = grid.addColumn(item -> "b" + item)
                .setHeader("b");
        Grid.Column<String> c = grid.addColumn(item -> "c" + item)
                .setHeader("c");
        Grid.Column<String> d = grid.addColumn(item -> "d" + item)
                .setHeader("d");
        grid.setItems("1", "2");

        if (variant == Variant.TOOLTIP_AFTER_COLUMNS) {
            grid.setTooltipGenerator(item -> item);
        }

        HeaderRow row = grid.prependHeaderRow();
        row.getCell(a).setText("A");
        row.getCell(b).setText("B");
        row.getCell(c).setText("C");
        row.getCell(d).setText("D");

        Span status = new Span("not joined: " + texts(row));
        status.setId(id + "-status");

        // Joins after the grid is attached
        NativeButton join = new NativeButton("join(b, c)", e -> {
            try {
                row.join(b, c).setText("B+C");
                status.setText("joined: " + texts(row));
            } catch (Throwable t) {
                status.setText("join threw " + t);
            }
        });
        join.setId(id + "-join");

        NativeButton reorder = new NativeButton("setColumnOrder(d, a, b, c)",
                e -> {
                    try {
                        grid.setColumnOrder(d, a, b, c);
                        status.setText("reordered: " + texts(row));
                    } catch (Throwable t) {
                        status.setText("reorder threw " + t + " at "
                                + t.getStackTrace()[0] + ", cells: "
                                + texts(row));
                    }
                });
        reorder.setId(id + "-reorder");

        // Joins before attach, the same as the issue snippet
        if (variant != Variant.MULTI_SELECT) {
            row.join(b, c).setText("B+C");
            status.setText("joined: " + texts(row));
            join.setEnabled(false);
        }

        add(new H3(variant.name()), status, new Div(join, reorder), grid);
    }

    private static String texts(HeaderRow row) {
        return row.getCells().size() + " cells "
                + row.getCells().stream().map(HeaderRow.HeaderCell::getText)
                        .collect(Collectors.joining(", ", "[", "]"));
    }
}
