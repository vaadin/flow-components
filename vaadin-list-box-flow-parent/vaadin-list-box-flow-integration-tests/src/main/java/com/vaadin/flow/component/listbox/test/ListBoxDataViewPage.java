/*
 * Copyright 2000-2020 Vaadin Ltd.
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

package com.vaadin.flow.component.listbox.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.dataview.ListBoxDataView;
import com.vaadin.flow.component.listbox.dataview.ListBoxListDataView;
import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;

@Route("vaadin-list-box/list-box-data-view")
public class ListBoxDataViewPage extends Div {

    private static final String FIRST = "first";
    private static final String SECOND = "second";
    private static final String THIRD = "third";
    private static final String CHANGED_1 = "changed-1";
    private static final String CHANGED_2 = "changed-2";

    static final String LIST_BOX_FOR_DATA_VIEW = "list-box-for-data-view";
    static final String LIST_BOX_FOR_LIST_DATA_VIEW = "list-box-for-list-data-view";
    static final String LIST_BOX_FOR_ADD_TO_DATA_VIEW = "list-box-for-add-to-data-view";
    static final String LIST_BOX_FOR_REMOVE_FROM_DATA_VIEW = "list-box-for-remove-from-data-view";
    static final String LIST_BOX_FOR_FILTER_DATA_VIEW = "list-box-for-filter-data-view";
    static final String LIST_BOX_FOR_NEXT_PREV_DATA_VIEW = "list-box-for-next-prev-data-view";
    static final String LIST_BOX_FOR_SORT_DATA_VIEW = "list-box-for-sort-data-view";

    static final String DATA_VIEW_UPDATE_BUTTON = "data-view-update-button";
    static final String LIST_DATA_VIEW_UPDATE_BUTTON = "list-data-view-update-button";
    static final String LIST_DATA_VIEW_ADD_BUTTON = "list-data-view-add-button";
    static final String LIST_DATA_VIEW_REMOVE_BUTTON = "list-data-view-remove-button";
    static final String LIST_DATA_VIEW_SET_FILTER_BUTTON = "list-data-view-set-filter-button";
    static final String LIST_DATA_VIEW_ADD_FILTER_BUTTON = "list-data-view-add-filter-button";
    static final String LIST_DATA_VIEW_REMOVE_FILTER_BUTTON = "list-data-view-remove-filter-button";
    static final String LIST_DATA_VIEW_NEXT_BUTTON = "list-data-view-next-button";
    static final String LIST_DATA_VIEW_PREV_BUTTON = "list-data-view-prev-button";
    static final String LIST_DATA_VIEW_SORT_BUTTON = "list-data-view-sort-button";

    static final String CURRENT_ITEM_SPAN = "current-item-span";
    static final String HAS_NEXT_ITEM_SPAN = "has-next-item-span";
    static final String HAS_PREV_ITEM_SPAN = "has-prev-item-span";

    public ListBoxDataViewPage() {
        createGenericDataView();
        createListDataView();
        createAddItemByDataView();
        createRemoveItemByDataView();
        createFilterItemsByDataView();
        createNextPreviousItemDataView();
        createSetSortComparatorDataView();
    }

    private void createGenericDataView() {
        Item first = new Item(1L, FIRST);
        Item second = new Item(2L, SECOND);

        List<Item> items = new ArrayList<>(Arrays.asList(first, second));
        GenericDataProvider dataProvider = new GenericDataProvider(items);

        ListBox<Item> listBoxForDataView = new ListBox<>();
        listBoxForDataView.setId(LIST_BOX_FOR_DATA_VIEW);

        ListBoxDataView<Item> dataView = listBoxForDataView
                .setItems(dataProvider);
        dataView.setIdentifierProvider(Item::getId);

        NativeButton dataViewUpdateButton = new NativeButton("Update",
                click -> {
                    first.setValue(CHANGED_1);
                    second.setValue(CHANGED_2);

                    dataView.refreshItem(new Item(1L));
                });
        dataViewUpdateButton.setId(DATA_VIEW_UPDATE_BUTTON);

        add(listBoxForDataView, dataViewUpdateButton);
    }

    private void createListDataView() {
        Item first = new Item(1L, FIRST);
        Item second = new Item(2L, SECOND);

        ListBox<Item> listBoxForListDataView = new ListBox<>();
        listBoxForListDataView.setId(LIST_BOX_FOR_LIST_DATA_VIEW);

        ListBoxListDataView<Item> dataView = listBoxForListDataView
                .setItems(first, second);
        dataView.setIdentifierProvider(Item::getId);

        NativeButton dataViewUpdateButton = new NativeButton("Update",
                click -> {
                    first.setValue(CHANGED_1);
                    second.setValue(CHANGED_2);

                    dataView.refreshItem(new Item(1L));
                });
        dataViewUpdateButton.setId(LIST_DATA_VIEW_UPDATE_BUTTON);

        add(listBoxForListDataView, dataViewUpdateButton);
    }

    private void createAddItemByDataView() {
        Item first = new Item(1L, FIRST);
        List<Item> items = new ArrayList<>();
        items.add(first);

        ListBox<Item> listBoxForAddToDataView = new ListBox<>();
        listBoxForAddToDataView.setId(LIST_BOX_FOR_ADD_TO_DATA_VIEW);

        ListBoxListDataView<Item> dataView = listBoxForAddToDataView
                .setItems(items);

        NativeButton dataViewAddButton = new NativeButton("Add", click -> {
            Item second = new Item(2L, SECOND);
            dataView.addItem(second);
        });
        dataViewAddButton.setId(LIST_DATA_VIEW_ADD_BUTTON);

        add(listBoxForAddToDataView, dataViewAddButton);
    }

    private void createRemoveItemByDataView() {
        Item first = new Item(1L, FIRST);
        Item second = new Item(2L, SECOND);
        List<Item> items = new ArrayList<>(Arrays.asList(first, second));

        ListBox<Item> listBoxForRemoveFromDataView = new ListBox<>();
        listBoxForRemoveFromDataView.setId(LIST_BOX_FOR_REMOVE_FROM_DATA_VIEW);

        ListBoxListDataView<Item> dataView = listBoxForRemoveFromDataView
                .setItems(items);

        NativeButton dataViewRemoveButton = new NativeButton("Remove",
                click -> dataView.removeItem(second));
        dataViewRemoveButton.setId(LIST_DATA_VIEW_REMOVE_BUTTON);

        add(listBoxForRemoveFromDataView, dataViewRemoveButton);
    }

    private void createFilterItemsByDataView() {
        ListBox<Integer> numbers = new ListBox<>();
        numbers.setId(LIST_BOX_FOR_FILTER_DATA_VIEW);
        ListBoxListDataView<Integer> numbersDataView = numbers
                .setItems(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        NativeButton filterOdds = new NativeButton("Filter Odds",
                click -> numbersDataView.setFilter(num -> num % 2 == 0));
        filterOdds.setId(LIST_DATA_VIEW_SET_FILTER_BUTTON);

        NativeButton filterMultiplesOfThree = new NativeButton(
                "Filter Multiples of 3",
                click -> numbersDataView.addFilter(num -> num % 3 != 0));
        filterMultiplesOfThree.setId(LIST_DATA_VIEW_ADD_FILTER_BUTTON);

        NativeButton noFilter = new NativeButton("No Filter",
                click -> numbersDataView.removeFilters());
        noFilter.setId(LIST_DATA_VIEW_REMOVE_FILTER_BUTTON);

        add(numbers, filterOdds, filterMultiplesOfThree, noFilter);
    }

    private void createNextPreviousItemDataView() {
        Item first = new Item(1L, FIRST);
        Item second = new Item(2L, SECOND);
        Item third = new Item(3L, THIRD);
        List<Item> items = new ArrayList<>(Arrays.asList(first, second, third));

        ListBox<Item> listBoxForNextPrevDataView = new ListBox<>();
        listBoxForNextPrevDataView.setId(LIST_BOX_FOR_NEXT_PREV_DATA_VIEW);

        ListBoxListDataView<Item> dataView = listBoxForNextPrevDataView
                .setItems(items);

        AtomicReference<Item> current = new AtomicReference<>(second);
        Span currentItem = new Span(current.get().getValue());
        currentItem.setId(CURRENT_ITEM_SPAN);
        Span hasNextItem = new Span(String
                .valueOf(dataView.getNextItem(current.get()).isPresent()));
        hasNextItem.setId(HAS_NEXT_ITEM_SPAN);
        Span hasPrevItem = new Span(String
                .valueOf(dataView.getPreviousItem(current.get()).isPresent()));
        hasPrevItem.setId(HAS_PREV_ITEM_SPAN);

        NativeButton dataViewNextButton = new NativeButton("Next", click -> {
            current.set(dataView.getNextItem(current.get()).get());
            currentItem.setText(current.get().getValue());
            hasNextItem.setText(String
                    .valueOf(dataView.getNextItem(current.get()).isPresent()));
            hasPrevItem.setText(String.valueOf(
                    dataView.getPreviousItem(current.get()).isPresent()));
        });
        dataViewNextButton.setId(LIST_DATA_VIEW_NEXT_BUTTON);

        NativeButton dataViewPrevButton = new NativeButton("Previous",
                click -> {
                    current.set(dataView.getPreviousItem(current.get()).get());
                    currentItem.setText(current.get().getValue());
                    hasNextItem.setText(String.valueOf(
                            dataView.getNextItem(current.get()).isPresent()));
                    hasPrevItem.setText(String.valueOf(dataView
                            .getPreviousItem(current.get()).isPresent()));
                });
        dataViewPrevButton.setId(LIST_DATA_VIEW_PREV_BUTTON);

        add(listBoxForNextPrevDataView, dataViewNextButton, dataViewPrevButton,
                currentItem, hasNextItem, hasPrevItem);
    }

    private void createSetSortComparatorDataView() {
        Item first = new Item(1L, FIRST);
        Item second = new Item(2L, SECOND);
        Item third = new Item(3L, THIRD);
        List<Item> items = new ArrayList<>(Arrays.asList(third, first, second));

        ListBox<Item> listBoxForSortDataView = new ListBox<>();
        listBoxForSortDataView.setId(LIST_BOX_FOR_SORT_DATA_VIEW);

        ListBoxListDataView<Item> dataView = listBoxForSortDataView
                .setItems(items);

        NativeButton dataViewSortButton = new NativeButton("Sort",
                click -> dataView.setSortComparator((item1, item2) -> item1
                        .getValue().compareToIgnoreCase(item2.getValue())));
        dataViewSortButton.setId(LIST_DATA_VIEW_SORT_BUTTON);

        add(listBoxForSortDataView, dataViewSortButton);
    }

    private static class GenericDataProvider
            extends AbstractDataProvider<Item, Void> {
        private final transient List<Item> items;

        public GenericDataProvider(List<Item> items) {
            this.items = items;
        }

        @Override
        public boolean isInMemory() {
            return true;
        }

        @Override
        public int size(Query<Item, Void> query) {
            return 2;
        }

        @Override
        public Stream<Item> fetch(Query<Item, Void> query) {
            return Stream.of(items.toArray(new Item[0]));
        }
    }

    private static class Item {
        private long id;
        private String value;

        public Item(long id) {
            this.id = id;
        }

        public Item(long id, String value) {
            this.id = id;
            this.value = value;
        }

        public long getId() {
            return this.id;
        }

        public String getValue() {
            return this.value;
        }

        public void setId(long id) {
            this.id = id;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                Item item = (Item) o;
                return this.id == item.id
                        && Objects.equals(this.value, item.value);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hash(this.id, this.value);
        }

        public String toString() {
            return String.valueOf(this.value);
        }
    }
}
