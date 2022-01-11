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

package com.vaadin.flow.component.listbox.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.listbox.dataview.ListBoxDataView;
import com.vaadin.flow.component.listbox.dataview.ListBoxListDataView;
import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
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

    static final String OTHER_LIST_BOX_FOR_ADD_TO_DATA_VIEW = "other-list-box-for-add-to-data-view";
    static final String OTHER_LIST_BOX_FOR_REMOVE_FROM_DATA_VIEW = "other-list-box-for-remove-from-data-view";
    static final String OTHER_LIST_BOX_FOR_FILTER_DATA_VIEW = "other-list-box-for-filter-data-view";
    static final String OTHER_LIST_BOX_FOR_SORT_DATA_VIEW = "other-list-box-for-sort-data-view";

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
    static final String LIST_BOX_SELECTION_BY_ID_UPDATE_BUTTON = "list-box-selection-by-id-update-button";
    static final String LIST_BOX_SELECTION_BY_ID_AND_NAME_UPDATE_BUTTON = "list-box-selection-by-id-and-name-update-button";
    static final String MULTI_SELECT_LIST_BOX_SELECTION_UPDATE_BUTTON = "multi-select-list-box-selection-update-button";
    static final String MULTI_SELECT_LIST_BOX_SELECTION_BY_ID_AND_NAME_BUTTON = "multi-select-list-box-selection-by-id-and-name-button";

    static final String CURRENT_ITEM_SPAN = "current-item-span";
    static final String HAS_NEXT_ITEM_SPAN = "has-next-item-span";
    static final String HAS_PREV_ITEM_SPAN = "has-prev-item-span";
    static final String LIST_BOX_SELECTED_IDS_SPAN = "list-box-selected-ids-span";
    static final String MULTI_SELECT_LIST_BOX_SELECTED_IDS_SPAN = "multi-select-list-box-selected-ids-span";

    public ListBoxDataViewPage() {
        createGenericDataView();
        createListDataView();
        createAddItemByDataView();
        createRemoveItemByDataView();
        createFilterItemsByDataView();
        createNextPreviousItemDataView();
        createSetSortComparatorDataView();
        createIdentifierProviderForMultiSelectListBox();
        createIdentifierProviderForListBox();
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

        ListBox<Item> listBoxForAddToDataView = new ListBox<>();
        listBoxForAddToDataView.setId(LIST_BOX_FOR_ADD_TO_DATA_VIEW);

        ListBox<Item> otherListBox = new ListBox<>();
        otherListBox.setId(OTHER_LIST_BOX_FOR_ADD_TO_DATA_VIEW);

        final ListDataProvider<Item> listDataProvider = DataProvider
                .ofItems(first);

        ListBoxListDataView<Item> dataView = listBoxForAddToDataView
                .setItems(listDataProvider);

        otherListBox.setItems(listDataProvider);

        NativeButton dataViewAddButton = new NativeButton("Add", click -> {
            Item second = new Item(2L, SECOND);
            dataView.addItem(second);
        });
        dataViewAddButton.setId(LIST_DATA_VIEW_ADD_BUTTON);

        add(listBoxForAddToDataView, otherListBox, dataViewAddButton);
    }

    private void createRemoveItemByDataView() {
        Item first = new Item(1L, FIRST);
        Item second = new Item(2L, SECOND);

        ListBox<Item> listBoxForRemoveFromDataView = new ListBox<>();
        listBoxForRemoveFromDataView.setId(LIST_BOX_FOR_REMOVE_FROM_DATA_VIEW);

        ListBox<Item> otherListBox = new ListBox<>();
        otherListBox.setId(OTHER_LIST_BOX_FOR_REMOVE_FROM_DATA_VIEW);

        final ListDataProvider<Item> listDataProvider = DataProvider
                .ofItems(first, second);

        ListBoxListDataView<Item> dataView = listBoxForRemoveFromDataView
                .setItems(listDataProvider);

        otherListBox.setItems(listDataProvider);

        NativeButton dataViewRemoveButton = new NativeButton("Remove",
                click -> dataView.removeItem(second));
        dataViewRemoveButton.setId(LIST_DATA_VIEW_REMOVE_BUTTON);

        add(listBoxForRemoveFromDataView, otherListBox, dataViewRemoveButton);
    }

    private void createFilterItemsByDataView() {
        ListBox<Integer> numbers = new ListBox<>();
        numbers.setId(LIST_BOX_FOR_FILTER_DATA_VIEW);

        ListBox<Integer> otherNumbers = new ListBox<>();
        otherNumbers.setId(OTHER_LIST_BOX_FOR_FILTER_DATA_VIEW);

        final ListDataProvider<Integer> dataProvider = DataProvider.ofItems(1,
                2, 3, 4, 5, 6, 7, 8, 9, 10);

        ListBoxListDataView<Integer> numbersDataView = numbers
                .setItems(dataProvider);

        otherNumbers.setItems(dataProvider);

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

        add(numbers, otherNumbers, filterOdds, filterMultiplesOfThree,
                noFilter);
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

        ListBox<Item> listBoxForSortDataView = new ListBox<>();
        listBoxForSortDataView.setId(LIST_BOX_FOR_SORT_DATA_VIEW);

        ListBox<Item> otherListBox = new ListBox<>();
        otherListBox.setId(OTHER_LIST_BOX_FOR_SORT_DATA_VIEW);

        final ListDataProvider<Item> dataProvider = DataProvider.ofItems(third,
                first, second);

        ListBoxListDataView<Item> dataView = listBoxForSortDataView
                .setItems(dataProvider);

        otherListBox.setItems(dataProvider);

        NativeButton dataViewSortButton = new NativeButton("Sort",
                click -> dataView.setSortComparator((item1, item2) -> item1
                        .getValue().compareToIgnoreCase(item2.getValue())));
        dataViewSortButton.setId(LIST_DATA_VIEW_SORT_BUTTON);

        add(listBoxForSortDataView, otherListBox, dataViewSortButton);
    }

    private void createIdentifierProviderForMultiSelectListBox() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        CustomItem fourth = new CustomItem(4L, "Fourth");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third, fourth));

        MultiSelectListBox<CustomItem> multiSelectListBox = new MultiSelectListBox<>();
        ListBoxListDataView<CustomItem> listDataView = multiSelectListBox
                .setItems(items);
        // Setting the following Identifier Provider makes the component
        // independent from the CustomItem's equals method implementation:
        listDataView.setIdentifierProvider(CustomItem::getId);

        Set<CustomItem> selected = new HashSet<>(
                Arrays.asList(new CustomItem(1L), third));
        multiSelectListBox.setValue(selected);

        Span selectedIdsSpan = new Span();
        selectedIdsSpan.setId(MULTI_SELECT_LIST_BOX_SELECTED_IDS_SPAN);
        multiSelectListBox.getSelectedItems().stream()
                .map(item -> String.valueOf(item.getId())).sorted()
                .reduce((a, b) -> a + ", " + b)
                .ifPresent(selectedIdsSpan::setText);

        Button updateAndSelectByIdButton = new Button("Update & Select by Id",
                click -> {
                    // Make the names of unselected items similar to the name of
                    // selected
                    // one to mess with the <equals> implementation in
                    // CustomItem:
                    second.setName("First");
                    listDataView.refreshItem(second);

                    fourth.setName("Third");
                    listDataView.refreshItem(fourth);

                    // Select the items not only with the reference of existing
                    // items,
                    // but also the Id to verify <equals> is not in use and the
                    // selection is happening only based on identifier:
                    Set<CustomItem> newSelected = new HashSet<>(
                            Arrays.asList(second, new CustomItem(4L)));
                    multiSelectListBox.setValue(newSelected);

                    multiSelectListBox.getSelectedItems().stream()
                            .map(item -> String.valueOf(item.getId())).sorted()
                            .reduce((a, b) -> a + ", " + b)
                            .ifPresent(selectedIdsSpan::setText);
                });
        updateAndSelectByIdButton
                .setId(MULTI_SELECT_LIST_BOX_SELECTION_UPDATE_BUTTON);

        Button selectByIdAndNameButton = new Button("Select by Id and Name",
                click -> {
                    // Select the items not only with the reference of existing
                    // items,
                    // but also the Id and a challenging name to verify <equals>
                    // is not
                    // in use and the selection is happening only based on
                    // identifier:
                    Set<CustomItem> newSelected = new HashSet<>(
                            Arrays.asList(first, new CustomItem(3L, "Third")));
                    multiSelectListBox.setValue(newSelected);

                    multiSelectListBox.getSelectedItems().stream()
                            .map(item -> String.valueOf(item.getId())).sorted()
                            .reduce((a, b) -> a + ", " + b)
                            .ifPresent(selectedIdsSpan::setText);
                });
        selectByIdAndNameButton
                .setId(MULTI_SELECT_LIST_BOX_SELECTION_BY_ID_AND_NAME_BUTTON);

        add(multiSelectListBox, selectedIdsSpan, updateAndSelectByIdButton,
                selectByIdAndNameButton);
    }

    private void createIdentifierProviderForListBox() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        ListBox<CustomItem> listBox = new ListBox<>();
        ListBoxListDataView<CustomItem> listDataView = listBox.setItems(items);
        // Setting the following Identifier Provider makes the component
        // independent from the CustomItem's equals method implementation:
        listDataView.setIdentifierProvider(CustomItem::getId);

        listBox.setValue(new CustomItem(3L));

        Span selectedIdsSpan = new Span();
        selectedIdsSpan.setId(LIST_BOX_SELECTED_IDS_SPAN);
        selectedIdsSpan.setText(String.valueOf(listBox.getValue().getId()));

        Button updateAndSelectByIdOnlyButton = new Button(
                "Update & Select by Id", click -> {
                    // Make the names of unselected items similar to the name of
                    // selected one to mess with the <equals> implementation in
                    // CustomItem:
                    first.setName("Second");
                    listDataView.refreshItem(first);

                    third.setName("Second");
                    listDataView.refreshItem(third);

                    // Select the item not with the reference of existing item,
                    // and instead with just the Id:
                    listBox.setValue(new CustomItem(2L));

                    selectedIdsSpan.setText(
                            String.valueOf(listBox.getValue().getId()));
                });
        updateAndSelectByIdOnlyButton
                .setId(LIST_BOX_SELECTION_BY_ID_UPDATE_BUTTON);

        Button selectByIdAndNameButton = new Button("Select by Id and Name",
                click -> {
                    // Select the item with the Id and a challenging wrong name
                    // to verify <equals> method is not in use:
                    listBox.setValue(new CustomItem(3L, "Second"));

                    selectedIdsSpan.setText(
                            String.valueOf(listBox.getValue().getId()));
                });
        selectByIdAndNameButton
                .setId(LIST_BOX_SELECTION_BY_ID_AND_NAME_UPDATE_BUTTON);

        add(listBox, selectedIdsSpan, updateAndSelectByIdOnlyButton,
                selectByIdAndNameButton);
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

    private static class CustomItem {
        private Long id;
        private String name;

        public CustomItem(Long id) {
            this(id, null);
        }

        public CustomItem(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof CustomItem))
                return false;
            CustomItem that = (CustomItem) o;
            return Objects.equals(getName(), that.getName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getName());
        }
    }
}
