package com.vaadin.flow.component.radiobutton.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.dataview.RadioButtonGroupDataView;
import com.vaadin.flow.component.radiobutton.dataview.RadioButtonGroupListDataView;
import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;

@Route("vaadin-radio-button/radio-Button-group-data-view")
public class RadioButtonGroupDataViewPage extends Div {

    private static final String FIRST = "first";
    private static final String SECOND = "second";
    private static final String THIRD = "third";
    private static final String CHANGED_1 = "changed-1";
    private static final String CHANGED_2 = "changed-2";

    static final String RADIO_GROUP_FOR_DATA_VIEW = "radio-group-for-data-view";
    static final String RADIO_GROUP_FOR_LIST_DATA_VIEW = "radio-group-for-list-data-view";
    static final String RADIO_GROUP_FOR_ADD_TO_DATA_VIEW = "radio-group-for-add-to-data-view";
    static final String RADIO_GROUP_FOR_REMOVE_FROM_DATA_VIEW = "radio-group-for-remove-from-data-view";
    static final String RADIO_GROUP_FOR_FILTER_DATA_VIEW = "radio-group-for-filter-data-view";
    static final String RADIO_GROUP_FOR_NEXT_PREV_DATA_VIEW = "radio-group-for-next-prev-data-view";
    static final String RADIO_GROUP_FOR_SORT_DATA_VIEW = "radio-group-for-sort-data-view";
    static final String RADIO_GROUP_SELECTED_ID_SPAN = "radio-group-for-selected-ids-span";
    static final String OTHER_RADIO_GROUP_FOR_ADD_TO_DATA_VIEW = "other-radio-group-for-add-to-data-view";
    static final String OTHER_RADIO_GROUP_FOR_REMOVE_FROM_DATA_VIEW = "other-radio-group-for-remove-from-data-view";
    static final String OTHER_RADIO_GROUP_FOR_FILTER_DATA_VIEW = "other-radio-group-for-filter-data-view";
    static final String OTHER_RADIO_GROUP_FOR_SORT_DATA_VIEW = "other-radio-group-for-sort-data-view";

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
    static final String RADIO_GROUP_SELECTION_BY_ID_UPDATE_BUTTON = "radio-group-selection-by-id-update-button";
    static final String RADIO_GROUP_SELECTION_BY_ID_AND_NAME_UPDATE_BUTTON = "radio-group-selection-by-id-and-name-update-button";

    static final String CURRENT_ITEM_SPAN = "current-item-span";
    static final String HAS_NEXT_ITEM_SPAN = "has-next-item-span";
    static final String HAS_PREV_ITEM_SPAN = "has-prev-item-span";

    public RadioButtonGroupDataViewPage() {
        createGenericDataView();
        createListDataView();
        createAddItemByDataView();
        createRemoveItemByDataView();
        createFilterItemsByDataView();
        createNextPreviousItemDataView();
        createSetSortComparatorDataView();
        createIdentifierProviderForListBox();
    }

    private void createGenericDataView() {
        Item first = new Item(1L, FIRST);
        Item second = new Item(2L, SECOND);

        List<Item> items = new ArrayList<>(Arrays.asList(first, second));
        GenericDataProvider dataProvider = new GenericDataProvider(items);

        RadioButtonGroup<Item> rbgForDataView = new RadioButtonGroup<>();
        rbgForDataView.setId(RADIO_GROUP_FOR_DATA_VIEW);

        RadioButtonGroupDataView<Item> dataView = rbgForDataView
                .setItems(dataProvider);
        dataView.setIdentifierProvider(Item::getId);

        NativeButton dataViewUpdateButton = new NativeButton("Update",
                click -> {
                    first.setValue(CHANGED_1);
                    second.setValue(CHANGED_2);

                    dataView.refreshItem(new Item(1L));
                });
        dataViewUpdateButton.setId(DATA_VIEW_UPDATE_BUTTON);

        add(rbgForDataView, dataViewUpdateButton);
    }

    private void createListDataView() {
        Item first = new Item(1L, FIRST);
        Item second = new Item(2L, SECOND);

        RadioButtonGroup<Item> rbgForListDataView = new RadioButtonGroup<>();
        rbgForListDataView.setId(RADIO_GROUP_FOR_LIST_DATA_VIEW);

        RadioButtonGroupListDataView<Item> dataView = rbgForListDataView
                .setItems(first, second);
        dataView.setIdentifierProvider(Item::getId);

        NativeButton dataViewUpdateButton = new NativeButton("Update",
                click -> {
                    first.setValue(CHANGED_1);
                    second.setValue(CHANGED_2);

                    dataView.refreshItem(new Item(1L));
                });
        dataViewUpdateButton.setId(LIST_DATA_VIEW_UPDATE_BUTTON);

        add(rbgForListDataView, dataViewUpdateButton);
    }

    private void createAddItemByDataView() {
        Item first = new Item(1L, FIRST);

        RadioButtonGroup<Item> rbgForAddToDataView = new RadioButtonGroup<>();
        rbgForAddToDataView.setId(RADIO_GROUP_FOR_ADD_TO_DATA_VIEW);

        RadioButtonGroup<Item> otherRbgForAddToDataView = new RadioButtonGroup<>();
        otherRbgForAddToDataView.setId(OTHER_RADIO_GROUP_FOR_ADD_TO_DATA_VIEW);

        final ListDataProvider<Item> listDataProvider = DataProvider
                .ofItems(first);

        RadioButtonGroupListDataView<Item> dataView = rbgForAddToDataView
                .setItems(listDataProvider);

        otherRbgForAddToDataView.setItems(listDataProvider);

        NativeButton dataViewAddButton = new NativeButton("Add", click -> {
            Item second = new Item(2L, SECOND);
            dataView.addItem(second);
        });
        dataViewAddButton.setId(LIST_DATA_VIEW_ADD_BUTTON);

        add(rbgForAddToDataView, otherRbgForAddToDataView, dataViewAddButton);
    }

