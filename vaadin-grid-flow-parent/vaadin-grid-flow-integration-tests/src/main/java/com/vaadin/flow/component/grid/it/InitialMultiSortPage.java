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

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/initial-multi-sort")
public class InitialMultiSortPage extends Div {
    int clientSortEventCount = 0;
    Span clientSortEventCountSpan = new Span();

    public InitialMultiSortPage() {
        Grid<String> grid = new Grid<>();

        Grid.Column<String> firstColumn = grid.addColumn(value -> "A" + value)
                .setSortable(true).setHeader("A");
        Grid.Column<String> secondColumn = grid.addColumn(value -> "B" + value)
                .setSortable(true).setHeader("B");

        List<GridSortOrder<String>> sorts = new ArrayList<>();
        sorts.add(new GridSortOrder<>(firstColumn, SortDirection.DESCENDING));
        sorts.add(new GridSortOrder<>(secondColumn, SortDirection.ASCENDING));

        grid.setMultiSort(true);
        grid.sort(sorts);
        add(grid);

        add(clientSortEventCountSpan);
        clientSortEventCountSpan.setId("client-sort-event-count");
        updateEventCountSpan();
        grid.addSortListener(e -> {
            if (e.isFromClient()) {
                clientSortEventCount++;
                updateEventCountSpan();
            }
        });
    }

    private void updateEventCountSpan() {
        clientSortEventCountSpan
                .setText("Client sort events: " + clientSortEventCount);
    }
}
