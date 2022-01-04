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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.Route;

/**
 * Page created for testing purposes. Not suitable for demos.
 *
 * @author Vaadin Ltd.
 *
 */
@Route("vaadin-grid-test")
public class GridTestPage extends Div {

    private static class Item implements Serializable {
        private String name;
        private int number;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }
    }

    public GridTestPage() {
        createGridWithComponentRenderers();
        createGridWithTemplateDetailsRow();
        createGridWithComponentDetailsRow();
        createGridWithRemovableColumns();
        createDetachableGrid();
    }

    private void createGridWithComponentRenderers() {
        Grid<Item> grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        AtomicBoolean usingFirstList = new AtomicBoolean(true);

        List<Item> firstList = generateItems(20, 0);
        List<Item> secondList = generateItems(10, 20);

        grid.setItems(firstList);

        grid.addColumn(new ComponentRenderer<>(item -> {
            Label label = new Label(item.getName());
            label.setId("grid-with-component-renderers-item-name-"
                    + item.getNumber());
            return label;
        })).setKey("name").setHeader("Name");
        grid.addColumn(new ComponentRenderer<>(item -> {
            Label label = new Label(String.valueOf(item.getNumber()));
            label.setId("grid-with-component-renderers-item-number-"
                    + item.getNumber());
            return label;
        })).setKey("number").setHeader("Number");
        grid.addColumn(new ComponentRenderer<>(item -> {
            NativeButton remove = new NativeButton("Remove", evt -> {
                if (usingFirstList.get()) {
                    firstList.remove(item);
                } else {
                    secondList.remove(item);
                }
                grid.getDataProvider().refreshAll();
            });
            remove.setId(
                    "grid-with-component-renderers-remove-" + item.getNumber());
            return remove;
        })).setKey("remove");
        grid.addColumn(TemplateRenderer.<Item> of("hidden")).setHeader("hidden")
                .setKey("hidden").setVisible(false);

        grid.setId("grid-with-component-renderers");
        grid.setWidth("500px");
        grid.setHeight("500px");

        NativeButton changeList = new NativeButton("Change list", evt -> {
            if (usingFirstList.get()) {
                grid.setItems(secondList);
            } else {
                grid.setItems(firstList);
            }
            usingFirstList.set(!usingFirstList.get());
        });
        changeList.setId("grid-with-component-renderers-change-list");
        NativeButton toggleColumnOrdering = new NativeButton(
                "Toggle column ordering", evt -> {
                    grid.setColumnReorderingAllowed(
                            !grid.isColumnReorderingAllowed());
                });
        toggleColumnOrdering.setId("toggle-column-ordering");

        NativeButton setReorderListener = new NativeButton(
                "Set reorder listener", evt -> {
                    grid.addColumnReorderListener(e -> {
                        if (e.isFromClient()) {
                            List<Column<Item>> columnList = e.getColumns()
                                    .stream().collect(Collectors.toList());
                            // Reorder columns in the list
                            Collections.swap(columnList, 1, 2);
                            grid.setColumnOrder(columnList);
                        }
                    });
                });
        setReorderListener.setId("set-reorder-listener");

        Span currentColumnOrdering = new Span();
        currentColumnOrdering.setId("current-column-ordering");
        grid.addColumnReorderListener(e -> currentColumnOrdering
                .setText(e.getColumns().stream().map(Column::getKey)
                        .collect(Collectors.joining(", "))));

        add(grid, changeList, toggleColumnOrdering, setReorderListener,
                currentColumnOrdering);
    }

    private void createGridWithTemplateDetailsRow() {
        Grid<Item> grid = new Grid<>();

        grid.setItems(generateItems(20, 0));

        grid.addColumn(Item::getName);
        grid.setItemDetailsRenderer(
                TemplateRenderer.<Item> of("[[item.detailsProperty]]")
                        .withProperty("detailsProperty",
                                item -> "Details opened! " + item.getNumber()));

        grid.setId("grid-with-template-details-row");
        grid.setWidth("500px");
        grid.setHeight("500px");
        add(grid);
    }

    private void createGridWithComponentDetailsRow() {
        Grid<Item> grid = new Grid<>();

        grid.setItems(generateItems(20, 0));

        grid.addColumn(Item::getName);
        grid.setItemDetailsRenderer(new ComponentRenderer<>(
                item -> new Label("Details opened! " + item.getNumber())));

        grid.setId("grid-with-component-details-row");
        grid.setWidth("500px");
        grid.setHeight("500px");
        add(grid);
    }

    private void createGridWithRemovableColumns() {
        Grid<Item> grid = new Grid<>();

        grid.setItems(generateItems(20, 0));

        Column<Item> removedColumn = grid.addColumn(Item::getName);
        Column<Item> nameColumn = grid.addColumn(Item::getName);
        grid.addColumn(Item::getNumber);

        grid.removeColumn(removedColumn);

        NativeButton removeNameColumn = new NativeButton("Remove name column",
                evt -> {
                    grid.removeColumn(nameColumn);
                    // forces refresh of the data
                    grid.getDataCommunicator().reset();
                });

        grid.setId("grid-with-removable-columns");
        grid.setWidth("500px");
        grid.setHeight("500px");
        removeNameColumn.setId("remove-name-column-button");
        add(grid);
        add(removeNameColumn);
    }

    private void createDetachableGrid() {
        Div container1 = new Div(new Label("Container 1"));
        container1.setId("detachable-grid-container-1");
        Div container2 = new Div(new Label("Container 2"));
        container2.setId("detachable-grid-container-2");

        Grid<Item> grid = new Grid<>();
        grid.setId("detachable-grid");

        grid.setItems(generateItems(200, 0));
        grid.addColumn(Item::getName);
        container1.add(grid);
        add(container1);
        NativeButton detach = new NativeButton("Detach grid",
                e -> grid.getParent().ifPresent(
                        parent -> ((HasComponents) parent).remove(grid)));
        detach.setId("detachable-grid-detach");
        NativeButton attach1 = new NativeButton("Attach grid to container 1",
                e -> container1.add(grid));
        attach1.setId("detachable-grid-attach-1");
        NativeButton attach2 = new NativeButton("Attach grid to container 2",
                e -> container2.add(grid));
        attach2.setId("detachable-grid-attach-2");
        NativeButton invisible = new NativeButton("Set grid invisble",
                e -> grid.setVisible(false));
        invisible.setId("detachable-grid-invisible");
        NativeButton visible = new NativeButton("Set grid visible",
                e -> grid.setVisible(true));
        visible.setId("detachable-grid-visible");
        add(container1, container2, detach, attach1, attach2, invisible,
                visible);
    }

    private List<Item> generateItems(int amount, int startingIndex) {
        List<Item> list = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            Item item = new Item();
            item.setName("Item " + (i + startingIndex));
            item.setNumber(i + startingIndex);
            list.add(item);
        }
        return list;
    }

}