    private void createRemoveItemByDataView() {
        Item first = new Item(1L, FIRST);
        Item second = new Item(2L, SECOND);

        RadioButtonGroup<Item> rbgForRemoveFromDataView = new RadioButtonGroup<>();
        rbgForRemoveFromDataView.setId(RADIO_GROUP_FOR_REMOVE_FROM_DATA_VIEW);

        RadioButtonGroup<Object> otherRbgForRemoveFromDataView = new RadioButtonGroup<>();
        otherRbgForRemoveFromDataView
                .setId(OTHER_RADIO_GROUP_FOR_REMOVE_FROM_DATA_VIEW);

        ListDataProvider<Item> listDataProvider = DataProvider.ofItems(first,
                second);

        RadioButtonGroupListDataView<Item> dataView = rbgForRemoveFromDataView
                .setItems(listDataProvider);

        otherRbgForRemoveFromDataView.setItems(listDataProvider);

        NativeButton dataViewRemoveButton = new NativeButton("Remove",
                click -> dataView.removeItem(second));
        dataViewRemoveButton.setId(LIST_DATA_VIEW_REMOVE_BUTTON);

        add(rbgForRemoveFromDataView, otherRbgForRemoveFromDataView,
                dataViewRemoveButton);
    }

    private void createFilterItemsByDataView() {
        RadioButtonGroup<Integer> numbers = new RadioButtonGroup<>();
        numbers.setId(RADIO_GROUP_FOR_FILTER_DATA_VIEW);

        RadioButtonGroup<Integer> otherNumbers = new RadioButtonGroup<>();
        otherNumbers.setId(OTHER_RADIO_GROUP_FOR_FILTER_DATA_VIEW);

        final ListDataProvider<Integer> listDataProvider = DataProvider
                .ofItems(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        RadioButtonGroupListDataView<Integer> numbersDataView = numbers
                .setItems(listDataProvider);

        otherNumbers.setItems(listDataProvider);

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

        RadioButtonGroup<Item> rbgForNextPrevDataView = new RadioButtonGroup<>();
        rbgForNextPrevDataView.setId(RADIO_GROUP_FOR_NEXT_PREV_DATA_VIEW);

        RadioButtonGroupListDataView<Item> dataView = rbgForNextPrevDataView
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

        add(rbgForNextPrevDataView, dataViewNextButton, dataViewPrevButton,
                currentItem, hasNextItem, hasPrevItem);
    }

    private void createSetSortComparatorDataView() {
        Item first = new Item(1L, FIRST);
        Item second = new Item(2L, SECOND);
        Item third = new Item(3L, THIRD);

        RadioButtonGroup<Item> rbgForSortDataView = new RadioButtonGroup<>();
        rbgForSortDataView.setId(RADIO_GROUP_FOR_SORT_DATA_VIEW);

        RadioButtonGroup<Item> otherRbgForSortDataView = new RadioButtonGroup<>();
        otherRbgForSortDataView.setId(OTHER_RADIO_GROUP_FOR_SORT_DATA_VIEW);

        final ListDataProvider<Item> listDataProvider = DataProvider
                .ofItems(third, first, second);

        RadioButtonGroupListDataView<Item> dataView = rbgForSortDataView
                .setItems(listDataProvider);

        otherRbgForSortDataView.setItems(listDataProvider);

        NativeButton dataViewSortButton = new NativeButton("Sort",
                click -> dataView.setSortComparator((item1, item2) -> item1
                        .getValue().compareToIgnoreCase(item2.getValue())));
        dataViewSortButton.setId(LIST_DATA_VIEW_SORT_BUTTON);

        add(rbgForSortDataView, otherRbgForSortDataView, dataViewSortButton);
    }

    private void createIdentifierProviderForListBox() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        RadioButtonGroup<CustomItem> radioButtonGroup = new RadioButtonGroup<>();
        RadioButtonGroupListDataView<CustomItem> listDataView = radioButtonGroup
                .setItems(items);
        // Setting the following Identifier Provider makes the component
        // independent from the CustomItem's equals method implementation:
        listDataView.setIdentifierProvider(CustomItem::getId);

        radioButtonGroup.setValue(new CustomItem(3L));

        Span selectedIdSpan = new Span();
        selectedIdSpan.setId(RADIO_GROUP_SELECTED_ID_SPAN);
        selectedIdSpan
                .setText(String.valueOf(radioButtonGroup.getValue().getId()));

        NativeButton updateAndSelectByIdOnlyButton = new NativeButton(
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
                    radioButtonGroup.setValue(new CustomItem(2L));

                    selectedIdSpan.setText(String
                            .valueOf(radioButtonGroup.getValue().getId()));
                });
        updateAndSelectByIdOnlyButton
                .setId(RADIO_GROUP_SELECTION_BY_ID_UPDATE_BUTTON);

        NativeButton selectByIdAndNameButton = new NativeButton(
                "Select by Id and Name", click -> {
                    // Select the item with the Id and a challenging wrong name
                    // to verify <equals> method is not in use:
                    radioButtonGroup.setValue(new CustomItem(3L, "Second"));

                    selectedIdSpan.setText(String
                            .valueOf(radioButtonGroup.getValue().getId()));
                });
        selectByIdAndNameButton
                .setId(RADIO_GROUP_SELECTION_BY_ID_AND_NAME_UPDATE_BUTTON);

        add(radioButtonGroup, selectedIdSpan, updateAndSelectByIdOnlyButton,
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
