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
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.router.Route;

@Route("repro-6848")
public class Repro6848View extends Div {

    public Repro6848View() {
        // Failing case: unitless "0" width on a flex-grow column
        addGrid("grid-zero", "0");
        // Controls: "0px" and no width
        addGrid("grid-zero-px", "0px");
        addGrid("grid-no-width", null);
    }

    private void addGrid(String id, String zWidth) {
        Grid<String> grid = new Grid<>();
        grid.setId(id);
        grid.prependHeaderRow();
        HeaderRow groupHeaderRow = grid.prependHeaderRow();

        Grid.Column<String> z = grid.addColumn(s -> s).setHeader("Col Z")
                .setFlexGrow(1);
        if (zWidth != null) {
            z.setWidth(zWidth);
        }
        Grid.Column<String> a = grid.addColumn(s -> s).setHeader("Col A");
        Grid.Column<String> b = grid.addColumn(s -> s).setHeader("Col B");
        groupHeaderRow.join(a, b).setText("Join");

        grid.setItems(List.of("value", "value"));
        grid.setAllRowsVisible(true);
        grid.setWidth("800px");
        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);

        add(new H3(id + " (Z width: " + zWidth + ")"), grid);
    }
}
