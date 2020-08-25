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
package com.vaadin.flow.component.grid.it;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.bean.Address;
import com.vaadin.flow.data.bean.Gender;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Route("vaadin-grid/grid-list-data-view-page")
@Theme(Lumo.class)
public class GridListDataViewPage extends Div {

    public static final String ITEM_COUNT = "itemCount";
    public static final String ITEM_DATA = "itemData";
    public static final String ROW_SELECT = "rowSelect";
    public static final String SHOW_ITEM_DATA = "showItemData";
    public static final String SHOW_NEXT_DATA = "showNextData";
    public static final String SHOW_PREVIOUS_DATA = "showPreviousData";
    public static final String FIRST_NAME_FILTER = "firstNameFilter";
    public static final String ADD_ITEM = "addItem";
    public static final String UPDATE_ITEM = "updateItem";
    public static final String DELETE_ITEM = "deleteItem";

    public GridListDataViewPage() {
        List<Person> personList = generatePersonItems();
        Grid<Person> grid = new Grid<>(Person.class);
        final GridListDataView<Person> dataView = grid
                .setItems(personList);

        grid.removeColumnByKey("id");

        // The Grid<>(Person.class) sorts the properties and in order to
        // reorder the properties we use the 'setColumns' method.
        grid.setColumns("firstName", "lastName", "age");
        Span count = new Span(Integer.toString(dataView.getItemCount()));
        count.setId(ITEM_COUNT);
        Span itemData = new Span("Item: ");
        itemData.setId(ITEM_DATA);

        // Create data controls
        // For selecting a row to manipulate through the data view
        IntegerField rowSelect = new IntegerField("Target row");
        rowSelect.setId(ROW_SELECT);
        Button selectItemOnRow = new Button("Select item", event -> grid
                .select(dataView.getItem(rowSelect.getValue())));
        selectItemOnRow.setId("selectItemOnRow");
        Button showItemData = new Button("Person data", event -> itemData
                .setText("Item: " + dataView.getItem(rowSelect.getValue())
                        .getFirstName()));
        showItemData.setId(SHOW_ITEM_DATA);
        Button showNextData = new Button("Next person", event -> itemData
                .setText("Item: " + dataView.getNextItem(
                        dataView.getItem(rowSelect.getValue())).get()
                        .getFirstName()));
        showNextData.setId(SHOW_NEXT_DATA);
        Button showPreviousData = new Button("Previous person",
                event -> itemData.setText("Item: " + dataView.getPreviousItem(
                        dataView.getItem(rowSelect.getValue())).get()
                        .getFirstName()));
        showPreviousData.setId(SHOW_PREVIOUS_DATA);
        TextField filterByFirstName = new TextField("Firstname filter",
                event -> {
                    dataView.setFilter(
                            item -> item.getFirstName().toLowerCase()
                                    .contains(event.getValue().toLowerCase()));
                });
        filterByFirstName.setId(FIRST_NAME_FILTER);
        Button addNewPerson = new Button("Add new person",
                event -> {
                    Person newPerson = new Person("John", "Doe",
                            "john@test.com", 33, Gender.MALE, new Address());
                    dataView.addItem(newPerson);
                });
        addNewPerson.setId(ADD_ITEM);
        TextField updateFirstNameField = new TextField("Update first name",
                event -> {
                    Person updatedPerson =
                            dataView.getItem(rowSelect.getValue());
                    updatedPerson.setFirstName(event.getValue());
                    dataView.refreshItem(updatedPerson);
                });
        updateFirstNameField.setId(UPDATE_ITEM);
        Button deletePerson = new Button("Delete person",
                event -> {
                    Person deletedPerson =
                            dataView.getItem(rowSelect.getValue());
                    dataView.removeItem(deletedPerson);
                });
        deletePerson.setId(DELETE_ITEM);

        dataView.addItemCountChangeListener(event -> {
            count.setText(Integer.toString(event.getItemCount()));
            showPreviousData.setEnabled(rowSelect.getValue() > 0);
            showNextData.setEnabled(rowSelect.getValue() < event.getItemCount() - 1);
        });

        rowSelect.setValue(0);
        showPreviousData.setEnabled(false);
        rowSelect.addValueChangeListener(event -> {
            if (event.getValue() >= dataView.getItemCount()) {
                itemData.setText("Action: Item outside of data rage of [0," + (
                        dataView.getItemCount() - 1)
                        + "]. Resetting to previous");
                rowSelect.setValue(event.getOldValue());
                return;
            }
            showPreviousData.setEnabled(event.getValue() > 0);
            showNextData
                    .setEnabled(event.getValue() < dataView.getItemCount() - 1);
        });

        add(grid, rowSelect, filterByFirstName, selectItemOnRow, showItemData,
                showNextData, showPreviousData, count, itemData,
                addNewPerson, updateFirstNameField, deletePerson);
    }

    private List<Person> generatePersonItems() {
        return IntStream.range(1, 251).mapToObj(
                i -> new Person("Person " + i, "lastName",
                        "person" + i + "@test.com", (i % 90) + 15,
                        Gender.UNKNOWN, new Address()))
                .collect(Collectors.toList());
    }

}
