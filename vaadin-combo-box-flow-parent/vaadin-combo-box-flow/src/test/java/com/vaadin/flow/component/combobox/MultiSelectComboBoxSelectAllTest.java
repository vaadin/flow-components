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
package com.vaadin.flow.component.combobox;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.node.ArrayNode;

class MultiSelectComboBoxSelectAllTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private MultiSelectComboBox<String> comboBox;
    private List<String> items;

    @BeforeEach
    void setup() {
        comboBox = new MultiSelectComboBox<>();
        ui.add(comboBox);
        // More items than the page size, so that filtering happens on the
        // server and the first page does not contain all items
        items = IntStream.rangeClosed(1, 100).mapToObj(i -> "Item " + i)
                .toList();
    }

    @Nested
    class SelectAllButtonVisible {
        @Test
        void defaultsToFalse() {
            comboBox.setItems(items);

            Assertions.assertFalse(comboBox.isSelectAllButtonVisible());
            Assertions.assertFalse(getSelectAllButtonVisibleProperty());
        }

        @Test
        void inMemoryItems_propertyFollowsSetter() {
            comboBox.setItems(items);

            comboBox.setSelectAllButtonVisible(true);
            Assertions.assertTrue(comboBox.isSelectAllButtonVisible());
            Assertions.assertTrue(getSelectAllButtonVisibleProperty());

            comboBox.setSelectAllButtonVisible(false);
            Assertions.assertFalse(comboBox.isSelectAllButtonVisible());
            Assertions.assertFalse(getSelectAllButtonVisibleProperty());
        }

        @Test
        void noItems_propertyIsSet() {
            comboBox.setSelectAllButtonVisible(true);

            Assertions.assertTrue(getSelectAllButtonVisibleProperty());
        }

        @Test
        void lazyDataProvider_getterReturnsTrue_propertyIsNotSet() {
            setLazyItems();

            comboBox.setSelectAllButtonVisible(true);

            Assertions.assertTrue(comboBox.isSelectAllButtonVisible());
            Assertions.assertFalse(getSelectAllButtonVisibleProperty());
        }

        @Test
        void switchToLazyDataProvider_propertyIsCleared() {
            comboBox.setItems(items);
            comboBox.setSelectAllButtonVisible(true);

            setLazyItems();

            Assertions.assertTrue(comboBox.isSelectAllButtonVisible());
            Assertions.assertFalse(getSelectAllButtonVisibleProperty());
        }

        @Test
        void switchToInMemoryItems_propertyIsSet() {
            setLazyItems();
            comboBox.setSelectAllButtonVisible(true);

            comboBox.setItems(items);

            Assertions.assertTrue(getSelectAllButtonVisibleProperty());
        }

        private boolean getSelectAllButtonVisibleProperty() {
            return comboBox.getElement().getProperty("selectAllButtonVisible",
                    false);
        }
    }

    @Nested
    class AllSelectedProperty {
        @BeforeEach
        void setup() {
            comboBox.setItems(items);
            comboBox.setSelectAllButtonVisible(true);
        }

        @Test
        void noItemsSelected_false() {
            ui.fakeClientCommunication();
            Assertions.assertEquals(false, getAllSelectedProperty());
        }

        @Test
        void allItemsSelected_true() {
            comboBox.setValue(items);

            ui.fakeClientCommunication();
            Assertions.assertEquals(true, getAllSelectedProperty());
        }

        @Test
        void valueChange_updatesProperty() {
            comboBox.setValue(items);
            ui.fakeClientCommunication();
            Assertions.assertEquals(true, getAllSelectedProperty());

            comboBox.deselect("Item 1");
            ui.fakeClientCommunication();
            Assertions.assertEquals(false, getAllSelectedProperty());

            comboBox.select("Item 1");
            ui.fakeClientCommunication();
            Assertions.assertEquals(true, getAllSelectedProperty());
        }

        @Test
        void toggleSelectAll_updatesProperty() {
            comboBox.toggleSelectAll();
            ui.fakeClientCommunication();
            Assertions.assertEquals(true, getAllSelectedProperty());

            comboBox.toggleSelectAll();
            ui.fakeClientCommunication();
            Assertions.assertEquals(false, getAllSelectedProperty());
        }

        @Test
        void addItemAndRefresh_updatesProperty() {
            List<String> mutableItems = new ArrayList<>(items);
            comboBox.setItems(mutableItems);
            comboBox.setValue(mutableItems);
            ui.fakeClientCommunication();
            Assertions.assertEquals(true, getAllSelectedProperty());

            mutableItems.add("Item 101");
            comboBox.getDataProvider().refreshAll();
            ui.fakeClientCommunication();
            Assertions.assertEquals(false, getAllSelectedProperty());
        }

        @Test
        void refreshItem_updatesProperty() {
            MutableItem apple = new MutableItem("Apple");
            MutableItem applePie = new MutableItem("Apple pie");
            List<MutableItem> beans = new ArrayList<>(List.of(apple, applePie));
            IntStream.rangeClosed(1, 100)
                    .forEach(i -> beans.add(new MutableItem("Other " + i)));
            MultiSelectComboBox<MutableItem> beanComboBox = new MultiSelectComboBox<>();
            beanComboBox.setItemLabelGenerator(MutableItem::getLabel);
            beanComboBox.setItems(beans);
            beanComboBox.setSelectAllButtonVisible(true);
            ui.add(beanComboBox);
            beanComboBox.select(apple);
            beanComboBox.getDataController().setViewportRange(0,
                    beanComboBox.getPageSize(), "Apple");
            ui.fakeClientCommunication();
            Assertions.assertEquals(false,
                    getAllSelectedProperty(beanComboBox));

            // The item no longer matches the filter after the refresh
            applePie.setLabel("Banana");
            beanComboBox.getDataProvider().refreshItem(applePie);

            ui.fakeClientCommunication();
            Assertions.assertEquals(true, getAllSelectedProperty(beanComboBox));
        }

        @Test
        void filter_updatesPropertyForMatchingItems() {
            comboBox.select("Item 10", "Item 100");
            ui.fakeClientCommunication();
            Assertions.assertEquals(false, getAllSelectedProperty());

            setFilter("Item 10");
            ui.fakeClientCommunication();
            Assertions.assertEquals(true, getAllSelectedProperty());

            setFilter("Item 1");
            ui.fakeClientCommunication();
            Assertions.assertEquals(false, getAllSelectedProperty());
        }

        @Test
        void filterMatchesNoItems_false() {
            comboBox.setValue(items);

            setFilter("foo");
            ui.fakeClientCommunication();

            Assertions.assertEquals(false, getAllSelectedProperty());
        }

        @Test
        void closeDropdownWithFilter_updatesPropertyForAllItems() {
            comboBox.select("Item 10", "Item 100");
            setFilter("Item 10");
            ui.fakeClientCommunication();
            Assertions.assertEquals(true, getAllSelectedProperty());

            // Closing the dropdown clears the filter
            comboBox.getElement().setProperty("opened", false);

            ui.fakeClientCommunication();
            Assertions.assertEquals(false, getAllSelectedProperty());
        }

        @Test
        void listDataViewFilter_updatesProperty() {
            // Keep more items than the page size, so that filtering still
            // happens on the server
            List<String> filteredItems = items.subList(0, 60);
            comboBox.setValue(filteredItems);
            ui.fakeClientCommunication();
            Assertions.assertEquals(false, getAllSelectedProperty());

            comboBox.getListDataView().setFilter(filteredItems::contains);

            ui.fakeClientCommunication();
            Assertions.assertEquals(true, getAllSelectedProperty());
        }

        @Test
        void buttonNotVisible_propertyNotSet() {
            comboBox.setSelectAllButtonVisible(false);

            comboBox.setValue(items);

            ui.fakeClientCommunication();
            Assertions.assertNull(getAllSelectedProperty());
        }

        @Test
        void hideButton_removesProperty() {
            comboBox.setValue(items);
            ui.fakeClientCommunication();
            Assertions.assertEquals(true, getAllSelectedProperty());

            comboBox.setSelectAllButtonVisible(false);

            ui.fakeClientCommunication();
            Assertions.assertNull(getAllSelectedProperty());
        }

        @Test
        void lazyDataProvider_propertyNotSet() {
            setLazyItems();

            ui.fakeClientCommunication();
            Assertions.assertNull(getAllSelectedProperty());
        }

        @Test
        void switchToInMemoryItems_propertyIsSet() {
            setLazyItems();
            ui.fakeClientCommunication();
            Assertions.assertNull(getAllSelectedProperty());

            comboBox.setItems(items);
            ui.fakeClientCommunication();
            Assertions.assertEquals(false, getAllSelectedProperty());
        }

        @Test
        void clientSideFilter_propertyNotSet() {
            // Items fit into one page, so the client filters them
            comboBox.setItems(items.subList(0, 10));
            comboBox.setValue(items.subList(0, 10));

            ui.fakeClientCommunication();
            Assertions.assertNull(getAllSelectedProperty());
        }

        @Test
        void switchFromClientSideToServerSideFilter_propertyIsSet() {
            comboBox.setItems(items.subList(0, 10));
            ui.fakeClientCommunication();
            Assertions.assertNull(getAllSelectedProperty());

            comboBox.setItems(items);
            ui.fakeClientCommunication();
            Assertions.assertEquals(false, getAllSelectedProperty());
        }

        @Test
        void switchFromServerSideToClientSideFilter_propertyIsRemoved() {
            ui.fakeClientCommunication();
            Assertions.assertEquals(false, getAllSelectedProperty());

            // All items fit into one page now
            comboBox.setPageSize(200);

            ui.fakeClientCommunication();
            Assertions.assertNull(getAllSelectedProperty());
        }

        @Test
        void selectAndAddItems_switchToServerSideFilter_propertyIsSet() {
            List<String> mutableItems = new ArrayList<>(items.subList(0, 10));
            comboBox.setItems(mutableItems);
            ui.fakeClientCommunication();
            Assertions.assertNull(getAllSelectedProperty());

            comboBox.select("Item 1");
            mutableItems.addAll(items.subList(10, 100));
            comboBox.getDataProvider().refreshAll();

            ui.fakeClientCommunication();
            Assertions.assertEquals(false, getAllSelectedProperty());
        }

        @Test
        void selectAndAddItems_fetchesAllItemsOnce() {
            AtomicInteger fullFetches = new AtomicInteger();
            List<String> mutableItems = new ArrayList<>(items);
            comboBox.setItems(countFullFetches(mutableItems, fullFetches));
            setFilter("");
            ui.fakeClientCommunication();
            fullFetches.set(0);

            comboBox.select("Item 1");
            // Changes the item count, but keeps the server-side filter
            mutableItems.add("Item 101");
            comboBox.getDataProvider().refreshAll();
            ui.fakeClientCommunication();

            Assertions.assertEquals(1, fullFetches.get());
        }

        @Test
        void loadNextPage_doesNotFetchAllItems() {
            AtomicInteger fullFetches = new AtomicInteger();
            comboBox.setItems(countFullFetches(items, fullFetches));
            setFilter("");
            ui.fakeClientCommunication();
            fullFetches.set(0);

            comboBox.getDataController().setViewportRange(50, 50, "");
            ui.fakeClientCommunication();

            Assertions.assertEquals(0, fullFetches.get());
        }

        @Test
        void customItemFilter_fewItems_propertyIsSet() {
            // A custom filter always runs on the server
            comboBox.setItems(
                    (item, filterText) -> item.equals("Item " + filterText),
                    items.subList(0, 10));

            ui.fakeClientCommunication();
            Assertions.assertEquals(false, getAllSelectedProperty());
        }

        @Test
        void changeWhileDetached_updatesPropertyOnAttach() {
            ui.fakeClientCommunication();
            Assertions.assertEquals(false, getAllSelectedProperty());
            ui.remove(comboBox);

            comboBox.setValue(items);
            ui.add(comboBox);

            ui.fakeClientCommunication();
            Assertions.assertEquals(true, getAllSelectedProperty());
        }

        @Test
        void moveToDifferentUiWithPendingUpdate_updatesPropertyInNewUi() {
            ui.fakeClientCommunication();
            Assertions.assertEquals(false, getAllSelectedProperty());

            comboBox.setValue(items);
            // Simulates what the router does for @PreserveOnRefresh
            comboBox.getElement().removeFromTree(false);
            ui.replaceUI();
            ui.add(comboBox);

            ui.fakeClientCommunication();
            Assertions.assertEquals(true, getAllSelectedProperty());
        }
    }

    @Nested
    class ToggleSelectAll {
        private List<ComponentValueChangeEvent<MultiSelectComboBox<String>, Set<String>>> events;

        @BeforeEach
        void setup() {
            comboBox.setItems(items);
            comboBox.setSelectAllButtonVisible(true);
            events = new ArrayList<>();
            comboBox.addValueChangeListener(events::add);
        }

        @Test
        void noItemsSelected_selectsAllItems() {
            comboBox.toggleSelectAll();

            Assertions.assertEquals(new LinkedHashSet<>(items),
                    comboBox.getValue());
            Assertions.assertEquals(new LinkedHashSet<>(items),
                    comboBox.getSelectedItems());
        }

        @Test
        void someItemsSelected_selectsRemainingItems() {
            comboBox.select("Item 1", "Item 2");
            events.clear();

            comboBox.toggleSelectAll();

            Assertions.assertEquals(new LinkedHashSet<>(items),
                    comboBox.getValue());
            Assertions.assertEquals(1, events.size());
        }

        @Test
        void allItemsSelected_deselectsAllItems() {
            comboBox.setValue(items);
            events.clear();

            comboBox.toggleSelectAll();

            Assertions.assertTrue(comboBox.getValue().isEmpty());
            Assertions.assertTrue(comboBox.getSelectedItems().isEmpty());
            Assertions.assertEquals(1, events.size());
        }

        @Test
        void noItems_valueDoesNotChange() {
            comboBox.setItems();

            comboBox.toggleSelectAll();

            Assertions.assertTrue(comboBox.getValue().isEmpty());
            Assertions.assertTrue(events.isEmpty());
        }

        @Test
        void firesValueChangeEventFromClient() {
            comboBox.toggleSelectAll();

            Assertions.assertEquals(1, events.size());
            Assertions.assertTrue(events.get(0).isFromClient());
            Assertions.assertEquals(new LinkedHashSet<>(items),
                    events.get(0).getValue());
            Assertions.assertTrue(events.get(0).getOldValue().isEmpty());
        }

        @Test
        void updatesSelectedItemsProperty() {
            comboBox.toggleSelectAll();

            ArrayNode selectedItems = getSelectedItemsProperty();
            Assertions.assertEquals(100, selectedItems.size());
            Assertions.assertEquals("Item 1",
                    selectedItems.get(0).get("label").asString());
            Assertions.assertEquals(comboBox.getKeyMapper().key("Item 1"),
                    selectedItems.get(0).get("key").asString());
            Assertions.assertEquals("Item 100",
                    selectedItems.get(99).get("label").asString());
        }

        @Test
        void valueChangeRevertedByListener_keepsPreviousSelection() {
            comboBox.select("Item 1");
            comboBox.addValueChangeListener(e -> {
                if (e.isFromClient()) {
                    comboBox.setValue(e.getOldValue());
                }
            });

            comboBox.toggleSelectAll();

            Assertions.assertEquals(Set.of("Item 1"), comboBox.getValue());
            Assertions.assertEquals(Set.of("Item 1"),
                    comboBox.getSelectedItems());
            ArrayNode selectedItems = getSelectedItemsProperty();
            Assertions.assertEquals(1, selectedItems.size());
            Assertions.assertEquals("Item 1",
                    selectedItems.get(0).get("label").asString());
        }

        @Test
        void filter_selectsMatchingItems_keepsOtherItems() {
            comboBox.select("Item 1");
            setFilter("Item 10");
            events.clear();

            comboBox.toggleSelectAll();

            Assertions.assertEquals(Set.of("Item 1", "Item 10", "Item 100"),
                    comboBox.getValue());
            Assertions.assertEquals(1, events.size());
            Assertions.assertTrue(events.get(0).isFromClient());
        }

        @Test
        void filterAllMatchingItemsSelected_deselectsMatchingItems_keepsOtherItems() {
            comboBox.select("Item 1", "Item 10", "Item 100");
            setFilter("Item 10");
            events.clear();

            comboBox.toggleSelectAll();

            Assertions.assertEquals(Set.of("Item 1"), comboBox.getValue());
            Assertions.assertEquals(1, events.size());
        }

        @Test
        void filterMatchesNoItems_valueDoesNotChange() {
            comboBox.select("Item 1");
            setFilter("foo");
            events.clear();

            comboBox.toggleSelectAll();

            Assertions.assertEquals(Set.of("Item 1"), comboBox.getValue());
            Assertions.assertTrue(events.isEmpty());
        }

        @Test
        void listDataViewFilter_selectsMatchingItems() {
            // Keep more items than the page size, so that filtering still
            // happens on the server
            List<String> filteredItems = items.subList(0, 60);
            comboBox.getListDataView().setFilter(filteredItems::contains);
            ui.fakeClientCommunication();

            comboBox.toggleSelectAll();

            Assertions.assertEquals(new LinkedHashSet<>(filteredItems),
                    comboBox.getValue());
        }

        @Test
        void buttonNotVisible_valueDoesNotChange() {
            comboBox.setSelectAllButtonVisible(false);

            comboBox.toggleSelectAll();

            Assertions.assertTrue(comboBox.getValue().isEmpty());
            Assertions.assertTrue(events.isEmpty());
        }

        @Test
        void readOnly_valueDoesNotChange() {
            comboBox.setReadOnly(true);

            comboBox.toggleSelectAll();

            Assertions.assertTrue(comboBox.getValue().isEmpty());
            Assertions.assertTrue(events.isEmpty());
        }

        @Test
        void lazyDataProvider_valueDoesNotChange() {
            setLazyItems();
            events.clear();

            comboBox.toggleSelectAll();

            Assertions.assertTrue(comboBox.getValue().isEmpty());
            Assertions.assertTrue(events.isEmpty());
        }

        @Test
        void clientSideFilter_valueDoesNotChange() {
            // The client toggles the selection itself in this mode
            comboBox.setItems(items.subList(0, 10));
            ui.fakeClientCommunication();
            events.clear();

            comboBox.toggleSelectAll();

            Assertions.assertTrue(comboBox.getValue().isEmpty());
            Assertions.assertTrue(events.isEmpty());
        }

        private ArrayNode getSelectedItemsProperty() {
            return (ArrayNode) comboBox.getElement()
                    .getPropertyRaw("selectedItems");
        }
    }

    private void setLazyItems() {
        comboBox.setItems(query -> items.stream().skip(query.getOffset())
                .limit(query.getLimit()));
    }

    /**
     * Creates a data provider that counts the queries for all items.
     */
    private static ListDataProvider<String> countFullFetches(List<String> items,
            AtomicInteger fullFetches) {
        return new ListDataProvider<>(items) {
            @Override
            public Stream<String> fetch(
                    Query<String, SerializablePredicate<String>> query) {
                if (query.getLimit() == Integer.MAX_VALUE) {
                    fullFetches.incrementAndGet();
                }
                return super.fetch(query);
            }
        };
    }

    /**
     * Simulates the client requesting items for a filter.
     */
    private void setFilter(String filter) {
        comboBox.getDataController().setViewportRange(0, comboBox.getPageSize(),
                filter);
    }

    private Boolean getAllSelectedProperty() {
        return getAllSelectedProperty(comboBox);
    }

    /**
     * Returns the value of the {@code _allSelected} property, or {@code null}
     * if the property is not set.
     */
    private Boolean getAllSelectedProperty(MultiSelectComboBox<?> target) {
        if (!target.getElement().hasProperty("_allSelected")) {
            return null;
        }
        return (Boolean) target.getElement().getPropertyRaw("_allSelected");
    }

    private static class MutableItem {
        private String label;

        MutableItem(String label) {
            this.label = label;
        }

        String getLabel() {
            return label;
        }

        void setLabel(String label) {
            this.label = label;
        }
    }
}
