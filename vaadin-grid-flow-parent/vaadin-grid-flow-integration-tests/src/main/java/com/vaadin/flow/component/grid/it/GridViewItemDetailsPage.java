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

import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid-it-demo/item-details")
public class GridViewItemDetailsPage extends LegacyTestView {

    public GridViewItemDetailsPage() {
        createItemDetails();
        createItemDetailsOpenedProgrammatically();
    }

    private Grid<Person> createGridWithDetails() {
        Grid<Person> grid = new Grid<>();
        List<Person> people = createItems();
        grid.setItems(people);

        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        grid.setSelectionMode(SelectionMode.NONE);

        // You can use any renderer for the item details. By default, the
        // details are opened and closed by clicking the rows.
        grid.setItemDetailsRenderer(TemplateRenderer.<Person> of(
                "<div class='custom-details' style='border: 1px solid gray; padding: 10px; width: 100%; box-sizing: border-box;'>"
                        + "<div>Hi! My name is <b>[[item.firstName]]!</b></div>"
                        + "<div><button on-click='handleClick'>Update Person</button></div>"
                        + "</div>")
                .withProperty("firstName", Person::getFirstName)
                .withEventHandler("handleClick", person -> {
                    person.setFirstName(person.getFirstName() + " Updated");
                    grid.getDataProvider().refreshItem(person);
                }));
        return grid;
    }

    private void createItemDetails() {
        Grid<Person> grid = createGridWithDetails();
        grid.setId("grid-with-details-row");
        addCard("Item details", "Grid with item details", grid);
    }

    private void createItemDetailsOpenedProgrammatically() {
        Grid<Person> grid = createGridWithDetails();

        // Disable the default way of opening item details:
        grid.setDetailsVisibleOnClick(false);

        grid.addColumn(new NativeButtonRenderer<>("Toggle details open",
                item -> grid.setDetailsVisible(item,
                        !grid.isDetailsVisible(item))));

        grid.setId("grid-with-details-row-2");
        addCard("Item details", "Open details programmatically", grid);
    }

}
