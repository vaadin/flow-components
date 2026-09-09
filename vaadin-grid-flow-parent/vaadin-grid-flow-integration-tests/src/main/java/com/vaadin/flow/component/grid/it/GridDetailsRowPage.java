/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.bean.PeopleGenerator;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/grid-details-row")
public class GridDetailsRowPage extends Div {

    private List<DetailItem> items = new ArrayList<>();
    private Grid<DetailItem> grid = new Grid<>();

    private static record DetailItem(int id, String name) {
    }

    public GridDetailsRowPage() {
        items.add(new DetailItem(0, "Person 0"));
        items.add(new DetailItem(1, "Person 1"));
        items.add(new DetailItem(2, "Person 2"));

        ListDataProvider<DetailItem> dataProvider = new ListDataProvider<>(
                items) {
            @Override
            public Object getId(DetailItem person) {
                return person.id();
            };
        };
        grid.setItems(dataProvider);
        grid.setSelectionMode(SelectionMode.NONE);
        grid.addColumn(person -> person.name()).setHeader("Name");
        grid.setItemDetailsRenderer(
                new ComponentRenderer<>(person -> new Button(person.name())));

        NativeButton updatePerson2 = new NativeButton("Update person 2", e -> {
            DetailItem updatedPerson = new DetailItem(2, "Updated Person 2");
            items.set(2, updatedPerson);
            grid.getDataProvider().refreshItem(updatedPerson);
        });
        updatePerson2.setId("update-person-2");

        NativeButton removePerson2 = new NativeButton("Remove person 2", e -> {
            items.remove(2);
            grid.getDataProvider().refreshAll();
        });
        removePerson2.setId("remove-person-2");

        setFirstAndSecondItemsVisible();

        NativeButton openDetails = new NativeButton("Open details", e -> {
            setFirstAndSecondItemsVisible();
        });

        add(grid, openDetails, updatePerson2, removePerson2);

        createItemDetails();
        createItemDetailsOpenedProgrammatically();
    }

    public void setFirstAndSecondItemsVisible() {
        grid.setDetailsVisible(items.get(0), true);
        grid.setDetailsVisible(items.get(1), true);
    }

    private Grid<Person> createGridWithDetails() {
        Grid<Person> detailsGrid = new Grid<>();
        detailsGrid.setItems(new PeopleGenerator().generatePeople(500));

        detailsGrid.addColumn(Person::getFirstName).setHeader("Name");
        detailsGrid.addColumn(Person::getAge).setHeader("Age");

        // Any renderer can be used for the item details. By default, the
        // details are opened and closed by clicking the rows.
        detailsGrid.setItemDetailsRenderer(LitRenderer.<Person> of(
                "<div class='custom-details' style='border: 1px solid gray; padding: 10px; width: 100%; box-sizing: border-box;'>"
                        + "<div>Hi! My name is <b>${item.firstName}!</b></div>"
                        + "<div><button @click=${handleClick}>Update Person</button></div>"
                        + "</div>")
                .withProperty("firstName", Person::getFirstName)
                .withFunction("handleClick", person -> {
                    person.setFirstName(person.getFirstName() + " Updated");
                    detailsGrid.getDataProvider().refreshItem(person);
                }));
        return detailsGrid;
    }

    private void createItemDetails() {
        Grid<Person> detailsGrid = createGridWithDetails();
        detailsGrid.setId("grid-with-details-row");
        add(detailsGrid);
    }

    private void createItemDetailsOpenedProgrammatically() {
        Grid<Person> detailsGrid = createGridWithDetails();

        // Disable the default way of opening item details:
        detailsGrid.setDetailsVisibleOnClick(false);

        detailsGrid.addColumn(new NativeButtonRenderer<>("Toggle details open",
                item -> detailsGrid.setDetailsVisible(item,
                        !detailsGrid.isDetailsVisible(item))));

        detailsGrid.setId("grid-with-details-row-2");
        add(detailsGrid);
    }
}
