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
package com.vaadin.flow.component.checkbox.tests;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.dataview.CheckboxGroupListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-checkbox-group-list-data-view")
public class CheckboxGroupListDataViewPage extends Div {

    static final String CHECKBOX_GROUP = "checkbox-group-data-view";
    static final String OTHER_CHECKBOX_GROUP = "other-checkbox-group-data-view";
    static final String ITEMS_SIZE = "size-span-data-view";
    static final String ITEM_PRESENT = "item-present-span-data-view";
    static final String ALL_ITEMS = "all-items-span-data-view";
    static final String ITEM_ON_INDEX = "item-on-index-data-view";
    static final String CURRENT_ITEM = "current-item-span-list-data-view";
    static final String HAS_NEXT_ITEM = "has-next-item-span-list-data-view";
    static final String HAS_PREVIOUS_ITEM = "has-prev-item-span-list-data-view";
    static final String NEXT_ITEM = "next-item-button-list-data-view";
    static final String PREVIOUS_ITEM = "prev-item-button-list-data-view";
    static final String FILTER_BUTTON = "filter-button-list-data-view";
    static final String SORT_BUTTON = "sort-button-list-data-view";
    static final String ADD_ITEM = "add-person-button-list-data-view";
    static final String UPDATE_ITEM = "update-person-button-list-data-view";
    static final String DELETE_ITEM = "delete-person-button-list-data-view";

    public CheckboxGroupListDataViewPage() {
        CheckboxGroup<CheckboxGroupDemoPage.Person> checkboxGroup = new CheckboxGroup<>();
        CheckboxGroup<CheckboxGroupDemoPage.Person> otherCheckBox = new CheckboxGroup<>();

        CheckboxGroupDemoPage.Person john = new CheckboxGroupDemoPage.Person(1,
                "John");
        CheckboxGroupDemoPage.Person paul = new CheckboxGroupDemoPage.Person(2,
                "Paul");
        CheckboxGroupDemoPage.Person mike = new CheckboxGroupDemoPage.Person(3,
                "Mike");

        final ListDataProvider<CheckboxGroupDemoPage.Person> personListDataProvider = DataProvider
                .ofItems(john, paul, mike);

        CheckboxGroupListDataView<CheckboxGroupDemoPage.Person> dataView = checkboxGroup
                .setItems(personListDataProvider);

        otherCheckBox.setItems(personListDataProvider);

        Span sizeSpan = new Span(String.valueOf(dataView.getItemCount()));
        Span containsItemSpan = new Span(
                String.valueOf(dataView.contains(john)));
        Span allItemsSpan = new Span(
                dataView.getItems().map(CheckboxGroupDemoPage.Person::getName)
                        .collect(Collectors.joining(",")));
        Span itemOnIndexSpan = new Span(dataView.getItem(0).getName());

        AtomicReference<CheckboxGroupDemoPage.Person> currentItem = new AtomicReference<>(
                paul);

        Span currentItemSpan = new Span(currentItem.get().getName());
        Span hasNextItemSpan = new Span(
                String.valueOf(dataView.getNextItem(john).isPresent()));
        Span hasPrevItemSpan = new Span(
                String.valueOf(dataView.getPreviousItem(paul).isPresent()));

        Button nextItemButton = new Button("Next Item", event -> {
            CheckboxGroupDemoPage.Person nextItem = dataView
                    .getNextItem(currentItem.get()).get();
            currentItem.set(nextItem);
            currentItemSpan.setText(currentItem.get().getName());
        });
        Button prevItemButton = new Button("Previous Item", event -> {
            CheckboxGroupDemoPage.Person prevItem = dataView
                    .getPreviousItem(currentItem.get()).get();
            currentItem.set(prevItem);
            currentItemSpan.setText(currentItem.get().getName());
        });
        Button filterButton = new Button("Filter Items",
                event -> dataView.setFilter(p -> p.getName().equals("Paul")));
        Button sortButton = new Button("Sort Items",
                event -> dataView.setSortComparator((p1, p2) -> p1.getName()
                        .compareToIgnoreCase(p2.getName())));

        dataView.setIdentifierProvider(CheckboxGroupDemoPage.Person::getId);
        Button addNew = new Button("Add new item", event -> {
            CheckboxGroupDemoPage.Person newItem = new CheckboxGroupDemoPage.Person(
                    4, "Peter");
            dataView.addItem(newItem);
        });
        Button updateName = new Button("Update first name", event -> {
            CheckboxGroupDemoPage.Person updatedPerson = dataView.getItem(0);
            updatedPerson.setName("Jack");
            dataView.refreshItem(updatedPerson);
        });
        Button deletePerson = new Button("Delete person", event -> dataView
                .removeItem(new CheckboxGroupDemoPage.Person(1, null)));

        checkboxGroup.setId(CHECKBOX_GROUP);
        otherCheckBox.setId(OTHER_CHECKBOX_GROUP);
        sizeSpan.setId(ITEMS_SIZE);
        containsItemSpan.setId(ITEM_PRESENT);
        allItemsSpan.setId(ALL_ITEMS);
        itemOnIndexSpan.setId(ITEM_ON_INDEX);
        currentItemSpan.setId(CURRENT_ITEM);
        hasNextItemSpan.setId(HAS_NEXT_ITEM);
        hasPrevItemSpan.setId(HAS_PREVIOUS_ITEM);
        nextItemButton.setId(NEXT_ITEM);
        prevItemButton.setId(PREVIOUS_ITEM);
        filterButton.setId(FILTER_BUTTON);
        sortButton.setId(SORT_BUTTON);
        addNew.setId(ADD_ITEM);
        updateName.setId(UPDATE_ITEM);
        deletePerson.setId(DELETE_ITEM);

        add(checkboxGroup, sizeSpan, containsItemSpan, allItemsSpan,
                itemOnIndexSpan, currentItemSpan, hasNextItemSpan,
                hasPrevItemSpan, filterButton, sortButton, nextItemButton,
                prevItemButton, addNew, updateName, deletePerson,
                otherCheckBox);
    }
}
