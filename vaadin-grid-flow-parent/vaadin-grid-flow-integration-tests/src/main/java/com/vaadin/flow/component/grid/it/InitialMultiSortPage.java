/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

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
