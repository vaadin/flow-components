/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.tests;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.renderer.ComponentTemplateRenderer;
import com.vaadin.flow.renderer.TemplateRenderer;
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
    }

    private void createGridWithComponentRenderers() {
        Grid<Item> grid = new Grid<>();

        AtomicBoolean usingFirstList = new AtomicBoolean(true);

        List<Item> firstList = generateItems(20, 0);
        List<Item> secondList = generateItems(10, 20);

        grid.setItems(firstList);

        grid.addColumn(new ComponentTemplateRenderer<Label, Item>(item -> {
            Label label = new Label(item.getName());
            label.setId("grid-with-component-renderers-item-name-"
                    + item.getNumber());
            return label;
        }).withProperty("id", item -> "grid-with-component-renderers-item-name-"
                + item.getNumber()));
        grid.addColumn(new ComponentTemplateRenderer<>(item -> {
            Label label = new Label(String.valueOf(item.getNumber()));
            label.setId("grid-with-component-renderers-item-number-"
                    + item.getNumber());
            return label;
        }));
        grid.addColumn(new ComponentTemplateRenderer<>(item -> {
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
        }));

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
        add(grid, changeList);
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
        grid.setItemDetailsRenderer(new ComponentTemplateRenderer<>(
                item -> new Label("Details opened! " + item.getNumber())));

        grid.setId("grid-with-component-details-row");
        grid.setWidth("500px");
        grid.setHeight("500px");
        add(grid);
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
