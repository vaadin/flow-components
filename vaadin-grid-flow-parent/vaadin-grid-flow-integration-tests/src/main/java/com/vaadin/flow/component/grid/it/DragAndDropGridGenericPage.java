/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.dnd.DragEndEvent;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DragStartEvent;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/generic-dnd")
public class DragAndDropGridGenericPage extends Div {

    private List<String> items = IntStream.range(0, 10)
            .mapToObj(String::valueOf).collect(Collectors.toList());
    private Grid<String> grid = new Grid<>();
    private String draggedCard;
    private final Div dropbox;

    public DragAndDropGridGenericPage() {
        grid.addColumn(item -> item).setHeader("Header");
        grid.setItems(items);
        grid.setId("grid");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setRowsDraggable(true);
        grid.setDropMode(null);

        dropbox = new Div();
        dropbox.getStyle().set("display", "flex");
        dropbox.getStyle().set("border", "1px solid blue");
        dropbox.setWidth("300px");
        dropbox.setHeight("300px");
        DropTarget<Div> dropTarget = DropTarget.create(dropbox);
        dropTarget.addDropListener(event -> {
            Optional<Object> dragData = event.getDragData();
            dragData.filter(object -> object instanceof List)
                    .map(object -> ((List<String>) object))
                    .ifPresent(draggedItems -> {
                        draggedItems.stream().forEach(this::addCard);
                        items.removeAll(draggedItems);
                        grid.setItems(items);
                    });
        });

        add(grid, dropbox);

        addListeners();
        createControls();
    }

    private void addListeners() {
        grid.addDragStartListener(event -> {
            dropbox.getStyle().set("background-color", "lightgreen");
            DropTarget.configure(dropbox, true);
        });
        grid.addDragEndListener(event -> {
            dropbox.getStyle().remove("background-color");
            DropTarget.configure(dropbox, false);
        });

        grid.addDropListener(event -> {
            if (draggedCard != null) {
                items.add(draggedCard);
                grid.setItems(items);
                getUI().ifPresent(ui -> dropbox
                        .remove(ui.getActiveDragSourceComponent()));
            }
        });
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

    private void addCard(String card) {
        Div div = new Div();
        div.setText(card);
        div.getStyle().set("border", "1px solid red");
        div.setHeight("50px");
        div.setWidth("50px");
        DragSource<Div> dragSource = DragSource.create(div);
        dragSource.setDragData(card);
        dragSource.addDragStartListener(this::onCardDragStart);
        dragSource.addDragEndListener(this::onCardDragEnd);
        dropbox.add(div);
    }

    private void onCardDragEnd(DragEndEvent<Div> divDragEndEvent) {
        grid.setDropMode(null);
        draggedCard = null;
    }

    private void onCardDragStart(DragStartEvent<Div> event) {
        grid.setDropMode(GridDropMode.BETWEEN);
        draggedCard = event.getComponent().getText();
    }

}
