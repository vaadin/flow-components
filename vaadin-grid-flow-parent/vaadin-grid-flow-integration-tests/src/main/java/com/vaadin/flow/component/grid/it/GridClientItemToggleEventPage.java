/*
 * Copyright 2000-2025 Vaadin Ltd.
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
