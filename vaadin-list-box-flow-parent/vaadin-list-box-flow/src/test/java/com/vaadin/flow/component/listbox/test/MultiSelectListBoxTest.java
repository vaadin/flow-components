/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.listbox.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.selection.MultiSelectionEvent;

import elemental.json.JsonArray;

public class MultiSelectListBoxTest {

    private MultiSelectListBox<Item> listBox;

    private Item foo, bar;

    private List<Item> items;
    private ListDataProvider<Item> dataProvider;

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
        listBox.setDataProvider(dataProvider);

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
        dataProvider.refreshAll();
        assertItemContents("foo updated", "bar updated");
    }

    @Test
    public void changeData_refreshItem_itemComponentUpdated() {
        foo.setName("foo updated");
        bar.setName("bar updated");
        dataProvider.refreshItem(foo);
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
