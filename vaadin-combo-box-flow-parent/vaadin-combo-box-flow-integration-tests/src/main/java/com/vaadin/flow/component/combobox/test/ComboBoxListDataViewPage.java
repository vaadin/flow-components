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

package com.vaadin.flow.component.combobox.test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.dataview.ComboBoxListDataView;
import com.vaadin.flow.component.combobox.test.entity.Person;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/combobox-list-data-view-page")
public class ComboBoxListDataViewPage extends Div {

    static final String FIRST_COMBO_BOX_ID = "first-combo-box";
    static final String SECOND_COMBO_BOX_ID = "second-combo-box";
    static final String ITEM_COUNT = "itemCount";
    static final String ITEM_DATA = "itemData";
    static final String ITEM_SELECT = "itemSelect";
    static final String SHOW_ITEM_DATA = "showItemData";
    static final String SHOW_NEXT_DATA = "showNextData";
    static final String SHOW_PREVIOUS_DATA = "showPreviousData";
    static final String SHOW_ITEM_COUNT = "showItemCount";
    static final String SHOW_ITEMS = "showItems";
    static final String AGE_FILTER = "ageFilter";
    static final String REMOVE_ITEM = "removeItem";
    static final String REVERSE_SORTING = "reverseSorting";

    static final String NEW_PERSON_NAME = "Person NEW";

    public ComboBoxListDataViewPage() {
        List<Person> personList = generatePersonItems();
        final ListDataProvider<Person> dataProvider = DataProvider
                .ofCollection(personList);

        ComboBox<Person> comboBox = new ComboBox<>();
        comboBox.setAllowCustomValue(true);
        comboBox.setItemLabelGenerator(Person::getFirstName);
        comboBox.setId(FIRST_COMBO_BOX_ID);

        ComboBox<Person> secondComboBox = new ComboBox<>();
        secondComboBox.setId(SECOND_COMBO_BOX_ID);

        final ComboBoxListDataView<Person> dataView = comboBox
                .setItems(dataProvider);

        secondComboBox.setItems(dataProvider);

        // Add custom item to collection
        comboBox.addCustomValueSetListener(event -> {
            Person newPerson = new Person();
            newPerson.setFirstName(event.getDetail());
            // set ID outside the range of persons [0, 250)
            newPerson.setId(999);
            dataView.addItem(newPerson);
            comboBox.setValue(newPerson);
        });

        Span count = new Span(Integer.toString(dataView.getItemCount()));
        count.setId(ITEM_COUNT);
        Span itemData = new Span("Item: ");
        itemData.setId(ITEM_DATA);

        // Create data controls
        // For selecting an item to manipulate through the data view
        IntegerField itemSelect = new IntegerField("Target item");
        itemSelect.setId(ITEM_SELECT);
        NativeButton selectItemOnIndex = new NativeButton("Select item",
                event -> comboBox
                        .setValue(dataView.getItem(itemSelect.getValue())));
        selectItemOnIndex.setId("selectItemOnIndex");
        NativeButton showItemData = new NativeButton("Person data",
                event -> itemData.setText("Item: " + dataView
                        .getItem(itemSelect.getValue()).getFirstName()));
        showItemData.setId(SHOW_ITEM_DATA);

        NativeButton showItemCount = new NativeButton("Show Item Count",
                click -> count
                        .setText(String.valueOf(dataView.getItemCount())));
        showItemCount.setId(SHOW_ITEM_COUNT);

        NativeButton showItems = new NativeButton("Show Items",
                click -> itemData
                        .setText(dataView.getItems().map(Person::toString)
                                .collect(Collectors.joining(","))));
        showItems.setId(SHOW_ITEMS);

        // Navigation
        NativeButton showNextData = new NativeButton("Next person", event -> {
            itemData.setText("Item: " + dataView
                    .getNextItem(dataView.getItem(itemSelect.getValue())).get()
                    .getFirstName());
            itemSelect.setValue(itemSelect.getValue() + 1);
        });
        showNextData.setId(SHOW_NEXT_DATA);

        NativeButton showPreviousData = new NativeButton("Previous person",
                event -> {
                    itemData.setText("Item: " + dataView
                            .getPreviousItem(
                                    dataView.getItem(itemSelect.getValue()))
                            .get().getFirstName());
                    itemSelect.setValue(itemSelect.getValue() - 1);
                });

        // Remove item
        NativeButton removePerson = new NativeButton("Remove person", event -> {
            dataView.removeItem(comboBox.getValue());
            comboBox.setValue(null);
        });
        removePerson.setId(REMOVE_ITEM);
        showPreviousData.setId(SHOW_PREVIOUS_DATA);

        // Filter by Age
        IntegerField filterByAge = new IntegerField("Age filter",
                event -> dataView.setFilter(person -> event.getValue() == null
                        || event.getValue().equals(person.getAge())));
        filterByAge.setId(AGE_FILTER);

        // Sorting
        NativeButton reverseSorting = new NativeButton("Reverse sorting",
                event -> dataView.setSortOrder(Person::getFirstName,
                        SortDirection.DESCENDING));
        reverseSorting.setId(REVERSE_SORTING);

        // Item count listener
        dataView.addItemCountChangeListener(event -> {
            count.setText(Integer.toString(event.getItemCount()));
            showPreviousData.setEnabled(itemSelect.getValue() > 0);
            showNextData.setEnabled(
                    itemSelect.getValue() < event.getItemCount() - 1);
        });

        itemSelect.setValue(0);
        showPreviousData.setEnabled(false);
        itemSelect.addValueChangeListener(event -> {
            if (event.getValue() >= dataView.getItemCount()) {
                itemData.setText("Action: Item outside of data rage of [0,"
                        + (dataView.getItemCount() - 1)
                        + "]. Resetting to previous");
                itemSelect.setValue(event.getOldValue());
                return;
            }
            showPreviousData.setEnabled(event.getValue() > 0);
            showNextData
                    .setEnabled(event.getValue() < dataView.getItemCount() - 1);
        });

        add(comboBox, itemSelect, filterByAge, reverseSorting,
                selectItemOnIndex, showItemData, showNextData, showPreviousData,
                removePerson, count, itemData, showItemCount, showItems,
                secondComboBox);
    }

    private List<Person> generatePersonItems() {
        return IntStream.range(0, 250)
                .mapToObj(index -> new Person(index, "Person " + index,
                        "lastName", index % 100, new Person.Address(),
                        "1234567890"))
                .collect(Collectors.toList());
    }
}
