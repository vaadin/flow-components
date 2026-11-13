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
import java.util.stream.Collectors;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

/**
 * Query parameters: columns (default 30), frozen (default 3), rows (default 3).
 */
@Route("repro-6791")
public class Repro6791View extends Div implements BeforeEnterObserver {

    private int rows = 3;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var params = event.getLocation().getQueryParameters();
        int columns = Integer
                .parseInt(params.getSingleParameter("columns").orElse("30"));
        int frozen = Integer
                .parseInt(params.getSingleParameter("frozen").orElse("3"));
        rows = Integer.parseInt(params.getSingleParameter("rows").orElse("3"));

        removeAll();
        Grid<String> grid = new Grid<>();
        grid.setId("grid");
        for (int j = 0; j < columns; j++) {
            int i = j;
            Column<String> c = grid
                    .addColumn(item -> "%s (%s)".formatted(item, i));
            c.setKey("c" + (i + 1));
            if (i < frozen) {
                c.setFrozen(true);
            }
        }
        updateHeaders(grid);
        grid.setItems(List.of("A", "B", "C", "D", "E", "F"));

        var log = new Pre();
        log.setId("log");

        var update = new NativeButton("Update Headers",
                e -> updateHeaders(grid));
        update.setId("update");

        var removeAll = new NativeButton("Remove all header rows",
                e -> grid.removeAllHeaderRows());
        removeAll.setId("remove-all");

        var read = new NativeButton("Read server order",
                e -> log.setText("getColumns: "
                        + grid.getColumns().stream().map(Column::getKey)
                                .collect(Collectors.joining(","))
                        + "\nDOM leaves: " + leafKeys(grid.getElement())));
        read.setId("read");

        add(new Div(update, removeAll, read), grid, log);
    }

    private static String leafKeys(Element element) {
        return element.getChildren().map(child -> {
            if ("vaadin-grid-column".equals(child.getTag())) {
                return child.getComponent().map(c -> ((Column<?>) c).getKey())
                        .orElse("?");
            }
            if ("vaadin-grid-column-group".equals(child.getTag())) {
                return leafKeys(child);
            }
            return null;
        }).filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(","));
    }

    private void updateHeaders(Grid<String> grid) {
        grid.removeAllHeaderRows();
        for (int i = 0; i < rows; i++) {
            final HeaderRow headerRow = grid.appendHeaderRow();
            for (Column<String> c : grid.getColumns()) {
                int index = grid.getColumns().indexOf(c);
                headerRow.getCell(c)
                        .setText("C%s, H%s".formatted(index + 1, i + 1));
            }
        }
    }
}
