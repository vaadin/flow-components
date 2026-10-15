/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.List;
import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

import elemental.json.Json;
import elemental.json.JsonObject;

@Route("vaadin-grid/grid-client-item-toggle-event")
public class GridClientItemToggleEventPage extends Div {
    public GridClientItemToggleEventPage() {
        Grid<String> grid = new Grid<>();
        grid.addColumn(s -> s);
        grid.setItems(createItems(0, 1000));
        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        Div eventLog = new Div();
        eventLog.setId("event-log");

        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .addClientItemToggleListener(event -> {
                    JsonObject record = Json.createObject();
                    record.put("isFromClient", event.isFromClient());
                    record.put("item", event.getItem());
                    record.put("isSelected", event.isSelected());
                    record.put("isShiftKey", event.isShiftKey());
                    eventLog.add(new Div(record.toString()));
                });

        NativeButton clearEventLog = new NativeButton("Clear event log",
                event -> {
                    eventLog.removeAll();
                });
        clearEventLog.setId("clear-event-log");

        add(grid, eventLog, clearEventLog);
    }

    private List<String> createItems(int start, int end) {
        return IntStream.range(start, end).mapToObj((i) -> "Item " + i)
                .toList();
    }
}
