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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/grid-details-row")
public class GridDetailsRowPage extends Div {

    private Grid<Person> grid = new Grid<>();
    private List<Person> items = new ArrayList<>();

    private int nbUpdates;
    private Person person3 = new Person("Person 3", 2);
    private Person person4 = new Person("Person 4", 1111);

    public GridDetailsRowPage() {

        items.add(new Person("Person 1", 99));
        items.add(new Person("Person 2", 1));
        items.add(person3);
        items.add(person4);

        ListDataProvider<Person> ldp = new ListDataProvider<>(items);
        grid.setDataProvider(ldp);
        grid.setSelectionMode(SelectionMode.NONE);
        grid.addColumn(Person::getFirstName).setHeader("name");
        grid.setItemDetailsRenderer(new ComponentRenderer<>(
                item -> new Button(item.getFirstName())));

        add(grid, new Button("click to open details",
                e -> setFirstAndSecondItemsVisible()));
        Button updatePerson3 = new Button("update and refresh person 3", e -> {
            nbUpdates++;
            person3.setFirstName("Person 3 - updates " + nbUpdates);
            grid.getDataProvider().refreshItem(person3);
        });
        updatePerson3.setId("update-button");
        add(updatePerson3);

        Button removeButton = new Button("remove person 4", e -> {
            items.remove(person4);
            grid.getDataProvider().refreshAll();
        });
        removeButton.setId("remove-button");
        add(removeButton);
        setFirstAndSecondItemsVisible();
    }

    public void setFirstAndSecondItemsVisible() {
        grid.setDetailsVisible(items.get(0), true);
        grid.setDetailsVisible(items.get(1), true);
    }
}
