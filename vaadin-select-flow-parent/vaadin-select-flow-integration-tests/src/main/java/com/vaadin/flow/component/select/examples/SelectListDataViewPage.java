/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.select.examples;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.select.data.SelectListDataView;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-select-list-data-view")
public class SelectListDataViewPage extends Div {
    public static final String SELECT = "select-data-view";
    public static final String OTHER_SELECT = "other-select-data-view";
    public static final String ITEMS_SIZE = "size-span-data-view";
    public static final String ITEM_PRESENT = "item-present-span-data-view";
    public static final String ALL_ITEMS = "all-items-span-data-view";
    public static final String ITEM_ON_INDEX = "item-on-index-data-view";
    public static final String CURRENT_ITEM = "current-item-span-list-data-view";
    public static final String HAS_NEXT_ITEM = "has-next-item-span-list-data-view";
    public static final String HAS_PREVIOUS_ITEM = "has-prev-item-span-list-data-view";
    public static final String NEXT_ITEM = "next-item-button-list-data-view";
    public static final String PREVIOUS_ITEM = "prev-item-button-list-data-view";
    public static final String FILTER_BUTTON = "filter-button-list-data-view";
    public static final String SORT_BUTTON = "sort-button-list-data-view";
    public static final String ADD_ITEM = "add-person-button-list-data-view";
    public static final String UPDATE_ITEM = "update-person-button-list-data-view";
    public static final String DELETE_ITEM = "delete-person-button-list-data-view";

    public SelectListDataViewPage() {
        Select<Person> select = new Select<>();
        Select<Person> otherSelect = new Select<>();

        Person john = new Person(1, "John");
        Person paul = new Person(2, "Paul");
        Person mike = new Person(3, "Mike");

        final ListDataProvider<Person> dataProvider = DataProvider.ofItems(john,
                paul, mike);

        SelectListDataView<Person> dataView = select.setItems(dataProvider);
        otherSelect.setItems(dataProvider);

        Span sizeSpan = new Span(String.valueOf(dataView.getItemCount()));
        Span containsItemSpan = new Span(
                String.valueOf(dataView.contains(john)));
        Span allItemsSpan = new Span(dataView.getItems().map(Person::getName)
                .collect(Collectors.joining(",")));
        Span itemOnIndexSpan = new Span(dataView.getItem(0).getName());

        AtomicReference<Person> currentItem = new AtomicReference<>(paul);

        Span currentItemSpan = new Span(currentItem.get().getName());
        Span hasNextItemSpan = new Span(
                String.valueOf(dataView.getNextItem(john).isPresent()));
        Span hasPrevItemSpan = new Span(
                String.valueOf(dataView.getPreviousItem(paul).isPresent()));

        NativeButton nextItemButton = new NativeButton("Next Item", event -> {
            Person nextItem = dataView.getNextItem(currentItem.get()).get();
            currentItem.set(nextItem);
            currentItemSpan.setText(currentItem.get().getName());
        });
        NativeButton prevItemButton = new NativeButton("Previous Item",
                event -> {
                    Person prevItem = dataView
                            .getPreviousItem(currentItem.get()).get();
                    currentItem.set(prevItem);
                    currentItemSpan.setText(currentItem.get().getName());
                });
        NativeButton filterButton = new NativeButton("Filter Items",
                event -> dataView.setFilter(p -> p.getName().equals("Paul")));
        NativeButton sortButton = new NativeButton("Sort Items",
                event -> dataView.setSortComparator((p1, p2) -> p1.getName()
                        .compareToIgnoreCase(p2.getName())));

        dataView.setIdentifierProvider(Person::getId);
        NativeButton addNew = new NativeButton("Add new item", event -> {
            Person newItem = new Person(4, "Peter");
            dataView.addItem(newItem);
        });
        NativeButton updateName = new NativeButton("Update first name",
                event -> {
                    Person updatedPerson = dataView.getItem(0);
                    updatedPerson.setName("Jack");
                    dataView.refreshItem(updatedPerson);
                });
        NativeButton deletePerson = new NativeButton("Delete person",
                event -> dataView.removeItem(new Person(1, null)));

        select.setId(SELECT);
        otherSelect.setId(OTHER_SELECT);
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

        add(select, sizeSpan, containsItemSpan, allItemsSpan, itemOnIndexSpan,
                currentItemSpan, hasNextItemSpan, hasPrevItemSpan, filterButton,
                sortButton, nextItemButton, prevItemButton, addNew, updateName,
                deletePerson, otherSelect);
    }

    public static class Person {
        private String name;
        private int id;

        public Person(String name) {
            this.name = name;
        }

        public Person(int id, String name) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return String.valueOf(name);
        }
    }
}
