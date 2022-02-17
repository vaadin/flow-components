/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/drag-and-drop")
@JavaScript("DragAndDropHelpers.js")
public class DragAndDropGridPage extends Div {

    private List<String> items = IntStream.range(0, 5).mapToObj(String::valueOf)
            .collect(Collectors.toList());
    private Grid<String> grid = new Grid<>();

    public DragAndDropGridPage() {

        grid.addColumn(item -> item).setHeader("Header");
        grid.setItems(items);
        grid.setId("grid");
        grid.setSelectionMode(SelectionMode.MULTI);

        grid.setRowsDraggable(true);

        add(grid);

        addListeners();
        createControls();
    }

    private void addListeners() {

        Div startMessage = new Div();
        startMessage.setId("start-message");
        grid.addDragStartListener(e -> startMessage.add(
                e.getDraggedItems().stream().collect(Collectors.joining(","))));

        Div endMessage = new Div();
        endMessage.setId("end-message");
        grid.addDragEndListener(
                e -> endMessage.add(e.getSource().getId().get()));

        Div dropMessage = new Div();
        dropMessage.setId("drop-message");

        Div dropDataTextMessage = new Div();
        dropDataTextMessage.setId("drop-data-text-message");

        Div dropDataHtmlMessage = new Div();
        dropDataHtmlMessage.setId("drop-data-html-message");
        grid.addDropListener(e -> {
            dropMessage.add(e.getSource().getId().get() + " "
                    + e.getDropLocation().toString() + " "
                    + e.getDropTargetItem().orElse(null));

            dropDataTextMessage
                    .add(e.getDataTransferText().replaceAll("\n", "-"));

            String htmlData = e.getDataTransferData().get("text/html");
            if (htmlData != null) {
                dropDataHtmlMessage.add(new Html(htmlData));
            }
        });

        add(startMessage, endMessage, dropMessage, dropDataTextMessage,
                dropDataHtmlMessage);
    }

    private void createControls() {

        NativeButton toggleRowsDraggable = new NativeButton(
                "Toggle rowsDraggable",
                e -> grid.setRowsDraggable(!grid.isRowsDraggable()));
        toggleRowsDraggable.setId("toggle-rows-draggable");
        add(toggleRowsDraggable);

        Arrays.stream(GridDropMode.values()).forEach(mode -> {
            NativeButton button = new NativeButton(mode.toString(),
                    e -> grid.setDropMode(mode));
            button.setId(mode.toString());
            add(button);
        });

        NativeButton setGeneratorsButton = new NativeButton("set generators",
                e -> {
                    grid.setDragDataGenerator("text", item -> item + " foo");
                    grid.setDragDataGenerator("text/html",
                            item -> "<b>" + item + "</b>");
                });
        setGeneratorsButton.setId("set-generators");
        add(setGeneratorsButton);

        NativeButton setSelectionDragDataButton = new NativeButton(
                "set selection drag data", e -> {
                    Map<String, String> dragData = new HashMap<>();
                    dragData.put("text", "selection-drag-data");
                    grid.setSelectionDragDetails(-1, dragData);
                });
        setSelectionDragDataButton.setId("set-selection-drag-data");
        add(setSelectionDragDataButton);

        NativeButton setFiltersButton = new NativeButton("set filters", e -> {
            grid.setDragFilter(item -> "1".equals(item));
            grid.setDropFilter(item -> "2".equals(item));
        });
        setFiltersButton.setId("set-filters");
        add(setFiltersButton);

        NativeButton multiSelectButton = new NativeButton("multiselect", e -> {
            grid.select("0");
            grid.select("1");
        });
        multiSelectButton.setId("multiselect");
        add(multiSelectButton);
    }

}
