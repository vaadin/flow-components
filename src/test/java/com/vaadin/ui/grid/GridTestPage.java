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
package com.vaadin.ui.grid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.vaadin.router.Route;
import com.vaadin.ui.button.Button;
import com.vaadin.ui.html.Div;
import com.vaadin.ui.html.Label;
import com.vaadin.ui.renderers.ComponentTemplateRenderer;

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
    }

    private void createGridWithComponentRenderers() {
        Grid<Item> grid = new Grid<>();

        AtomicBoolean usingFirstList = new AtomicBoolean(true);

        List<Item> firstList = generateItems(20, 0);
        List<Item> secondList = generateItems(10, 20);

        grid.setItems(firstList);

        grid.addColumn(new ComponentTemplateRenderer<>(item -> {
            Label label = new Label(item.getName());
            label.setId("grid-with-component-renderers-item-name-"
                    + item.getNumber());
            return label;
        }));
        grid.addColumn(new ComponentTemplateRenderer<>(item -> {
            Label label = new Label(String.valueOf(item.getNumber()));
            label.setId("grid-with-component-renderers-item-number-"
                    + item.getNumber());
            return label;
        }));
        grid.addColumn(new ComponentTemplateRenderer<>(item -> {
            Button remove = new Button("Remove", evt -> {
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

        Button changeList = new Button("Change list", evt -> {
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
