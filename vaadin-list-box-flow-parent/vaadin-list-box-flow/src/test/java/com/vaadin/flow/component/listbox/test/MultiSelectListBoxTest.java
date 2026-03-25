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
package com.vaadin.flow.component.listbox.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.listbox.dataview.ListBoxListDataView;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.SelectionPreservationMode;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.selection.MultiSelectionEvent;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

class MultiSelectListBoxTest {

    private MultiSelectListBox<Item> listBox;

    private Item foo, bar;

    private List<Item> items;
    private ListDataProvider<Item> dataProvider;
    private ListBoxListDataView<Item> dataView;

    private List<Set<Item>> eventValues;

    private MultiSelectionEvent<MultiSelectListBox<Item>, Item> selectionEvent;

    @BeforeEach
    void setup() {
        listBox = new MultiSelectListBox<>();

        foo = new Item("foo");
        bar = new Item("bar");

        items = new ArrayList<>();
        items.add(foo);
        items.add(bar);
        dataProvider = new ListDataProvider<>(items);
        dataView = listBox.setItems(dataProvider);

        eventValues = new ArrayList<>();
        listBox.addValueChangeListener(e -> eventValues.add(e.getValue()));

        selectionEvent = null;
        listBox.addSelectionListener(e -> selectionEvent = e);
    }

    @Test
    void hasEmptySetAsDefaultValue() {
        Set<Item> value = listBox.getValue();
        Assertions.assertNotNull(value);
        Assertions.assertTrue(value.isEmpty());
    }

