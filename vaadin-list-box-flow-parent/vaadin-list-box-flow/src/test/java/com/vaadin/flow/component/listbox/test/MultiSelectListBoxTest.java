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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.listbox.dataview.ListBoxListDataView;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.selection.MultiSelectionEvent;

import elemental.json.JsonArray;

public class MultiSelectListBoxTest {

    private MultiSelectListBox<Item> listBox;

    private Item foo, bar;

    private List<Item> items;
    private ListDataProvider<Item> dataProvider;
    private ListBoxListDataView<Item> dataView;

    private List<Set<Item>> eventValues;

    private MultiSelectionEvent<MultiSelectListBox<Item>, Item> selectionEvent;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void init() {
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
    public void hasEmptySetAsDefaultValue() {
        Set<Item> value = listBox.getValue();
        Assert.assertNotNull(value);
        Assert.assertTrue(value.isEmpty());
    }

    @Test
    public void setValueNull_throws() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Use the clear-method");
        listBox.setValue(null);
    }

    @Test
    public void setValue_refreshAll_valueCleared() {
        listBox.setValue(createSet(foo));
        dataProvider.refreshAll();
        Assert.assertEquals(Collections.emptySet(), listBox.getValue());
    }

    @Test
    public void setValue_eventFired() {
        listBox.setValue(createSet(foo));
        assertValueChangeEvents(createSet(foo));
        listBox.setValue(createSet(foo, bar));
        assertValueChangeEvents(createSet(foo), createSet(foo, bar));
    }

    @Test
    public void setEqualSetAsValue_noEvent() {
        listBox.setValue(createSet(foo, bar));
        assertValueChangeEvents(createSet(foo, bar));

        listBox.setValue(createSet(bar, foo));
        assertValueChangeEvents(createSet(foo, bar));
    }

    @Test
    public void selectedValuesPropertyReflectsSelectedIndices() {
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
    public void nonItemComponentsIgnoredInSelectedIndices() {
        listBox.prependComponents(bar, new Div());
        listBox.setValue(createSet(foo, bar));
        assertSelectedValuesProperty(0, 1);
    }

    @Test
    public void changeData_refreshAll_itemComponentsUpdated() {
        foo.setName("foo updated");
        bar.setName("bar updated");
        dataView.refreshItem(foo);
        dataView.refreshItem(bar);
        assertItemContents("foo updated", "bar updated");
    }

    @Test
    public void changeData_refreshItem_itemComponentUpdated() {
        foo.setName("foo updated");
        bar.setName("bar updated");
        dataView.refreshItem(foo);
        assertItemContents("foo updated", "bar");
    }

    @Test
    public void setValue_selectionEventFired() {
        listBox.setValue(createSet(foo));
        listBox.setValue(createSet(bar));

        Assert.assertEquals(createSet(bar), selectionEvent.getAddedSelection());
        Assert.assertEquals(createSet(foo),
                selectionEvent.getRemovedSelection());

        Assert.assertEquals(createSet(bar), selectionEvent.getValue());
        Assert.assertEquals(createSet(bar), selectionEvent.getNewSelection());
        Assert.assertEquals(createSet(bar),
                selectionEvent.getAllSelectedItems());

        Assert.assertEquals(createSet(foo), selectionEvent.getOldSelection());

        Assert.assertEquals(bar, selectionEvent.getFirstSelectedItem().get());
    }

    @Test
    public void updateSelection_valueChangeEventFired() {
        listBox.setValue(createSet(foo));
        listBox.updateSelection(createSet(bar), createSet(foo));
        assertValueChangeEvents(createSet(foo), createSet(bar));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getValue_modifySet_throws() {
        listBox.getValue().add(new Item("baz"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getSelectedItems_modifySet_throws() {
        listBox.getSelectedItems().add(new Item("baz"));
    }

    @Test
    public void dataViewForFaultyDataProvider_throwsException() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(
                "ListBoxListDataView only supports 'ListDataProvider' "
                        + "or it's subclasses, but was given a "
                        + "'AbstractBackEndDataProvider'");

        MultiSelectListBox<String> multiSelectListBox = new MultiSelectListBox<>();
        final ListBoxListDataView<String> dataView = multiSelectListBox
                .setItems(Arrays.asList("one", "two"));

        DataProvider<String, Void> dataProvider = DataProvider
                .fromCallbacks(query -> Stream.of("one"), query -> 1);

        multiSelectListBox.setItems(dataProvider);

        multiSelectListBox.getListDataView();
    }

    @Test
    public void setIdentifierProvider_setItemsWithIdentifierOnly_shouldSelectCorrectItem() {
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
        Assert.assertArrayEquals(new long[] { 1L }, selectedIds);

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
        Assert.assertArrayEquals(new long[] { 2L }, selectedIds);
    }

    @Test
    public void setIdentifierProvider_setItemWithIdAndWrongName_shouldSelectCorrectItemBasedOnIdNotEquals() {
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
        Assert.assertArrayEquals(new long[] { 1L }, selectedIds);

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
        Assert.assertArrayEquals(new long[] { 3L }, selectedIds);
    }

    @Test
    public void withoutSettingIdentifierProvider_setItemWithNullId_shouldSelectCorrectItemBasedOnEquals() {
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

        Assert.assertNotNull(multiSelectListBox.getValue());

        long[] selectedIds = multiSelectListBox.getSelectedItems().stream()
                .mapToLong(CustomItem::getId).toArray();

        Assert.assertArrayEquals(new long[] { 2L }, selectedIds);
    }

    @Test
    public void setIdentifierProviderOnId_setItemWithNullId_shouldThrowException() {

        thrown.expect(NullPointerException.class);

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

        multiSelectListBox.setValue(new CustomItem(null, "First"));
    }

    private void assertValueChangeEvents(Set<Item>... expectedValues) {
        Assert.assertEquals(expectedValues.length, eventValues.size());
        IntStream.range(0, expectedValues.length).forEach(i -> {
            Assert.assertEquals(expectedValues[i], eventValues.get(i));
        });
    }

    private void assertSelectedValuesProperty(int... indices) {
        JsonArray selectedValues = (JsonArray) listBox.getElement()
                .getPropertyRaw("selectedValues");
        Set<Integer> actualIndices = jsonArrayToSet(selectedValues);
        Assert.assertEquals(
                "The selectedValues property had different length than expected.",
                selectedValues.length(), indices.length);
        for (int index : indices) {
            Assert.assertThat(
                    "The selectedValues property didn't contain expected value.",
                    actualIndices, CoreMatchers.hasItem(index));
        }
    }

    private void assertItemContents(String... expected) {
        String[] contents = listBox.getChildren()
                .map(c -> c.getElement().getChild(0).getText())
                .toArray(String[]::new);
        Assert.assertArrayEquals(expected, contents);
    }

    private <T> Set<T> createSet(T... items) {
        Set<T> set = new HashSet<>();
        Arrays.stream(items).forEach(set::add);
        return set;
    }

    private Set<Integer> jsonArrayToSet(JsonArray jsonArray) {
        Set<Integer> set = new HashSet<>();
        IntStream.range(0, jsonArray.length()).forEach(i -> {
            set.add((int) jsonArray.getNumber(i));
        });
        return set;
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