    @Test
    void setValueNull_throws() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class, () -> listBox.setValue(null));
        Assertions.assertTrue(
                exception.getMessage().contains("Use the clear-method"));
    }

    @Test
    void setValue_refreshAll_valueCleared() {
        listBox.setValue(createSet(foo));
        dataProvider.refreshAll();
        Assertions.assertEquals(Collections.emptySet(), listBox.getValue());
    }

    @Test
    void setValue_eventFired() {
        listBox.setValue(createSet(foo));
        assertValueChangeEvents(createSet(foo));
        listBox.setValue(createSet(foo, bar));
        assertValueChangeEvents(createSet(foo), createSet(foo, bar));
    }

    @Test
    void setEqualSetAsValue_noEvent() {
        listBox.setValue(createSet(foo, bar));
        assertValueChangeEvents(createSet(foo, bar));

        listBox.setValue(createSet(bar, foo));
        assertValueChangeEvents(createSet(foo, bar));
    }

    @Test
    void selectedValuesPropertyReflectsSelectedIndices() {
        listBox.setValue(createSet(bar));
        assertSelectedValuesProperty(1);
        listBox.select(foo);
        assertSelectedValuesProperty(0, 1);
        listBox.setValue(createSet(foo));
        assertSelectedValuesProperty(0);
        listBox.clear();
        assertSelectedValuesProperty();
    }

    @Test
    void nonItemComponentsIgnoredInSelectedIndices() {
        listBox.prependComponents(bar, new Div());
        listBox.setValue(createSet(foo, bar));
        assertSelectedValuesProperty(0, 1);
    }

    @Test
    void changeData_refreshAll_itemComponentsUpdated() {
        foo.setName("foo updated");
        bar.setName("bar updated");
        dataView.refreshItem(foo);
        dataView.refreshItem(bar);
        assertItemContents("foo updated", "bar updated");
    }

    @Test
    void changeData_refreshItem_itemComponentUpdated() {
        foo.setName("foo updated");
        bar.setName("bar updated");
        dataView.refreshItem(foo);
        assertItemContents("foo updated", "bar");
    }

    @Test
    void setValue_selectionEventFired() {
        listBox.setValue(createSet(foo));
        listBox.setValue(createSet(bar));

        Assertions.assertEquals(createSet(bar),
                selectionEvent.getAddedSelection());
        Assertions.assertEquals(createSet(foo),
                selectionEvent.getRemovedSelection());

        Assertions.assertEquals(createSet(bar), selectionEvent.getValue());
        Assertions.assertEquals(createSet(bar),
                selectionEvent.getAllSelectedItems());

        Assertions.assertEquals(createSet(foo),
                selectionEvent.getOldSelection());

        Assertions.assertEquals(bar,
                selectionEvent.getFirstSelectedItem().get());
    }

    @Test
    void updateSelection_valueChangeEventFired() {
        listBox.setValue(createSet(foo));
        listBox.updateSelection(createSet(bar), createSet(foo));
        assertValueChangeEvents(createSet(foo), createSet(bar));
    }

    @Test
    void getValue_modifySet_throws() {
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> listBox.getValue().add(new Item("baz")));
    }

    @Test
    void getSelectedItems_modifySet_throws() {
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> listBox.getSelectedItems().add(new Item("baz")));
    }

    @Test
    void dataViewForFaultyDataProvider_throwsException() {
        MultiSelectListBox<String> multiSelectListBox = new MultiSelectListBox<>();
        final ListBoxListDataView<String> dataView = multiSelectListBox
                .setItems(Arrays.asList("one", "two"));

        DataProvider<String, Void> dataProvider = DataProvider
                .fromCallbacks(query -> Stream.of("one"), query -> 1);

        multiSelectListBox.setItems(dataProvider);

        IllegalStateException exception = Assertions.assertThrows(
                IllegalStateException.class,
                () -> multiSelectListBox.getListDataView());
        Assertions.assertTrue(exception.getMessage().contains(
                "ListBoxListDataView only supports 'ListDataProvider' "
                        + "or it's subclasses, but was given a "
                        + "'AbstractBackEndDataProvider'"));
    }

    @Test
    void setIdentifierProvider_setItemsWithIdentifierOnly_shouldSelectCorrectItem() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        MultiSelectListBox<CustomItem> multiSelectListBox = new MultiSelectListBox<>();
        ListBoxListDataView<CustomItem> listDataView = multiSelectListBox
                .setItems(items);
        // Setting the following Identifier Provider makes the component
        // independent from the CustomItem's equals method implementation:
        listDataView.setIdentifierProvider(CustomItem::getId);

        multiSelectListBox.setValue(createSet(new CustomItem(1L)));

        long[] selectedIds = multiSelectListBox.getSelectedItems().stream()
                .mapToLong(CustomItem::getId).toArray();
        Assertions.assertArrayEquals(new long[] { 1L }, selectedIds);

        // Make the names similar to the name of not selected one to mess
        // with the <equals> implementation in CustomItem:
        first.setName("Second");
        listDataView.refreshItem(first);
        third.setName("Second");
        listDataView.refreshItem(third);

        // Select the item not with the reference of existing item, but instead
        // with just the Id:
        multiSelectListBox.setValue(Collections.singleton(new CustomItem(2L)));

        selectedIds = multiSelectListBox.getSelectedItems().stream()
                .mapToLong(CustomItem::getId).toArray();
        Assertions.assertArrayEquals(new long[] { 2L }, selectedIds);
    }

    @Test
    void setIdentifierProvider_setItemWithIdAndWrongName_shouldSelectCorrectItemBasedOnIdNotEquals() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        MultiSelectListBox<CustomItem> multiSelectListBox = new MultiSelectListBox<>();
        ListBoxListDataView<CustomItem> listDataView = multiSelectListBox
                .setItems(items);
        // Setting the following Identifier Provider makes the component
        // independent from the CustomItem's equals method implementation:
        listDataView.setIdentifierProvider(CustomItem::getId);

        multiSelectListBox.setValue(createSet(new CustomItem(1L)));

        long[] selectedIds = multiSelectListBox.getSelectedItems().stream()
                .mapToLong(CustomItem::getId).toArray();
        Assertions.assertArrayEquals(new long[] { 1L }, selectedIds);

        // Make the names similar to the name of not selected one to mess
        // with the <equals> implementation in CustomItem:
        first.setName("Second");
        listDataView.refreshItem(first);
        third.setName("Second");
        listDataView.refreshItem(third);

        // Select the item not with the reference of existing item, but instead
        // with just the Id:
        multiSelectListBox
                .setValue(Collections.singleton(new CustomItem(3L, "Second")));

        selectedIds = multiSelectListBox.getSelectedItems().stream()
                .mapToLong(CustomItem::getId).toArray();
        Assertions.assertArrayEquals(new long[] { 3L }, selectedIds);
    }

    @Test
    void withoutSettingIdentifierProvider_setItemWithNullId_shouldSelectCorrectItemBasedOnEquals() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        MultiSelectListBox<CustomItem> multiSelectListBox = new MultiSelectListBox<>();
        ListBoxListDataView<CustomItem> listDataView = multiSelectListBox
                .setItems(items);

        multiSelectListBox.setValue(
                Collections.singleton(new CustomItem(null, "Second")));

        Assertions.assertNotNull(multiSelectListBox.getValue());

        long[] selectedIds = multiSelectListBox.getSelectedItems().stream()
                .mapToLong(CustomItem::getId).toArray();

        Assertions.assertArrayEquals(new long[] { 2L }, selectedIds);
    }

    @Test
    void setIdentifierProviderOnId_setItemWithNullId_shouldThrowException() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        ListBox<CustomItem> multiSelectListBox = new ListBox<>();
        ListBoxListDataView<CustomItem> listDataView = multiSelectListBox
                .setItems(items);
        // Setting the following Identifier Provider makes the component
        // independent from the CustomItem's equals method implementation:
        listDataView.setIdentifierProvider(CustomItem::getId);

        Assertions.assertThrows(NullPointerException.class,
                () -> multiSelectListBox
                        .setValue(new CustomItem(null, "First")));
    }

    @Test
    void implementsHasTooltip() {
        Assertions.assertTrue(listBox instanceof HasTooltip);
    }

    @Test
    void implementsHasAriaLabel() {
        Assertions.assertTrue(listBox instanceof HasAriaLabel);
    }

    @Test
    void setAriaLabel() {
        listBox.setAriaLabel("aria-label");

        Assertions.assertTrue(listBox.getAriaLabel().isPresent());
        Assertions.assertEquals("aria-label", listBox.getAriaLabel().get());

        listBox.setAriaLabel(null);

        Assertions.assertTrue(listBox.getAriaLabel().isEmpty());
    }

    @Test
    void setAriaLabelledBy() {
        listBox.setAriaLabelledBy("aria-labelledby");

        Assertions.assertTrue(listBox.getAriaLabelledBy().isPresent());
        Assertions.assertEquals("aria-labelledby",
                listBox.getAriaLabelledBy().get());

        listBox.setAriaLabelledBy(null);

        Assertions.assertTrue(listBox.getAriaLabelledBy().isEmpty());
    }

    @Test
    void discardSelectionOnDataChange_noExtraChangeEventsFired() {
        listBox.setSelectionPreservationMode(SelectionPreservationMode.DISCARD);

        Item selectedItem = items.get(0);
        listBox.select(selectedItem);
        Assertions.assertEquals(Set.of(selectedItem),
                listBox.getSelectedItems());
        Assertions.assertNotNull(selectionEvent);
        selectionEvent = null;

        listBox.getDataProvider().refreshAll();
        Assertions.assertTrue(listBox.getSelectedItems().isEmpty());
        Assertions.assertNotNull(selectionEvent);
    }

    @Test
    void preserveExistingSelectionOnDataChange_noExtraChangeEventsFired() {
        listBox.setSelectionPreservationMode(
                SelectionPreservationMode.PRESERVE_EXISTING);

        Item selectedItem = items.get(0);
        listBox.select(selectedItem);
        Assertions.assertEquals(Set.of(selectedItem),
                listBox.getSelectedItems());
        Assertions.assertNotNull(selectionEvent);
        selectionEvent = null;

        listBox.getDataProvider().refreshAll();
        Assertions.assertEquals(Set.of(selectedItem),
                listBox.getSelectedItems());
        Assertions.assertNull(selectionEvent);

        items.remove(items.get(1));
        listBox.getDataProvider().refreshAll();
        Assertions.assertEquals(Set.of(selectedItem),
                listBox.getSelectedItems());
        Assertions.assertNull(selectionEvent);

        items.remove(selectedItem);
        listBox.getDataProvider().refreshAll();
        Assertions.assertTrue(listBox.getSelectedItems().isEmpty());
        Assertions.assertNotNull(selectionEvent);
    }

    @Test
    void preserveAllSelectionOnDataChange_noExtraChangeEventsFired() {
        listBox.setSelectionPreservationMode(
                SelectionPreservationMode.PRESERVE_ALL);

        Item selectedItem = items.get(0);
        listBox.select(selectedItem);
        Assertions.assertEquals(Set.of(selectedItem),
                listBox.getSelectedItems());
        Assertions.assertNotNull(selectionEvent);
        selectionEvent = null;

        listBox.getDataProvider().refreshAll();
        Assertions.assertEquals(Set.of(selectedItem),
                listBox.getSelectedItems());
        Assertions.assertNull(selectionEvent);

        items.remove(items.get(1));
        listBox.getDataProvider().refreshAll();
        Assertions.assertEquals(Set.of(selectedItem),
                listBox.getSelectedItems());
        Assertions.assertNull(selectionEvent);

        items.remove(selectedItem);
        listBox.getDataProvider().refreshAll();
        Assertions.assertEquals(Set.of(selectedItem),
                listBox.getSelectedItems());
        Assertions.assertNull(selectionEvent);
    }

    private void assertValueChangeEvents(Set<Item>... expectedValues) {
        Assertions.assertEquals(expectedValues.length, eventValues.size());
        IntStream.range(0, expectedValues.length).forEach(i -> {
            Assertions.assertEquals(expectedValues[i], eventValues.get(i));
        });
    }

    private void assertSelectedValuesProperty(int... indices) {
        ArrayNode selectedValues = (ArrayNode) listBox.getElement()
                .getPropertyRaw("selectedValues");
        Set<Integer> actualIndices = jsonArrayToSet(selectedValues);
        Assertions.assertEquals(selectedValues.size(), indices.length,
                "The selectedValues property had different length than expected.");
        for (int index : indices) {
            Assertions.assertTrue(actualIndices.contains(index),
                    "The selectedValues property didn't contain expected value.");
        }
    }

    private void assertItemContents(String... expected) {
        String[] contents = listBox.getChildren()
                .map(c -> c.getElement().getChild(0).getText())
                .toArray(String[]::new);
        Assertions.assertArrayEquals(expected, contents);
    }

    private <T> Set<T> createSet(T... items) {
        return new HashSet<>(Arrays.asList(items));
    }

    private Set<Integer> jsonArrayToSet(ArrayNode jsonArray) {
        return jsonArray.valueStream().map(JsonNode::asInt)
                .collect(Collectors.toSet());
    }

    public static class Item {
        private String name;

        public Item(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
